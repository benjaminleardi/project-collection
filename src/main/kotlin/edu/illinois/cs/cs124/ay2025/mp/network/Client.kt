package edu.illinois.cs.cs124.ay2025.mp.network

import android.os.Build
import com.fasterxml.jackson.core.type.TypeReference
import edu.illinois.cs.cs124.ay2025.mp.application.SERVER_URL
import edu.illinois.cs.cs124.ay2025.mp.helpers.ResultMightThrow
import edu.illinois.cs.cs124.ay2025.mp.helpers.objectMapper
import edu.illinois.cs.cs124.ay2025.mp.models.Event
import edu.illinois.cs.cs124.ay2025.mp.models.Favorite
import edu.illinois.cs.cs124.ay2025.mp.models.Summary
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

/**
 * STARTUP STEP 3: Client singleton object
 *
 * This singleton is initialized the first time it's referenced (lazy initialization).
 * The init block runs automatically when the object is first accessed.
 *
 * Purpose: Provides methods to communicate with the Server and fetch event data.
 */
object Client {
    /**
     * STARTUP STEP 6: Fetch event summaries from the server
     *
     * This method is called from MainActivity.onResume() to load event data.
     * The request happens on a background thread, and the callback runs when complete.
     *
     * @param callback Function that receives the result (success or error)
     */
    fun getSummaries(callback: (ResultMightThrow<List<Summary>>) -> Any?) {
        // Execute the network request on a background thread (not the main UI thread)
        executor.execute {
            try {
                // Build the HTTP GET request to the /summary/ endpoint
                val request = Request.Builder()
                    .url("$SERVER_URL/summary/")
                    .get()
                    .build()

                // Execute the request and automatically close the response when done
                httpClient.newCall(request).execute().use { response ->
                    // Check if the server returned an error status code
                    if (!response.isSuccessful) {
                        callback(
                            ResultMightThrow(IOException("Unexpected response code: ${response.code}")),
                        )
                        return@execute
                    }

                    // Read the JSON response body as a string
                    val responseBody = response.body.string()

                    // Parse the JSON string into a List<Summary> using Jackson
                    val summaries: List<Summary> =
                        objectMapper.readValue(responseBody, object : TypeReference<List<Summary>>() {})

                    // Call the callback with the successful result
                    callback(ResultMightThrow(summaries))
                }
            } catch (e: IOException) {
                // If any network error occurs, pass it to the callback
                callback(ResultMightThrow(e))
            }
        }
    }

    /**
     * Fetch full event details for a specific event by ID
     *
     * Makes a request to the /event/{id} endpoint and returns complete event information.
     * The request happens on a background thread, and the callback runs when complete.
     *
     * @param eventId The unique identifier of the event to retrieve
     * @param callback Function that receives the result (success or error)
     */
    fun getEvent(eventId: String, callback: (ResultMightThrow<Event>) -> Any?) {
        // Execute the network request on a background thread (not the main UI thread)
        executor.execute {
            try {
                // Build the HTTP GET request to the /event/{id} endpoint
                val request = Request.Builder()
                    .url("$SERVER_URL/event/$eventId")
                    .get()
                    .build()

                // Execute the request and automatically close the response when done
                httpClient.newCall(request).execute().use { response ->
                    // Check if the server returned an error status code
                    if (!response.isSuccessful) {
                        callback(
                            ResultMightThrow(IOException("Unexpected response code: ${response.code}")),
                        )
                        return@execute
                    }

                    // Read the JSON response body as a string
                    val responseBody = response.body.string()

                    // Parse the JSON string into an Event (EventData) using Jackson
                    val event: Event = objectMapper.readValue(responseBody, Event::class.java)

                    // Call the callback with the successful result
                    callback(ResultMightThrow(event))
                }
            } catch (e: IOException) {
                // If any network error occurs, pass it to the callback
                callback(ResultMightThrow(e))
            }
        }
    }

