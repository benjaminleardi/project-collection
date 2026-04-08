package edu.illinois.cs.cs124.ay2025.mp.models

data class EventData(
    val id: String,
    val seriesId: String,
    val title: String,
    val start: String,
    val location: String,
    val description: String,
    val categories: List<String>,
    val source: String,
    val url: String,
    val virtual: Boolean,
)

// Type alias for full event details (used by API responses)
typealias Event = EventData
