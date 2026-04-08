@file:Suppress("detekt:all")
@file:JvmName("DataKt")

package edu.illinois.cs.cs124.ay2025.mp.test.helpers

import androidx.annotation.NonNull
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import edu.illinois.cs.cs124.ay2025.mp.helpers.getTimeProvider
import edu.illinois.cs.cs124.ay2025.mp.models.EventData
import edu.illinois.cs.cs124.ay2025.mp.models.Summary
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Locale
import java.util.Random

@NonNull
@JvmField
val objectMapper: ObjectMapper = jacksonObjectMapper()

@NonNull
private const val EVENTS_FINGERPRINT = "feb8683086258d7881e2d3c0d2f86952"

@NonNull
@JvmField
val EVENT_DATA: List<EventData> = loadEventData()

@NonNull
@JvmField
val SUMMARIES: List<Summary> = loadSummaries()

val SUMMARY_COUNT = SUMMARIES.size

fun getSummaryCountFromToday(): Int {
    val now: Instant = getTimeProvider().now()
    val nowZoned: ZonedDateTime = now.atZone(ZoneId.of("America/Chicago"))
    val startOfToday: ZonedDateTime = nowZoned.toLocalDate().atStartOfDay(ZoneId.of("America/Chicago"))

    return SUMMARIES.count { summary ->
        try {
            val eventStart: ZonedDateTime = ZonedDateTime.parse(summary.start)
            !eventStart.isBefore(startOfToday)
        } catch (_: Exception) {
            true
        }
    }
}

fun getSummaryCountToday(): Int {
    val now: Instant = getTimeProvider().now()
    val nowZoned: ZonedDateTime = now.atZone(ZoneId.of("America/Chicago"))
    val startOfToday: ZonedDateTime = nowZoned.toLocalDate().atStartOfDay(ZoneId.of("America/Chicago"))
    val startOfTomorrow: ZonedDateTime = startOfToday.plusDays(1)

    return SUMMARIES.count { summary ->
        try {
            val eventStart: ZonedDateTime =
                ZonedDateTime.parse(summary.start)
                    .withZoneSameInstant(ZoneId.of("America/Chicago"))
            !eventStart.isBefore(startOfToday) && eventStart.isBefore(startOfTomorrow)
        } catch (_: Exception) {
            false
        }
    }
}

@NonNull
fun loadEventData(): List<EventData> {
    val json: String = loadAndFingerprintJSON()
    try {
        val root: com.fasterxml.jackson.databind.JsonNode = objectMapper.readTree(json)
        val eventsArray: com.fasterxml.jackson.databind.JsonNode = root.get("events")
        val events: List<EventData> = objectMapper.convertValue(
            eventsArray,
            object : TypeReference<List<EventData>>() {},
        )
        return events.toList()
    } catch (e: JsonProcessingException) {
        error(e.message ?: "Failed to parse JSON")
    }
}

@NonNull
fun loadSummaries(): List<Summary> = loadEventData().map { Summary(it) }.toList()

@NonNull
fun loadAndFingerprintJSON(): String {
    val digest: MessageDigest
    try {
        digest = MessageDigest.getInstance("MD5")
    } catch (_: Exception) {
        error("MD5 algorithm should be available")
    }

    val input: String = readEventDataFile()

    val toFingerprint: String =
        input.split("\n")
            .joinToString("\n") { it.trimEnd() }

    val currentFingerprint: String =
        String.format(
            Locale.US,
            "%1$32s",
            BigInteger(1, digest.digest(toFingerprint.toByteArray(StandardCharsets.UTF_8)))
                .toString(16),
        )
            .replace(' ', '0')

    if (currentFingerprint != EVENTS_FINGERPRINT) {
        error("events.json has been modified. Please restore the original version of the file.")
    }
    return input
}

@NonNull
fun getShuffledSummaries(seed: Int, size: Int) = SUMMARIES
    .toList()
    .shuffled(Random(seed.toLong()))
    .subList(0, size.coerceAtMost(SUMMARY_COUNT))