    /**
     * Fetch favorite status for a specific event by ID
     *
     * Makes a request to the /favorite/{id} endpoint and returns the favorite status.
     * The request happens on a background thread, and the callback runs when complete.
     *
     * @param eventId The unique identifier of the event
     * @param callback Function that receives the result (success or error)
     */
    fun getFavorite(eventId: String, callback: (ResultMightThrow<Boolean>) -> Any?) {
        // Execute the network request on a background thread (not the main UI thread)
        executor.execute {
            try {
                // Build the HTTP GET request to the /favorite/{id} endpoint
                val request = Request.Builder()
                    .url("$SERVER_URL/favorite/$eventId")
                    .get()
                    .build()

                // Execute the request and automatically close the response when done
                httpClient.newCall(request).execute().use { response ->
                    // Check if the server returned an error status code
                    if (!response.isSuccessful) {
                        callback(
                            ResultMightThrow(IOException("Unexpected response code: ${response.code}")),
                        )
                        return@execute
                    }

                    // Read the JSON response body as a string
                    val responseBody = response.body.string()

                    // Parse the JSON string into a Favorite object using Jackson
                    val favorite: Favorite = objectMapper.readValue(responseBody, Favorite::class.java)

                    // Call the callback with the favorite status (boolean)
                    callback(ResultMightThrow(favorite.favorite))
                }
            } catch (e: IOException) {
                // If any network error occurs, pass it to the callback
                callback(ResultMightThrow(e))
            }
        }
    }

    /**
     * Set favorite status for a specific event
     *
     * Makes a POST request to the /favorite endpoint to save the favorite status.
     * The request happens on a background thread, and the callback runs when complete.
     *
     * @param eventId The unique identifier of the event
     * @param isFavorite The favorite status to set (true or false)
     * @param callback Function that receives the result (success or error)
     */
    fun setFavorite(eventId: String, isFavorite: Boolean, callback: (ResultMightThrow<Boolean>) -> Any?) {
        // Execute the network request on a background thread (not the main UI thread)
        executor.execute {
            try {
                // Create a Favorite object with the event ID and favorite status
                val favorite = Favorite(eventId, isFavorite)

                // Serialize the Favorite object to JSON
                val jsonBody = objectMapper.writeValueAsString(favorite)

                // Create a request body with JSON content type
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val requestBody = jsonBody.toRequestBody(mediaType)

                // Build the HTTP POST request to the /favorite endpoint
                val request = Request.Builder()
                    .url("$SERVER_URL/favorite")
                    .post(requestBody)
                    .build()

                // Execute the request and automatically close the response when done
                httpClient.newCall(request).execute().use { response ->
                    // Check if the server returned an error status code
                    if (!response.isSuccessful) {
                        callback(
                            ResultMightThrow(IOException("Unexpected response code: ${response.code}")),
                        )
                        return@execute
                    }

                    // Read the JSON response body as a string
                    val responseBody = response.body.string()

                    // Parse the JSON string into a Favorite object using Jackson
                    val updatedFavorite: Favorite = objectMapper.readValue(responseBody, Favorite::class.java)

                    // Call the callback with the favorite status (boolean)
                    callback(ResultMightThrow(updatedFavorite.favorite))
                }
            } catch (e: IOException) {
                // If any network error occurs, pass it to the callback
                callback(ResultMightThrow(e))
            }
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////
    // YOU SHOULD NOT NEED TO MODIFY THE CODE BELOW
    // ///////////////////////////////////////////////////////////////////////////////////////////////

    private val httpClient: OkHttpClient
    private val executor: ExecutorService

    /**
     * STARTUP STEP 3 (continued): Initialize the HTTP client and thread pool
     *
     * This init block runs once when the Client object is first accessed.
     * It sets up the infrastructure needed for making network requests.
     */
    init {
        val testing = Build.FINGERPRINT == "robolectric"

        // Create the HTTP client with timeout and retry settings
        httpClient =
            OkHttpClient.Builder()
                .callTimeout(4.seconds.toJavaDuration())
                .retryOnConnectionFailure(true)
                .build()

        // Create thread pool for async operations
        // Single thread during testing for deterministic behavior
        // Cached thread pool in production for better performance
        executor = if (testing) {
            Executors.newSingleThreadExecutor()
        } else {
            Executors.newCachedThreadPool()
        }
    }
}
