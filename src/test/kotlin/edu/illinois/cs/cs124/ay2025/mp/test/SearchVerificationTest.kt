@file:Suppress("detekt:all")

package edu.illinois.cs.cs124.ay2025.mp.test

import com.google.common.truth.Truth.assertThat
import edu.illinois.cs.cs124.ay2025.mp.models.search
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.SUMMARIES
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Verification test for the search function in Summary.kt
 * This test verifies the exact counts expected by test4_testSummarySearchFilters
 */
@RunWith(JUnit4::class)
class SearchVerificationTest {

    @Test
    fun verifySearchCounts() {
        println("\n=== Search Function Verification ===")
        println("Total SUMMARIES: ${SUMMARIES.size}")

        // Test 1: location:union - should return 108 results
        val unionResults = SUMMARIES.search("location:union")
        println("\nTest 1: location:union")
        println("Expected: 108")
        println("Actual: ${unionResults.size}")
        assertThat(unionResults.size).isEqualTo(108)

        // Sample the results
        if (unionResults.isNotEmpty()) {
            println("Sample results (first 3):")
            unionResults.take(3).forEach { summary ->
                println("  - ID: ${summary.id}, Title: ${summary.title}, Location: ${summary.location}")
            }
        }

        // Test 2: virtual:true - should return 105 results
        val virtualResults = SUMMARIES.search("virtual:true")
        println("\nTest 2: virtual:true")
        println("Expected: 105")
        println("Actual: ${virtualResults.size}")
        assertThat(virtualResults.size).isEqualTo(105)

        // Verify all results are virtual
        val allVirtual = virtualResults.all { it.virtual }
        println("All results virtual: $allVirtual")
        assertThat(allVirtual).isTrue()

        // Sample the results
        if (virtualResults.isNotEmpty()) {
            println("Sample results (first 3):")
            virtualResults.take(3).forEach { summary ->
                println("  - ID: ${summary.id}, Title: ${summary.title}, Virtual: ${summary.virtual}")
            }
        }

        // Test 3: coffee location:union - should return 0 results
        val coffeeAtUnion = SUMMARIES.search("coffee location:union")
        println("\nTest 3: coffee location:union")
        println("Expected: 0")
        println("Actual: ${coffeeAtUnion.size}")
        assertThat(coffeeAtUnion.size).isEqualTo(0)

        // Debug: Check for coffee events separately
        val coffeeResults = SUMMARIES.search("coffee")
        println("\nDebug: Coffee events (any location): ${coffeeResults.size}")
        if (coffeeResults.isNotEmpty()) {
            println("Sample coffee events (first 3):")
            coffeeResults.take(3).forEach { summary ->
                println("  - Title: ${summary.title}, Location: ${summary.location}")
            }

            // Check if any coffee events are at union
            val coffeeAtUnionManual = coffeeResults.filter {
                it.location.contains("union", ignoreCase = true)
            }
            println("Coffee events at union (manual check): ${coffeeAtUnionManual.size}")
            if (coffeeAtUnionManual.isNotEmpty()) {
                println("Warning: Found coffee events at union manually:")
                coffeeAtUnionManual.forEach { summary ->
                    println("  - Title: ${summary.title}, Location: ${summary.location}")
                }
            }
        }

        // Additional edge case tests
        println("\n=== Edge Cases ===")

        // Test with extra whitespace
        val unionWithSpaces = SUMMARIES.search("  location:union  ")
        println("location:union with extra spaces: ${unionWithSpaces.size}")
        assertThat(unionWithSpaces.size).isEqualTo(108)

        // Test case sensitivity of location filter
        val unionUpperCase = SUMMARIES.search("location:UNION")
        println("location:UNION (uppercase): ${unionUpperCase.size}")
        assertThat(unionUpperCase.size).isEqualTo(108)

        // Test virtual:false
        val nonVirtualResults = SUMMARIES.search("virtual:false")
        println("virtual:false: ${nonVirtualResults.size}")
        val allNonVirtual = nonVirtualResults.all { !it.virtual }
        assertThat(allNonVirtual).isTrue()

        // Test combined filter: virtual:true location:union
        val virtualAtUnion = SUMMARIES.search("virtual:true location:union")
        println("virtual:true location:union: ${virtualAtUnion.size}")

        println("\n=== All Tests Passed ===")
    }

