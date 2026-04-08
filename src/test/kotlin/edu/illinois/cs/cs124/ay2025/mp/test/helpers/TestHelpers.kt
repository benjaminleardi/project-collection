@file:Suppress("detekt:all")

package edu.illinois.cs.cs124.ay2025.mp.test.helpers

import android.app.Activity
import android.content.Intent
import android.os.Looper.getMainLooper
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import com.fasterxml.jackson.databind.JsonNode
import com.google.common.truth.Truth.assertWithMessage
import edu.illinois.cs.cs124.ay2025.mp.activities.MainActivity
import edu.illinois.cs.cs124.ay2025.mp.network.Server
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowLog
import java.io.IOException
import kotlin.math.floor
import kotlin.reflect.KVisibility
import kotlin.reflect.full.declaredFunctions
import kotlin.reflect.full.declaredMemberProperties

fun startMainActivity(action: ActivityScenario.ActivityAction<MainActivity>) {
    ActivityScenario.launch(MainActivity::class.java).use { scenario ->
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.moveToState(Lifecycle.State.RESUMED)
        pause()
        scenario.onActivity(action)
    }
}

fun <T : Activity> startActivity(intent: Intent, action: ActivityScenario.ActivityAction<T>) {
    ActivityScenario.launch<T>(intent).use { scenario ->
        scenario.moveToState(Lifecycle.State.CREATED)
        scenario.moveToState(Lifecycle.State.RESUMED)
        pause()
        scenario.onActivity(action)
    }
}

fun pause(length: Int) {
    shadowOf(getMainLooper()).runToEndOfTasks()
    Thread.sleep(length.toLong())
}

fun pause() {
    pause(100)
}

fun configureLogging() {
    if (System.getenv("OFFICIAL_GRADING") == null) {
        ShadowLog.setLoggable("LifecycleMonitor", Log.WARN)
        ShadowLog.stream = FilteringPrintStream()
    }
}

fun trimmedMean(values: List<Long>, percent: Double): Double {
    val toReturn = values.toMutableList()
    toReturn.sort()
    val toDrop = floor(toReturn.size * percent).toInt()
    val toSum = toReturn.subList(toDrop, toReturn.size - toDrop)
    return toSum.sum().toDouble() / toSum.size
}

fun checkServerDesign() {
    val okMethods = listOf("dispatch", "startServer", "stopServer")
    val nonPrivateMethods = Server::class.declaredFunctions.filter { method ->
        method.visibility != KVisibility.PRIVATE && !okMethods.contains(method.name)
    }.map { method ->
        method.name
    }
    val nonPrivateFields =
        Server::class.declaredMemberProperties
            .filter { field -> field.visibility != KVisibility.PRIVATE && field.name != "INSTANCE" }
            .map { field -> field.name }
    val nonPrivateClasses =
        Server::class.nestedClasses.filter { klass -> klass.visibility != KVisibility.PRIVATE }
            .map { klass -> klass.simpleName }
    assertWithMessage("Server has visible methods, fields, or classes")
        .that(nonPrivateMethods + nonPrivateFields + nonPrivateClasses).isEmpty()
}

@Throws(IOException::class)
fun testSummaryRoute() {
    val nodes: JsonNode = testServerGet("/summary", JsonNode::class.java)
    assertWithMessage("Summary list is not the right size")
        .that(nodes)
        .hasSize(getSummaryCountFromToday())
    nodes.forEach { node ->
        assertWithMessage("Summary node has wrong number of fields")
            .that(node.size())
            .isAtLeast(3)
        assertWithMessage("Summary node has wrong number of fields")
            .that(node.size())
            .isAtMost(6)
    }
}

fun readEventDataFile(): String = Server::class.java.getResource("/events.json")!!.readText()
