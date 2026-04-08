@file:Suppress("detekt:all")

package edu.illinois.cs.cs124.ay2025.mp.test

import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.hasDescendant
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import edu.illinois.cs.cs124.ay2025.mp.R
import edu.illinois.cs.cs124.ay2025.mp.activities.EventActivity
import edu.illinois.cs.cs124.ay2025.mp.helpers.setTimeProvider
import edu.illinois.cs.cs124.ay2025.mp.models.Event
import edu.illinois.cs.cs124.ay2025.mp.network.Client
import edu.illinois.cs.cs124.ay2025.mp.network.Server
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.AdaptiveTimeout
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.AdaptiveTimeoutRule
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.EVENT_DATA
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.JSONReadCountSecurityManager
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.SUMMARIES
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.checkServerDesign
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.configureLogging
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.countRecyclerView
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.getShuffledSummaries
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.getSummaryCountToday
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.pause
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.startActivity
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.startMainActivity
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.testClient
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.testServerGet
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.testSummaryRoute
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.withRecyclerView
import edu.illinois.cs.cs125.gradlegrader.annotations.Graded
import org.hamcrest.BaseMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.containsString
import org.junit.After
import org.junit.AfterClass
import org.junit.Assert.fail
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.experimental.LazyApplication
import java.io.IOException
import java.net.HttpURLConnection
import java.time.Instant
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * MP2 test suite.
 *
 * The code below is used to evaluate your app during testing, local grading, and official grading.
 * You may not understand all of the code below, but you'll need to have some understanding of how
 * it works so that you can determine what is wrong with your app and what you need to fix.
 *
 * **ALL CHANGES TO THIS FILE WILL BE OVERWRITTEN DURING OFFICIAL GRADING.**
 *
 * You can and should modify the code below if it is useful during your own local testing,
 * but any changes you make will be discarded during official grading.
 * The local grader will not run if the test suites have been modified, so you'll need to undo any
 * local changes before you run the grader.
 *
 * The MP2 test suite is correct but incomplete. The tests we provide are accurate, but they don't
 * cover all possible cases. As part of MP2, you'll continue learning to identify missing test cases and add them
 * yourself. This is an important skill for software development.
 *
 * Note that when you add your own tests or test cases, those tests might be incorrect. We'll help
 * you learn to write good tests, but it's important to understand that a failing test you wrote
 * might indicate a problem with the test, not with your app.
 *
 * ## Test Organization
 *
 * Our test suites are broken into two parts:
 *
 * **Unit tests** can complete without running your app.
 * They test things like whether a method works properly or the behavior of your API server.
 * Unit tests are usually fairly fast.
 *
 * **Integration tests** require simulating your app.
 * This allows us to test things like your API client, and higher-level aspects of your app's
 * behavior, such as whether it displays the right thing on the display.
 * Because integration tests require simulating your entire app, they run more slowly.
 *
 * Our test suites will sometimes include both graded and ungraded tests.
 * The graded tests are marked with the `@Graded` annotation which includes a point amount.
 * Ungraded tests do not have this annotation.
 * Some ungraded tests will work immediately, and are there to help you pinpoint regressions:
 * changes you made that might have broken things that were previously working.
 * The ungraded tests below were actually written by me (Geoff) during MP development.
 * Other ungraded tests are simply there to help your development process.
 *
 * ## Test Class Configuration
 *
 * - `@RunWith(AndroidJUnit4::class)` - Uses the AndroidJUnit4 test runner for Android testing
 * - `@LooperMode(LooperMode.Mode.PAUSED)` - Controls Android's message loop for predictable testing
 * - `@FixMethodOrder(MethodSorters.NAME_ASCENDING)` - Runs tests in order: test0, test1, test2, etc.
 */

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class MP2Test {
    @get:Rule
    val adaptiveTimeout = AdaptiveTimeoutRule()

    @Test
    @AdaptiveTimeout(fast = 1000, slow = 1000)
    @Graded(points = 20, friendlyName = "Server GET /event (Unit)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    @Throws(IOException::class)
    fun test0_ServerEventRoute() {
        testSummaryRoute()

        val trimmedSummaries = getShuffledSummaries(12421, 128)

        for (eventSummary in trimmedSummaries) {
            val event = testServerGet<Event>("/event/" + eventSummary.id, Event::class.java)

            // Add your tests here
        }

        testServerGet<Any>("/events/61801f5ee9ce3704", HttpURLConnection.HTTP_NOT_FOUND)
        testServerGet<Any>("/event/61801f5ee9ce3705", HttpURLConnection.HTTP_NOT_FOUND)
        testServerGet<Any>("/event/", HttpURLConnection.HTTP_NOT_FOUND)
    }

    @Test
    @AdaptiveTimeout(fast = 1000, slow = 1000)
    @Graded(points = 20, friendlyName = "Client getEvent (Integration)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    @Throws(Exception::class)
    fun test1_ClientGetEvent() {
        val trimmedSummaries = getShuffledSummaries(12422, 32)

        for (summary in trimmedSummaries) {
            val event = testClient { callback ->
                Client.getEvent(summary.id, callback)
            }

            // Add your tests here
        }

        try {
            testClient { callback ->
                Client.getEvent("61801f5ee9ce3705", callback)
            }
            fail("Client getEvent for non-existent event should throw")
        } catch (_: Exception) {
        }
    }

    @Suppress("SpellCheckingInspection")
    @Test
    @AdaptiveTimeout(fast = 1000, slow = 1000)
    @Graded(points = 20, friendlyName = "Summary Click Launch (Integration)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    fun test2_SummaryClickLaunch() {
        startMainActivity { activity ->
            onView(withId(R.id.recycler_view)).check(countRecyclerView(getSummaryCountToday()))

            onView(withRecyclerView(R.id.recycler_view).atPosition(2))
                .check(matches(hasDescendant(withText("Freestyle"))))

            onView(withRecyclerView(R.id.recycler_view).atPosition(2)).perform(click())

            val id = shadowOf(activity).nextStartedActivity.getStringExtra("id")
            assertThat(id).isEqualTo("bc1dcfbdef502f70")
        }
    }

    @Test
    @AdaptiveTimeout(fast = 2000, slow = 2000)
    @Graded(points = 20, friendlyName = "Event View (Integration)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    @Throws(Exception::class)
    fun test3_EventView() {
        val completeEvents = mutableListOf<Event>()
        for (summary in SUMMARIES) {
            val event = testClient { callback ->
                Client.getEvent(summary.id, callback)
            }

            if (!event.url.isBlank() &&
                !event.description.isBlank() &&
                !event.categories.isEmpty() &&
                !event.location.isBlank() &&
                !event.source.isBlank()
            ) {
                completeEvents.add(event)
                if (completeEvents.size >= 32) {
                    break
                }
            }
        }

        assertThat(completeEvents.size)
            .isAtLeast(4)

        completeEvents.shuffle(java.util.Random(12424))
        val eventsToTest = completeEvents.take(minOf(4, completeEvents.size))

        for (event in eventsToTest) {
            val intent = Intent(ApplicationProvider.getApplicationContext(), EventActivity::class.java)
            intent.putExtra("id", event.id)

            startActivity<EventActivity>(intent) { activity ->
                pause()

                // Add your tests here

                try {
                    val startTime = ZonedDateTime.parse(event.start)
                    val date = startTime.format(DateTimeFormatter.ofPattern("MMM d"))
                    val time = startTime.format(DateTimeFormatter.ofPattern("h:mm a"))
                    val formattedStart = "$date • $time"
                    onView(first(withText(containsString(formattedStart)))).check(matches(isDisplayed()))
                } catch (_: Exception) {
                    fail("Failed to parse or verify start time")
                }
            }
        }
    }

    @Before
    fun beforeTest() {
        JSON_READ_COUNT_SECURITY_MANAGER.checkCount()
    }

    @After
    fun afterTest() {
        JSON_READ_COUNT_SECURITY_MANAGER.checkCount()
    }

    private fun <T> first(matcher: Matcher<T>): Matcher<T> {
        return object : BaseMatcher<T>() {
            var isFirst = true

            override fun matches(item: Any?): Boolean {
                if (isFirst && matcher.matches(item)) {
                    isFirst = false
                    return true
                }
                return false
            }

            override fun describeTo(description: Description) {
                description.appendText("should return first matching item")
            }
        }
    }

    companion object {
        private val JSON_READ_COUNT_SECURITY_MANAGER = JSONReadCountSecurityManager()

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            setTimeProvider { Instant.parse("2025-10-15T12:00:00Z") }

            checkServerDesign()

            configureLogging()

            EVENT_DATA.size
            SUMMARIES.size

            System.setSecurityManager(JSON_READ_COUNT_SECURITY_MANAGER)

            Server.startServer()
        }

        @AfterClass
        @JvmStatic
        fun afterClass() {
            Server.stopServer()

            System.setSecurityManager(null)
        }
    }
}
