@file:Suppress("detekt:all")

package edu.illinois.cs.cs124.ay2025.mp.test

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import edu.illinois.cs.cs124.ay2025.mp.R
import edu.illinois.cs.cs124.ay2025.mp.helpers.setTimeProvider
import edu.illinois.cs.cs124.ay2025.mp.models.Summary
import edu.illinois.cs.cs124.ay2025.mp.models.filterTime
import edu.illinois.cs.cs124.ay2025.mp.models.filterVirtual
import edu.illinois.cs.cs124.ay2025.mp.models.search
import edu.illinois.cs.cs124.ay2025.mp.network.Server
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.AdaptiveTimeout
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.AdaptiveTimeoutRule
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.SUMMARIES
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.configureLogging
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.countRecyclerView
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.getShuffledSummaries
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.pause
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.searchFor
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.startMainActivity
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.withRecyclerView
import edu.illinois.cs.cs125.gradlegrader.annotations.Graded
import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.experimental.LazyApplication
import java.time.Instant

/**
 * MP1 test suite.
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
 * The MP1 test suite is correct but incomplete. The tests we provide are accurate, but they don't
 * cover all possible cases. As part of MP1, you'll learn to identify missing test cases and add them
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
class MP1Test {
    @get:Rule
    val adaptiveTimeout = AdaptiveTimeoutRule()

    @Suppress("SpellCheckingInspection")
    @Test
    @AdaptiveTimeout(fast = 1000, slow = 1000)
    @Graded(points = 10, friendlyName = "Test Summary Sort (Unit)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    fun test0_testSummarySort() {
        // Add your tests here
        // Test sorting by start time (ascending) then by title
        val customList = listOf(
            Summary("3", "B", "2025-01-01T10:00:01Z", "Test"),
            Summary("1", "A", "2025-01-01T10:00:00Z", "Test"),
            Summary("4", "AA", "2025-01-01T10:00:01Z", "Test"),
            Summary("2", "AB", "2025-01-01T10:00:00Z", "Test"),
        ).sorted()
        val smallList = getShuffledSummaries(12410, 10).sorted()

        assertThat(smallList[0].id)
            .isEqualTo("93abbea86c95d909")
        assertThat(smallList[1].id)
            .isEqualTo("4a729ee96c15cf8b")
        assertThat(smallList[2].id)
            .isEqualTo("fdd2e81ba0139b3a")
        assertThat(smallList[3].id)
            .isEqualTo("9bea728583463029")
        assertThat(smallList[4].id)
            .isEqualTo("b15ac36df9e404fe")
        assertThat(smallList[5].id)
            .isEqualTo("75ac2c2f0744b14b")
        assertThat(smallList[6].id)
            .isEqualTo("153fa4eebbc37b3c")
        assertThat(smallList[7].id)
            .isEqualTo("3aca813563a16a20")
        assertThat(smallList[8].id)
            .isEqualTo("7d77c89a7bf7e334")
        assertThat(smallList[9].id)
            .isEqualTo("04b79587f97bcddc")

        assertThat(customList[0].id).isEqualTo("1") // 10:00:00 A
        assertThat(customList[1].id).isEqualTo("2") // 10:00:00 AB
        assertThat(customList[2].id).isEqualTo("4") // 10:00:01 AA
        assertThat(customList[3].id).isEqualTo("3") // 10:00:01 B
    }

    @Test
    @AdaptiveTimeout(fast = 1000, slow = 1000)
    @Graded(points = 10, friendlyName = "Test Summary Virtual Filter (Unit)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    fun test1_testSummaryVirtualFilter() {
        val allSummaries = SUMMARIES.toList()

        val nonVirtualFiltered = allSummaries.filterVirtual(false)
        assertThat(nonVirtualFiltered.size)
            .isEqualTo(2651)
        assertThat(nonVirtualFiltered)
            .isNotSameInstanceAs(allSummaries)

        assertThat(nonVirtualFiltered[0].virtual)
            .isFalse()

        // Add your tests here
    }

    @Test
    @AdaptiveTimeout(fast = 1000, slow = 1000)
    @Graded(points = 10, friendlyName = "Test Summary Time Filter (Unit)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    fun test2_testSummaryTimeFilter() {
        val startOfDay = Instant.parse("2025-10-15T05:00:00Z")
        val endOfDay = Instant.parse("2025-10-16T04:59:59.999Z")
        val todayEvents = SUMMARIES.filterTime(startOfDay, endOfDay)
        assertThat(todayEvents.size)
            .isEqualTo(45)
        assertThat(todayEvents)
            .isNotSameInstanceAs(SUMMARIES)

        val futureEvents = SUMMARIES.filterTime(startOfDay, null)
        assertThat(futureEvents.size)
            .isEqualTo(2349)

        val pastEvents = SUMMARIES.filterTime(null, endOfDay)
        assertThat(pastEvents.size)
            .isEqualTo(452)

        // Add your tests here
    }

    @Test
    @AdaptiveTimeout(fast = 1000, slow = 1000)
    @Graded(points = 10, friendlyName = "Test Summary Search Basic (Unit)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    fun test3_testSummarySearchBasic() {
        val allEvents = SUMMARIES.search("")
        assertThat(allEvents.size)
            .isEqualTo(2756)
        assertThat(allEvents)
            .isNotSameInstanceAs(SUMMARIES)

        val exhibitResults = SUMMARIES.search("exhibit")
        assertThat(exhibitResults.size)
            .isEqualTo(209)
        assertThat(exhibitResults.first().id)
            .isEqualTo("9f6535630fbe18ad")
        assertThat(exhibitResults.last().id)
            .isEqualTo("a536590b0211d019")

        // Add your tests here
    }

    @Test
    @AdaptiveTimeout(fast = 1000, slow = 1000)
    @Graded(points = 20, friendlyName = "Test Summary Search Filters (Unit)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    fun test4_testSummarySearchFilters() {
        val unionResults = SUMMARIES.search("location:union")
        assertThat(unionResults.size)
            .isEqualTo(108)

        val virtualResults = SUMMARIES.search("virtual:true")
        assertThat(virtualResults.size)
            .isEqualTo(105)

        val coffeeAtUnion = SUMMARIES.search("coffee location:union")
        assertThat(coffeeAtUnion.size)
            .isEqualTo(0)

        // Add your tests here
    }

    @Test
    @AdaptiveTimeout(fast = 1000, slow = 1000)
    @Graded(points = 10, friendlyName = "Test Main Activity Summary Sort (Integration)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    fun test5_testMainActivitySummarySort() {
        setTimeProvider { Instant.parse("2025-10-15T12:00:00Z") }

        startMainActivity { activity ->
            onView(withId(R.id.recycler_view)).check(countRecyclerView(45))

            // Filter today's events and apply default sort
            val startOfDay = Instant.parse("2025-10-15T05:00:00Z")
            val endOfDay = Instant.parse("2025-10-16T04:59:59.999Z")
            val sortedTodayEvents = SUMMARIES.filterTime(startOfDay, endOfDay).sorted()
            val expectedTitle = sortedTodayEvents[0].title

            // Check that the first event has the correct title
            onView(withRecyclerView(R.id.recycler_view).atPositionOnView(0, R.id.title))
                .check(matches(withText(expectedTitle)))

            // Add your tests here
        }
    }

    @Test
    @AdaptiveTimeout(fast = 1000, slow = 1000)
    @Graded(points = 10, friendlyName = "Test Main Activity Search (Integration)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    fun test6_testMainActivitySearch() {
        setTimeProvider { Instant.parse("2025-10-15T12:00:00Z") }

        startMainActivity { activity ->
            onView(withId(R.id.recycler_view)).check(countRecyclerView(45))

            onView(withId(R.id.todayButton)).perform(click())
            pause()
            onView(withId(R.id.recycler_view)).check(countRecyclerView(2349))

            onView(withId(R.id.search)).perform(searchFor("  "))
            pause()
            onView(withId(R.id.recycler_view)).check(countRecyclerView(2349))

            // Add your tests here
        }
    }

    @Test
    @AdaptiveTimeout(fast = 1000, slow = 1000)
    @Graded(points = 10, friendlyName = "Test Main Activity Filter Buttons (Integration)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    fun test7_testMainActivityFilterButtons() {
        setTimeProvider { Instant.parse("2025-10-15T12:00:00Z") }

        startMainActivity { activity ->
            onView(withId(R.id.todayButton)).check(matches(isDisplayed()))
            onView(withId(R.id.virtualButton)).check(matches(isDisplayed()))

            onView(withId(R.id.recycler_view)).check(countRecyclerView(45))

            onView(withId(R.id.todayButton)).perform(click())
            pause()
            onView(withId(R.id.recycler_view)).check(countRecyclerView(2349))

            // Add your tests here
        }
    }

    companion object {
        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            configureLogging()

            Server.startServer()
        }

        @AfterClass
        @JvmStatic
        fun afterClass() {
            Server.stopServer()
        }
    }

    // Add your tests here
}
