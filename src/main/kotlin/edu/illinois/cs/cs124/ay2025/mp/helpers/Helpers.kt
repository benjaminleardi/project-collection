package edu.illinois.cs.cs124.ay2025.mp.helpers

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import java.time.Instant

val objectMapper = jacksonObjectMapper()

const val CHECK_SERVER_RESPONSE = "AY2025"

fun interface TimeProvider {
    fun now(): Instant
}

class SystemTimeProvider : TimeProvider {
    override fun now(): Instant = Instant.now()
}

private var timeProvider: TimeProvider = SystemTimeProvider()

fun getTimeProvider(): TimeProvider = timeProvider

fun setTimeProvider(provider: TimeProvider) {
    timeProvider = provider
}
