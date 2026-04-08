@file:Suppress("detekt:all")

package edu.illinois.cs.cs124.ay2025.mp.test

import com.google.common.truth.Truth.assertThat
import edu.illinois.cs.cs124.ay2025.mp.models.search
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.SUMMARIES
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Edge case tests for the search function
 * These tests verify that the implementation handles various edge cases correctly
 */
@RunWith(JUnit4::class)
class SearchEdgeCasesTest {

    @Test
    fun testMultipleSpacesInQuery() {
        // Multiple spaces should be trimmed
        val result1 = SUMMARIES.search("location:union")
        val result2 = SUMMARIES.search("  location:union  ")
        val result3 = SUMMARIES.search("location:union   ")
        val result4 = SUMMARIES.search("   location:union")

        assertThat(result1.size).isEqualTo(result2.size)
        assertThat(result1.size).isEqualTo(result3.size)
        assertThat(result1.size).isEqualTo(result4.size)
        println("Multiple spaces handling: PASS (all return ${result1.size} results)")
    }

    @Test
    fun testFilterOrderDoesNotMatter() {
        // Order of filters should not matter
        val result1 = SUMMARIES.search("virtual:true location:union")
        val result2 = SUMMARIES.search("location:union virtual:true")

        assertThat(result1.size).isEqualTo(result2.size)
        assertThat(result1.map { it.id }.toSet()).isEqualTo(result2.map { it.id }.toSet())
        println("Filter order independence: PASS (both return ${result1.size} results)")
    }

    @Test
    fun testLocationFilterCaseInsensitive() {
        // Location filter should be case-insensitive
        val result1 = SUMMARIES.search("location:union")
        val result2 = SUMMARIES.search("location:UNION")
        val result3 = SUMMARIES.search("location:Union")
        val result4 = SUMMARIES.search("location:uNiOn")

        assertThat(result1.size).isEqualTo(result2.size)
        assertThat(result1.size).isEqualTo(result3.size)
        assertThat(result1.size).isEqualTo(result4.size)
        println("Location filter case insensitivity: PASS (all return ${result1.size} results)")
    }

    @Test
    fun testTextSearchCaseInsensitive() {
        // Text search should be case-insensitive
        val result1 = SUMMARIES.search("exhibit")
        val result2 = SUMMARIES.search("EXHIBIT")
        val result3 = SUMMARIES.search("Exhibit")
        val result4 = SUMMARIES.search("ExHiBiT")

        assertThat(result1.size).isEqualTo(result2.size)
        assertThat(result1.size).isEqualTo(result3.size)
        assertThat(result1.size).isEqualTo(result4.size)
        println("Text search case insensitivity: PASS (all return ${result1.size} results)")
    }

    @Test
    fun testEmptyQueryReturnsAllEvents() {
        // Empty query should return all events
        val result1 = SUMMARIES.search("")
        val result2 = SUMMARIES.search("   ")
        val result3 = SUMMARIES.search("\t")

        assertThat(result1.size).isEqualTo(SUMMARIES.size)
        assertThat(result2.size).isEqualTo(SUMMARIES.size)
        assertThat(result3.size).isEqualTo(SUMMARIES.size)
        println("Empty query handling: PASS (returns ${result1.size} events)")
    }

    @Test
    fun testPartialLocationMatch() {
        // Location filter should match partial strings
        val unionResults = SUMMARIES.search("location:union")

        // All results should have "union" somewhere in location (case-insensitive)
        val allMatchLocation = unionResults.all {
            it.location.contains("union", ignoreCase = true)
        }
        assertThat(allMatchLocation).isTrue()
        println("Partial location match: PASS (${unionResults.size} events contain 'union')")
    }

    @Test
    fun testTextSearchInTitleAndLocation() {
        // Text search should search both title and location
        val searchTerm = "library"
        val results = SUMMARIES.search(searchTerm)

        // Each result should contain search term in either title or location
        val allMatch = results.all {
            it.title.contains(searchTerm, ignoreCase = true) ||
                it.location.contains(searchTerm, ignoreCase = true)
        }
        assertThat(allMatch).isTrue()
        println("Text search in title and location: PASS (${results.size} events)")
    }

