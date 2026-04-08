@file:Suppress("detekt:all")

package edu.illinois.cs.cs124.ay2025.mp.test

import android.content.Intent
import android.widget.ToggleButton
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.fasterxml.jackson.databind.JsonNode
import com.google.common.truth.Truth.assertThat
import edu.illinois.cs.cs124.ay2025.mp.R
import edu.illinois.cs.cs124.ay2025.mp.activities.EventActivity
import edu.illinois.cs.cs124.ay2025.mp.helpers.setTimeProvider
import edu.illinois.cs.cs124.ay2025.mp.models.Favorite
import edu.illinois.cs.cs124.ay2025.mp.models.Summary
import edu.illinois.cs.cs124.ay2025.mp.network.Client
import edu.illinois.cs.cs124.ay2025.mp.network.Server
import edu.illinois.cs.cs124.ay2025.mp.network.resetServer
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
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.isChecked
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.objectMapper
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.pause
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.setChecked
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.startActivity
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.startMainActivity
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.testClient
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.testServerGet
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.testServerPost
import edu.illinois.cs.cs125.gradlegrader.annotations.Graded
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import org.robolectric.annotation.LooperMode
import org.robolectric.annotation.experimental.LazyApplication
import java.io.IOException
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Random

/**
 * MP3 test suite.
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
 * The MP3 test suite is correct but incomplete. The tests we provide are accurate, but they don't
 * cover all possible cases. As part of MP3, you'll continue learning to identify missing test cases and add them
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
class MP3Test {
    @get:Rule
    val adaptiveTimeout = AdaptiveTimeoutRule()

    @Suppress("SpellCheckingInspection")
    @Test
    @AdaptiveTimeout(fast = 1000, slow = 1000)
    @Graded(points = 20, friendlyName = "Server GET and POST /favorite (Unit)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    @Throws(IOException::class)
    fun test0_ServerGETAndPOSTFavorite() {
        val random = Random(12431)
        val trimmedSummaries = getShuffledSummaries(12431, 128)

        for (summary in trimmedSummaries) {
            val favorite = testServerGet<Favorite>("/favorite/" + summary.id, Favorite::class.java)
            assertThat(favorite!!.id).isEqualTo(summary.id)
            assertThat(favorite.favorite).isFalse()
        }

        val favorites = mutableMapOf<String, Boolean>()

        for (summary in trimmedSummaries.shuffled(random)) {
            val isFavorite = random.nextBoolean()

            val newFavorite = objectMapper.createObjectNode()
            newFavorite.set<JsonNode>("id", objectMapper.convertValue(summary.id, JsonNode::class.java))
            newFavorite.set<JsonNode>("favorite", objectMapper.convertValue(isFavorite, JsonNode::class.java))

            val favorite = testServerPost<Favorite>("/favorite", newFavorite, Favorite::class.java)

            assertThat(favorite!!.id).isEqualTo(summary.id)
            assertThat(favorite.favorite).isEqualTo(isFavorite)
            favorites[summary.id] = isFavorite
        }

        for ((id, expectedFavorite) in favorites) {
            val favorite = testServerGet<Favorite>("/favorite/$id", Favorite::class.java)
            assertThat(favorite!!.id).isEqualTo(id)
            assertThat(favorite.favorite).isEqualTo(expectedFavorite)
        }
    }

    @Test
    @AdaptiveTimeout(fast = 1000, slow = 1000)
    @Graded(points = 10, friendlyName = "Client getFavorite and setFavorite (Integration)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    @Throws(Exception::class)
    fun test1_ClientGetAndSetFavorite() {
        val trimmedSummaries = getShuffledSummaries(12433, 128)
        val firstSummary = trimmedSummaries[0]

        val setResult =
            testClient { callback -> Client.setFavorite(firstSummary.id, true, callback) }

        val getResult =
            testClient { callback -> Client.getFavorite(firstSummary.id, callback) }

        assertThat(setResult).isTrue()
        assertThat(getResult).isTrue()
    }

    private fun favoriteButtonHelper(summary: Summary, currentFavorite: Boolean, nextFavorite: Boolean) {
        val intent = Intent(ApplicationProvider.getApplicationContext(), EventActivity::class.java)
        intent.putExtra("id", summary.id)

        startActivity<EventActivity>(intent) { activity ->
            pause()

            onView(isAssignableFrom(ToggleButton::class.java))
                .check(isChecked(currentFavorite))
                .perform(setChecked(nextFavorite))
                .check(isChecked(nextFavorite))
        }

        // Add your tests here
    }

    @Test
    @AdaptiveTimeout(fast = 3000, slow = 3000)
    @Graded(points = 20, friendlyName = "Favorite Button (Integration)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    fun test2_FavoriteButton() {
        val random = Random(12434)
        val trimmedSummaries = getShuffledSummaries(12434, 4)
        val firstSummary = trimmedSummaries[0]

        val intent = Intent(ApplicationProvider.getApplicationContext(), EventActivity::class.java)
        intent.putExtra("id", firstSummary.id)

        startActivity<EventActivity>(intent) { activity ->
            pause()

            onView(isAssignableFrom(ToggleButton::class.java))
                .check(isChecked(false))
                .perform(setChecked(true))
                .check(isChecked(true))
        }

        // Add your tests here
    }

    @Test
    @AdaptiveTimeout(fast = 1000, slow = 1000)
    @Graded(points = 20, friendlyName = "Main Activity Starred Filter (Integration)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    @Throws(Exception::class)
    fun test3_MainActivityStarredFilter() {
        val trimmedSummaries = getShuffledSummaries(12435, 8)

        val favoriteIds = mutableListOf<String>()
        for (i in 0 until trimmedSummaries.size / 2) {
            val id = trimmedSummaries[i].id
            favoriteIds.add(id)
            val result = testClient { callback -> Client.setFavorite(id, true, callback) }
            assertThat(result).isTrue()
        }

        val expectedToday = getSummaryCountToday()

        val expectedStarredToday =
            SUMMARIES.count { s ->
                try {
                    val eventStart = ZonedDateTime.parse(s.start)
                    val eventDate =
                        eventStart.withZoneSameInstant(ZoneId.systemDefault()).toLocalDate()
                    eventDate == LocalDate.of(2025, 10, 15) &&
                        favoriteIds.contains(s.id)
                } catch (_: Exception) {
                    false
                }
            }

        startMainActivity { activity ->
            onView(withId(R.id.starredButton)).check(matches(isDisplayed()))

            onView(withId(R.id.recycler_view)).check(countRecyclerView(expectedToday))

            onView(withId(R.id.starredButton)).perform(setChecked(true))
            pause()
            onView(withId(R.id.recycler_view)).check(countRecyclerView(expectedStarredToday))

            // Add your tests here
        }
    }

    @Test
    @AdaptiveTimeout(fast = 2000, slow = 2000)
    @Graded(points = 10, friendlyName = "Cross-Activity Favorite Sync (Integration)")
    @LazyApplication(LazyApplication.LazyLoad.ON)
    @Throws(Exception::class)
    fun test4_CrossActivityFavoriteSync() {
        val testDate = LocalDate.of(2025, 10, 15).atStartOfDay(ZoneId.systemDefault())
        val testEvents =
            getShuffledSummaries(12437, 128)
                .filter { s ->
                    try {
                        !ZonedDateTime.parse(s.start).isBefore(testDate)
                    } catch (_: Exception) {
                        false
                    }
                }
                .take(3)

        // Add your tests here
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
            System.setSecurityManager(null)
            Server.stopServer()
        }
    }

    @Before
    fun beforeTest() {
        val summarySubset = getShuffledSummaries(Random().nextInt(), 16)

        for (summary in summarySubset) {
            try {
                val favoriteData = Favorite(summary.id, true)
                testServerPost<Unit>("/favorite", favoriteData, null)
            } catch (_: Exception) {
            }
        }

        try {
            resetServer()
        } catch (_: Exception) {
        }

        JSON_READ_COUNT_SECURITY_MANAGER.checkCount()
    }

    @After
    fun afterTest() {
        JSON_READ_COUNT_SECURITY_MANAGER.checkCount()
    }
}
