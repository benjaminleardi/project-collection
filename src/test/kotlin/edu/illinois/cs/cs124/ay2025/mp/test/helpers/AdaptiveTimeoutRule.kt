@file:Suppress("detekt:all")

package edu.illinois.cs.cs124.ay2025.mp.test.helpers

import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.junit.runners.model.TestTimedOutException
import org.robolectric.junit.rules.TimeoutRule
import java.io.InterruptedIOException
import java.util.Locale
import java.util.concurrent.TimeUnit

class AdaptiveTimeoutRule : TestRule {
    companion object {
        private var anyTestClassSeen = false
    }

    override fun apply(base: Statement, description: Description): Statement {
        val annotation = description.getAnnotation(AdaptiveTimeout::class.java) ?: return base

        val className = description.className
        val simpleClassName = className.substring(className.lastIndexOf('.') + 1)
        val methodName = description.methodName
        val isInitialized = anyTestClassSeen

        anyTestClassSeen = true

        val timeoutConfig = calculateTimeout(annotation, simpleClassName, methodName, isInitialized)
        val timingStatement = createTimingStatement(base, simpleClassName, methodName)
        val timeoutStatement = applyTimeoutToStatement(timingStatement, description, timeoutConfig.timeoutMs)

        return createEnhancedTimeoutStatement(
            timeoutStatement,
            description,
            timeoutConfig,
            isInitialized,
        )
    }

    private data class TimeoutConfig(
        val baseTimeoutMs: Long,
        val multiplier: Double,
        val rawTimeout: Long,
        val minTimeout: Long,
        val maxTimeout: Long,
        val timeoutMs: Long,
    )

    private fun calculateTimeout(
        annotation: AdaptiveTimeout,
        simpleClassName: String,
        methodName: String,
        isInitialized: Boolean,
    ): TimeoutConfig = calculateNormalTimeout(annotation, simpleClassName, methodName, isInitialized)

    private fun calculateNormalTimeout(
        annotation: AdaptiveTimeout,
        simpleClassName: String,
        methodName: String,
        isInitialized: Boolean,
    ): TimeoutConfig {
        val baseTimeoutMs = calculateAnnotationBasedTimeout(annotation, isInitialized)

        val multiplierStr = System.getenv("TEST_TIMEOUT_MULTIPLIER") ?: error(
            "TEST_TIMEOUT_MULTIPLIER environment variable must be set. " +
                "Recommended value: 4 for local development, higher for CI/containers.",
        )
        val multiplier = multiplierStr.toDouble()
        val rawTimeout = (baseTimeoutMs * multiplier).toLong()

        val minStr = System.getenv("TEST_TIMEOUT_MIN") ?: error(
            "TEST_TIMEOUT_MIN environment variable must be set. " +
                "Recommended value: 5000 (5 seconds minimum for short tests).",
        )
        val minTimeout = minStr.toLong()

        val maxStr = System.getenv("TEST_TIMEOUT_MAX") ?: error(
            "TEST_TIMEOUT_MAX environment variable must be set. " +
                "Recommended value: 30000 (30 seconds maximum to prevent excessive waits).",
        )
        val maxTimeout = maxStr.toLong()

        val timeoutMs = maxOf(minTimeout, minOf(maxTimeout, rawTimeout))

        return TimeoutConfig(
            baseTimeoutMs = baseTimeoutMs,
            multiplier = multiplier,
            rawTimeout = rawTimeout,
            minTimeout = minTimeout,
            maxTimeout = maxTimeout,
            timeoutMs = timeoutMs,
        )
    }

    private fun calculateAnnotationBasedTimeout(annotation: AdaptiveTimeout, isInitialized: Boolean): Long =
        if (isInitialized) {
            annotation.fast
        } else {
            annotation.slow
        }

    private fun createTimingStatement(base: Statement, simpleClassName: String, methodName: String): Statement = base

    private fun applyTimeoutToStatement(statement: Statement, description: Description, timeoutMs: Long): Statement {
        val robolectricTimeout = TimeoutRule.millis(timeoutMs)
        return robolectricTimeout.apply(statement, description)
    }

    private fun createEnhancedTimeoutStatement(
        timeoutStatement: Statement,
        description: Description,
        timeoutConfig: TimeoutConfig,
        isInitialized: Boolean,
    ): Statement {
        return object : Statement() {
            override fun evaluate() {
                try {
                    timeoutStatement.evaluate()
                } catch (e: TestTimedOutException) {
                    throw createTimeoutException(e.message ?: "Test timed out")
                } catch (_: InterruptedIOException) {
                    throw createTimeoutException("Test interrupted during I/O operation (likely timeout)")
                } catch (_: InterruptedException) {
                    throw createTimeoutException("Test interrupted (likely timeout)")
                }
            }

            private fun createTimeoutException(baseMessage: String): Exception {
                val constraintNote = when {
                    timeoutConfig.rawTimeout < timeoutConfig.minTimeout ->
                        String.format(Locale.US, " (raised from %dms to MIN)", timeoutConfig.rawTimeout)
                    timeoutConfig.rawTimeout > timeoutConfig.maxTimeout ->
                        String.format(Locale.US, " (capped from %dms to MAX)", timeoutConfig.rawTimeout)
                    else -> ""
                }

                val path = if (isInitialized) {
                    "fast/suite"
                } else {
                    "slow/isolated"
                }
                val contextInfo = String.format(
                    "\nTimeout details:\n" +
                        "  Base timeout: %dms (%s path)\n" +
                        "  Multiplier: %.1f (TEST_TIMEOUT_MULTIPLIER)\n" +
                        "  Raw timeout: %dms (base × multiplier)\n" +
                        "  Constraints: MIN=%dms, MAX=%dms\n" +
                        "  Applied timeout: %dms%s\n" +
                        "  Test: %s",
                    timeoutConfig.baseTimeoutMs,
                    path,
                    timeoutConfig.multiplier,
                    timeoutConfig.rawTimeout,
                    timeoutConfig.minTimeout,
                    timeoutConfig.maxTimeout,
                    timeoutConfig.timeoutMs,
                    constraintNote,
                    description.methodName,
                )
                val fullMessage = baseMessage + contextInfo
                val exception = TestTimedOutException(timeoutConfig.timeoutMs, TimeUnit.MILLISECONDS)
                exception.initCause(Exception(fullMessage))
                return exception
            }
        }
    }
}