    @Test
    fun verifySearchSorting() {
        println("\n=== Search Sorting Verification ===")

        // Verify that search results are sorted by title
        val exhibitResults = SUMMARIES.search("exhibit")
        println("Exhibit search results: ${exhibitResults.size}")

        if (exhibitResults.size > 1) {
            // Check that results are sorted by title
            val isSorted = exhibitResults.zipWithNext().all { (a, b) ->
                a.title <= b.title
            }
            println("Results sorted by title: $isSorted")
            assertThat(isSorted).isTrue()

            // Check first and last as per test3
            println("First result ID: ${exhibitResults.first().id}")
            println("Last result ID: ${exhibitResults.last().id}")
            assertThat(exhibitResults.first().id).isEqualTo("9f6535630fbe18ad")
            assertThat(exhibitResults.last().id).isEqualTo("a536590b0211d019")
        }

        // Verify empty query returns unsorted list
        val emptyQueryResults = SUMMARIES.search("")
        println("\nEmpty query results: ${emptyQueryResults.size}")
        assertThat(emptyQueryResults.size).isEqualTo(SUMMARIES.size)

        println("\n=== Sorting Tests Passed ===")
    }

    @Test
    fun verifyFilterLogic() {
        println("\n=== Filter Logic Verification ===")

        // Test location filter alone
        val unionOnly = SUMMARIES.search("location:union")
        println("location:union only: ${unionOnly.size}")

        // Manually verify location filtering
        val manualLocationFilter = SUMMARIES.filter {
            it.location.contains("union", ignoreCase = true)
        }.sortedBy { it.title }
        println("Manual location filter: ${manualLocationFilter.size}")
        assertThat(unionOnly.size).isEqualTo(manualLocationFilter.size)

        // Test virtual filter alone
        val virtualOnly = SUMMARIES.search("virtual:true")
        println("virtual:true only: ${virtualOnly.size}")

        // Manually verify virtual filtering
        val manualVirtualFilter = SUMMARIES.filter {
            it.virtual
        }.sortedBy { it.title }
        println("Manual virtual filter: ${manualVirtualFilter.size}")
        assertThat(virtualOnly.size).isEqualTo(manualVirtualFilter.size)

        // Test text search alone
        val coffeeOnly = SUMMARIES.search("coffee")
        println("coffee text search: ${coffeeOnly.size}")

        // Manually verify text search
        val manualTextSearch = SUMMARIES.filter {
            it.title.contains("coffee", ignoreCase = true) ||
                it.location.contains("coffee", ignoreCase = true)
        }.sortedBy { it.title }
        println("Manual text search: ${manualTextSearch.size}")
        assertThat(coffeeOnly.size).isEqualTo(manualTextSearch.size)

        // Test combined filters
        val coffeeAndUnion = SUMMARIES.search("coffee location:union")
        println("\ncoffee location:union: ${coffeeAndUnion.size}")

        // Manually verify combined filtering
        val manualCombinedFilter = SUMMARIES.filter {
            val matchesLocation = it.location.contains("union", ignoreCase = true)
            val matchesText = it.title.contains("coffee", ignoreCase = true) ||
                it.location.contains("coffee", ignoreCase = true)
            matchesLocation && matchesText
        }.sortedBy { it.title }
        println("Manual combined filter: ${manualCombinedFilter.size}")
        assertThat(coffeeAndUnion.size).isEqualTo(manualCombinedFilter.size)

        println("\n=== Filter Logic Tests Passed ===")
    }
}
