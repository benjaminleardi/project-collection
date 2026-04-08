package edu.illinois.cs.cs124.ay2025.mp.models

import java.time.Instant

open class Summary(
    val id: String,
    val title: String,
    val start: String,
    val location: String,
    val virtual: Boolean = false,
) : Comparable<Summary> {

    constructor(eventData: EventData) : this(
        id = eventData.id,
        title = eventData.title,
        start = eventData.start,
        location = eventData.location,
        virtual = eventData.virtual,
    )

    override fun equals(other: Any?) = when {
        other !is Summary -> false
        else -> id == other.id
    }

    override fun hashCode() = id.hashCode()

    override fun compareTo(other: Summary): Int {
        val startComparison = Instant.parse(start).compareTo(Instant.parse(other.start))
        return if (startComparison != 0) startComparison else title.compareTo(other.title)
    }
}

// Extension function stubs
fun List<Summary>.filterVirtual(virtual: Boolean): List<Summary> = filter { it.virtual == virtual }

fun List<Summary>.filterTime(start: Instant?, end: Instant?): List<Summary> = filter { summary ->
    val summaryTime = Instant.parse(summary.start)
    val afterStart = start == null || summaryTime >= start
    val beforeEnd = end == null || summaryTime <= end
    afterStart && beforeEnd
}

fun List<Summary>.search(query: String): List<Summary> {
    // Trim the query
    val trimmedQuery = query.trim().lowercase().replace(Regex("\\s+"), " ")

    // If empty, return a copy of the list in original order
    if (trimmedQuery.isEmpty()) {
        return sortedBy { it.title }
    }

    // Parse filters using regex
    // Each filter pattern captures the value after the colon
    val locationPattern = Regex("location:(\\S*)")
    val virtualPattern = Regex("virtual:(true|false)")
    val startPattern = Regex("start:(\\S*)")
    val titlePattern = Regex("title:(\\S*)")
    val idPattern = Regex("id:(\\S*)")

    val locationMatch = locationPattern.find(trimmedQuery)
    val virtualMatch = virtualPattern.find(trimmedQuery)
    val startMatch = startPattern.find(trimmedQuery)
    val titleMatch = titlePattern.find(trimmedQuery)
    val idMatch = idPattern.find(trimmedQuery)

    // Extract filter values from regex matches
    val locationFilter = locationMatch?.groupValues?.get(1)
    val virtualFilter = virtualMatch?.groupValues?.get(1)?.toBoolean()
    val startFilter = startMatch?.groupValues?.get(1)
    val titleFilter = titleMatch?.groupValues?.get(1)
    val idFilter = idMatch?.groupValues?.get(1)

    // Remove filters from query to get the text search term
    val textSearch = trimmedQuery
        .replace(locationPattern, "")
        .replace(virtualPattern, "")
        .replace(startPattern, "")
        .replace(titlePattern, "")
        .replace(idPattern, "")
        .trim()

    // Filter the list based on all specified filters
    val filtered = this.filter { summary ->
        // Check location filter (case-insensitive partial match)
        val matchesLocation = locationFilter == null ||
            summary.location.contains(locationFilter, ignoreCase = true)

        // Check virtual filter (exact boolean match)
        val matchesVirtual = virtualFilter == null || summary.virtual == virtualFilter

        // Check start filter (case-insensitive partial match on ISO timestamp)
        val matchesStart = startFilter == null ||
            summary.start.contains(startFilter, ignoreCase = true)

        // Check title filter (case-insensitive partial match)
        val matchesTitle = titleFilter == null ||
            summary.title.contains(titleFilter, ignoreCase = true)

        // Check id filter (case-insensitive partial match)
        val matchesId = idFilter == null ||
            summary.id.contains(idFilter, ignoreCase = true)

        // Check text search (searches in title and location, case-insensitive)
        val matchesText = textSearch.isEmpty() ||
            summary.title.contains(textSearch, ignoreCase = true) ||
            summary.location.contains(textSearch, ignoreCase = true)

        // All conditions must be true (AND logic)
        matchesLocation && matchesVirtual && matchesStart && matchesTitle && matchesId && matchesText
    }

    // Sort by title (case-sensitive)
    return filtered.sortedBy { it.title }
}
