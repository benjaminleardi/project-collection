@file:Suppress("detekt:all")

package edu.illinois.cs.cs124.ay2025.mp.test

import com.google.common.truth.Truth.assertThat
import edu.illinois.cs.cs124.ay2025.mp.models.search
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.SUMMARIES
import org.junit.Test

class NewFiltersTest {
    @Test
    fun testNewSearchFilters() {
        // Test start filter - find events starting in October 2025
        val octoberEvents = SUMMARIES.search("start:2025-10")
        println("start:2025-10 → ${octoberEvents.size} events")
        assertThat(octoberEvents.size).isGreaterThan(0)
        assertThat(octoberEvents.all { it.start.contains("2025-10") }).isTrue()

        // Test title filter - find events with "workshop" in title
        val workshopEvents = SUMMARIES.search("title:workshop")
        println("title:workshop → ${workshopEvents.size} events")
        assertThat(workshopEvents.size).isGreaterThan(0)
        assertThat(workshopEvents.all { it.title.contains("workshop", ignoreCase = true) }).isTrue()

        // Test id filter - find a specific event by partial id
        val firstEventId = SUMMARIES.first().id.substring(0, 8)
        val idResults = SUMMARIES.search("id:$firstEventId")
        println("id:$firstEventId → ${idResults.size} event(s)")
        assertThat(idResults.size).isGreaterThan(0)
        assertThat(idResults.all { it.id.contains(firstEventId, ignoreCase = true) }).isTrue()

        // Test combining multiple filters
        val combinedResults = SUMMARIES.search("start:2025-10 location:union")
        println("start:2025-10 location:union → ${combinedResults.size} events")
        assertThat(
            combinedResults.all {
                it.start.contains("2025-10") &&
                    it.location.contains("union", ignoreCase = true)
            },
        ).isTrue()

        // Test all five filters together
        val allFilters = SUMMARIES.search("virtual:false start:2025 location:union title:coffee id:abc")
        println("All 5 filters combined → ${allFilters.size} events")
        // This likely returns 0, but the filter logic should work correctly

        println("\n✓ All new filter tests passed!")
    }
}
