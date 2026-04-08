@file:Suppress("detekt:all")

package edu.illinois.cs.cs124.ay2025.mp.test

import com.google.common.truth.Truth.assertThat
import edu.illinois.cs.cs124.ay2025.mp.models.search
import edu.illinois.cs.cs124.ay2025.mp.test.helpers.SUMMARIES
import org.junit.Test

class Test4Verification {
    @Test
    fun verifyTest4Requirements() {
        // Test 1: location:union should return 108 results
        val unionResults = SUMMARIES.search("location:union")
        println("location:union → ${unionResults.size} results (expected 108)")
        assertThat(unionResults.size).isEqualTo(108)

        // Verify sorting by title (case-sensitive)
        val unionTitles = unionResults.map { it.title }
        val sortedTitles = unionTitles.sorted()
        assertThat(unionTitles).isEqualTo(sortedTitles)
        println("✓ Results are sorted by title (case-sensitive)")

        // Test 2: virtual:true should return 105 results
        val virtualResults = SUMMARIES.search("virtual:true")
        println("virtual:true → ${virtualResults.size} results (expected 105)")
        assertThat(virtualResults.size).isEqualTo(105)

        // Verify sorting
        val virtualTitles = virtualResults.map { it.title }
        val sortedVirtualTitles = virtualTitles.sorted()
        assertThat(virtualTitles).isEqualTo(sortedVirtualTitles)
        println("✓ Results are sorted by title (case-sensitive)")

        // Test 3: coffee location:union should return 0 results
        val coffeeAtUnion = SUMMARIES.search("coffee location:union")
        println("coffee location:union → ${coffeeAtUnion.size} results (expected 0)")
        assertThat(coffeeAtUnion.size).isEqualTo(0)

        // Test 4: Empty query returns unmodified order
        val emptyResults = SUMMARIES.search("")
        println("Empty query → ${emptyResults.size} results (expected ${SUMMARIES.size})")
        assertThat(emptyResults.size).isEqualTo(SUMMARIES.size)

        // Verify order is unmodified (same as original)
        val originalIds = SUMMARIES.map { it.id }
        val emptyResultIds = emptyResults.map { it.id }
        assertThat(emptyResultIds).isEqualTo(originalIds)
        println("✓ Empty query returns unmodified order")

        // Test 5: Whitespace-only query is treated as empty
        val spacesResults = SUMMARIES.search("   ")
        println("Spaces-only query → ${spacesResults.size} results (expected ${SUMMARIES.size})")
        assertThat(spacesResults.size).isEqualTo(SUMMARIES.size)
        assertThat(spacesResults.map { it.id }).isEqualTo(SUMMARIES.map { it.id })
        println("✓ Whitespace-only query returns unmodified order")

        println("\nAll test 4 requirements verified! ✓")
    }
}
