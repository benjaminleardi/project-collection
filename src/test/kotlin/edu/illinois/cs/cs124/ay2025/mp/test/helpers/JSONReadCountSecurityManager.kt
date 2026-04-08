@file:Suppress("detekt:all")

package edu.illinois.cs.cs124.ay2025.mp.test.helpers

import com.google.common.truth.Truth.assertWithMessage
import java.net.URISyntaxException
import java.nio.file.Path
import java.security.Permission

class JSONReadCountSecurityManager : SecurityManager() {
    private val jsonPath: Path

    private var jsonReadCount = 0

    init {
        try {
            jsonPath = Path.of(JSONReadCountSecurityManager::class.java.getResource("/events.json")!!.toURI())
        } catch (e: URISyntaxException) {
            @Suppress("TooGenericExceptionThrown")
            throw RuntimeException(e)
        }
    }

    override fun checkPermission(perm: Permission) {}

    override fun checkPermission(perm: Permission, context: Any) {}

    override fun checkRead(file: String) {
        try {
            if (Path.of(file) == jsonPath) {
                jsonReadCount++
            }
        } catch (ignored: Exception) {
        }
        super.checkRead(file)
    }

    override fun checkRead(file: String, context: Any) {
        try {
            if (Path.of(file) == jsonPath) {
                jsonReadCount++
            }
        } catch (ignored: Exception) {
        }
        super.checkRead(file, context)
    }

    fun checkCount(count: Int) {
        assertWithMessage("events.json should only be accessed during server start")
            .that(jsonReadCount)
            .isAtMost(count)
    }

    fun checkCount() {
        checkCount(EXPECTED_READ_COUNT)
    }
}

private const val EXPECTED_READ_COUNT = 3
