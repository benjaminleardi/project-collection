@file:Suppress("detekt:all", "UNCHECKED_CAST")
@file:JvmName("HTTP")

package edu.illinois.cs.cs124.ay2025.mp.test.helpers

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.JsonNode
import com.google.common.truth.Truth.assertWithMessage
import edu.illinois.cs.cs124.ay2025.mp.application.SERVER_URL
import edu.illinois.cs.cs124.ay2025.mp.helpers.ResultMightThrow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import java.io.IOException
import java.net.HttpURLConnection
import java.time.Duration
import java.time.Instant
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

data class TimedResponse<T>(val response: T, val responseTime: Duration)

private val httpClient = OkHttpClient.Builder().callTimeout(4.seconds.toJavaDuration()).build()

@Throws(IOException::class)
fun <T> testServerGet(route: String, responseCode: Int, klass: Any?): TimedResponse<T> {
    val request = Request.Builder().url(SERVER_URL + route).build()
    val start = Instant.now()

    httpClient.newCall(request).execute().use { response ->
        val responseTime = Duration.between(start, Instant.now())

        if (responseCode == HttpURLConnection.HTTP_OK) {
            assertWithMessage("GET request for $route should have succeeded")
                .that(response.code)
                .isEqualTo(HttpURLConnection.HTTP_OK)
        } else {
            assertWithMessage(
                "GET request for $route should have failed with code $responseCode",
            )
                .that(response.code)
                .isEqualTo(responseCode)
            return TimedResponse(null as T, responseTime)
        }

        val body: ResponseBody? = response.body
        assertWithMessage("GET response for $route body should not be null")
            .that(body)
            .isNotNull()

        if (klass == null) {
            val bodyString = body!!.string()
            return try {
                TimedResponse(objectMapper.readTree(bodyString) as T, responseTime)
            } catch (_: JsonParseException) {
                TimedResponse(bodyString as T, responseTime)
            }
        }

        // All remaining paths expect JSON
        assertWithMessage("Content-Type header not set correctly")
            .that(response.header("Content-Type"))
            .isEqualTo("application/json; charset=utf-8")
        return when (klass) {
            is Class<*> -> {
                TimedResponse(
                    objectMapper.readValue(body!!.string(), klass) as T,
                    responseTime,
                )
            }
            is TypeReference<*> -> {
                TimedResponse(
                    objectMapper.readValue(body!!.string(), klass) as T,
                    responseTime,
                )
            }
            else -> {
                error("Bad deserialization class passed to testServerGet")
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
@Throws(IOException::class)
fun <T> testServerGet(route: String, klass: Any?): T =
    testServerGet<T>(route, HttpURLConnection.HTTP_OK, klass).response

@Throws(IOException::class)
fun <T> testServerGetTimed(route: String, klass: Any?): TimedResponse<T> =
    testServerGet(route, HttpURLConnection.HTTP_OK, klass)

@Suppress("UNCHECKED_CAST")
@Throws(IOException::class)
fun <T> testServerGet(route: String, responseCode: Int): T = testServerGet<T>(route, responseCode, null).response

@Throws(IOException::class)
fun testServerGet(route: String): JsonNode = testServerGet<JsonNode>(route, HttpURLConnection.HTTP_OK, null).response

@Throws(IOException::class)
fun <T> testServerGetTimed(route: String): TimedResponse<T> = testServerGet(route, HttpURLConnection.HTTP_OK, null)

@Suppress("UNCHECKED_CAST")
@Throws(IOException::class)
fun <T> testServerPost(route: String, responseCode: Int, requestBody: Any?, klass: Any?): T? {
    val request = Request.Builder()
        .url(SERVER_URL + route)
        .post(
            objectMapper.writeValueAsString(requestBody)
                .toRequestBody("application/json".toMediaType()),
        )
        .build()

    httpClient.newCall(request).execute().use { response ->

        if (responseCode == HttpURLConnection.HTTP_OK) {
            assertWithMessage(
                "POST request for $route should have succeeded but was ${response.code}",
            )
                .that(response.code)
                .isEqualTo(HttpURLConnection.HTTP_OK)
        } else {
            assertWithMessage(
                "POST request for $route should have failed with code $responseCode",
            )
                .that(response.code)
                .isEqualTo(responseCode)
            return null
        }

        val responseBody: ResponseBody? = response.body
        assertWithMessage("POST response for $route body should not be null")
            .that(responseBody)
            .isNotNull()

        assertWithMessage("Content-Type header not set correctly")
            .that(response.header("Content-Type"))
            .isEqualTo("application/json; charset=utf-8")

        return when (klass) {
            null -> objectMapper.readTree(responseBody!!.string()) as T
            is Class<*> -> objectMapper.readValue(responseBody!!.string(), klass) as T
            is TypeReference<*> -> objectMapper.readValue(responseBody!!.string(), klass) as T
            else -> error("Bad deserialization class passed to testServerPost")
        }
    }
}

@Throws(IOException::class)
fun <T> testServerPost(route: String, requestBody: Any?, klass: Any?): T? =
    testServerPost(route, HttpURLConnection.HTTP_OK, requestBody, klass)

@Throws(IOException::class)
fun <T> testServerPost(route: String, requestBody: Any?, responseCode: Int): T? =
    testServerPost(route, responseCode, requestBody, null)

@Throws(Exception::class)
fun <T> testClient(method: ((ResultMightThrow<T>) -> Any?) -> Any?): T {
    val completableFuture = CompletableFuture<ResultMightThrow<T>>()

    method { result -> completableFuture.complete(result) }

    val result: ResultMightThrow<T> = completableFuture.get(6, TimeUnit.SECONDS)

    if (result.exception != null) {
        throw result.exception as Throwable
    }

    assertWithMessage("Client call expected to succeed returned null")
        .that(result.value)
        .isNotNull()

    return result.value!!
}