    @Test
    fun testVirtualFilterExactMatch() {
        // Virtual filter should be exact boolean match
        val virtualTrue = SUMMARIES.search("virtual:true")
        val virtualFalse = SUMMARIES.search("virtual:false")

        // All virtual:true results should be virtual
        assertThat(virtualTrue.all { it.virtual }).isTrue()

        // All virtual:false results should not be virtual
        assertThat(virtualFalse.all { !it.virtual }).isTrue()

        // Together they should cover all events
        assertThat(virtualTrue.size + virtualFalse.size).isEqualTo(SUMMARIES.size)
        println("Virtual filter exact match: PASS")
        println("  virtual:true = ${virtualTrue.size}")
        println("  virtual:false = ${virtualFalse.size}")
        println("  total = ${SUMMARIES.size}")
    }

    @Test
    fun testResultsAreSorted() {
        // All search results should be sorted by title, except empty query
        val queries = listOf(
            "exhibit",
            "location:union",
            "virtual:true",
            "coffee",
        )

        queries.forEach { query ->
            val results = SUMMARIES.search(query)
            if (results.size > 1) {
                val isSorted = results.zipWithNext().all { (a, b) ->
                    a.title <= b.title
                }
                if (!isSorted) {
                    throw AssertionError("Results for '$query' not sorted")
                }
                assertThat(isSorted).isTrue()
            }
        }

        // Empty query should return unsorted list
        val emptyResults = SUMMARIES.search("")
        assertThat(emptyResults.size).isEqualTo(SUMMARIES.size)
        // Verify it's not sorted (it should be in original order)
        assertThat(emptyResults).isEqualTo(SUMMARIES.toList())

        println("Results sorting: PASS (non-empty queries return sorted results, empty returns original order)")
    }

    @Test
    fun testFilterWithNoTextSearch() {
        // Filter alone should work (no text search)
        val result = SUMMARIES.search("location:union")

        // Should only filter by location, not require any text match
        val allMatchLocation = result.all {
            it.location.contains("union", ignoreCase = true)
        }
        assertThat(allMatchLocation).isTrue()
        println("Filter without text search: PASS (${result.size} results)")
    }

    @Test
    fun testTextSearchWithFilter() {
        // Combined text and filter search
        val coffeeAtUnion = SUMMARIES.search("coffee location:union")

        // Should match: text="coffee" AND location contains "union"
        val allMatch = coffeeAtUnion.all {
            val matchesLocation = it.location.contains("union", ignoreCase = true)
            val matchesText = it.title.contains("coffee", ignoreCase = true) ||
                it.location.contains("coffee", ignoreCase = true)
            matchesLocation && matchesText
        }
        assertThat(allMatch).isTrue()
        println("Text search with filter: PASS (${coffeeAtUnion.size} results)")
    }

    @Test
    fun testMultipleFilters() {
        // Multiple filters should all be applied (AND logic)
        val result = SUMMARIES.search("virtual:true location:union")

        // Should be both virtual AND at union
        val allMatch = result.all {
            it.virtual && it.location.contains("union", ignoreCase = true)
        }
        assertThat(allMatch).isTrue()
        println("Multiple filters: PASS (${result.size} results)")
    }

    @Test
    fun testInvalidVirtualValue() {
        // Invalid virtual value should be ignored (or handled gracefully)
        // The regex only matches "true" or "false", so "virtual:maybe" should not match
        val result = SUMMARIES.search("virtual:maybe")

        // Should return all events since "virtual:maybe" doesn't match the pattern
        // The text search will look for "virtual:maybe" in title/location
        println("Invalid virtual value: ${result.size} results")
        // This is an edge case - the current implementation will search for "virtual:maybe" as text
    }

    @Test
    fun testNoResultsScenario() {
        // A query that should return no results
        val result = SUMMARIES.search("xyzzyplugh123456789")
        assertThat(result.size).isEqualTo(0)
        println("No results scenario: PASS (0 results for nonsense query)")
    }

    @Test
    fun testSpecialCharactersInSearch() {
        // Search with special characters
        val results = SUMMARIES.search("&")
        // Should search for & in title/location
        val allMatch = results.all {
            it.title.contains("&", ignoreCase = true) ||
                it.location.contains("&", ignoreCase = true)
        }
        assertThat(allMatch).isTrue()
        println("Special characters in search: PASS (${results.size} results for '&')")
    }
}
