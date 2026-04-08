package edu.illinois.cs.cs124.ay2025.mp.activities

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.ToggleButton
import edu.illinois.cs.cs124.ay2025.mp.R
import edu.illinois.cs.cs124.ay2025.mp.network.Client
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

/**
 * EventActivity - Displays full details of a single event.
 *
 * This activity is launched when a user clicks on an event summary in MainActivity.
 * It receives an event ID via intent extra and displays the complete event information.
 */
class EventActivity : Activity() {

    /**
     * Called when the activity is first created.
     *
     * @param savedInstanceState Bundle containing saved state from previous instance
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the layout for this activity
        setContentView(R.layout.activity_event)

        // Get the event ID passed via intent
        val eventId = intent.getStringExtra("id")

        // If no event ID was provided, log error and return
        if (eventId == null) {
            Log.e(TAG, "No event ID provided")
            return
        }

        // Load the favorite button and event details
        loadFavoriteButton(eventId)
        loadEventDetails(eventId)
    }

    private fun loadFavoriteButton(eventId: String) {
        // Fetch the favorite status from the server
        Client.getFavorite(eventId) { favoriteResult ->
            try {
                val isFavorite = favoriteResult.value

                // Update UI on the main thread
                runOnUiThread {
                    val favoriteButton = findViewById<ToggleButton>(R.id.favoriteButton)
                    favoriteButton.isChecked = isFavorite

                    // Set up the favorite button click listener
                    favoriteButton.setOnCheckedChangeListener { _, isChecked ->
                        // Save the favorite status to the server
                        Client.setFavorite(eventId, isChecked) { setResult ->
                            try {
                                val updatedFavorite = setResult.value
                                // Update button state on the main thread
                                runOnUiThread {
                                    favoriteButton.isChecked = updatedFavorite
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "Error setting favorite status", e)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading favorite status", e)
            }
        }
    }

    private fun loadEventDetails(eventId: String) {
        // Fetch the event details from the server
        Client.getEvent(eventId) { result ->
            try {
                val event = result.value

                // Update UI on the main thread
                runOnUiThread {
                    // Set the event title
                    findViewById<TextView>(R.id.eventTitle).text = event.title

                    // Format and set the date/time
                    try {
                        val startTime = ZonedDateTime.parse(event.start)
                        val date = startTime.format(DateTimeFormatter.ofPattern("MMM d"))
                        val time = startTime.format(DateTimeFormatter.ofPattern("h:mm a"))
                        val formattedStart = "$date • $time"
                        findViewById<TextView>(R.id.eventDateTime).text = formattedStart
                    } catch (e: Exception) {
                        Log.e(TAG, "Error parsing start time", e)
                        findViewById<TextView>(R.id.eventDateTime).text = event.start
                    }

                    // Set the location
                    if (event.location.isNotBlank()) {
                        findViewById<TextView>(R.id.eventLocation).text = "Location: ${event.location}"
                    }

                    // Set virtual status
                    if (event.virtual) {
                        findViewById<TextView>(R.id.eventVirtual).text = "Virtual Event"
                    }

                    // Set the description
                    if (event.description.isNotBlank()) {
                        findViewById<TextView>(R.id.eventDescription).text = event.description
                    }

                    // Set categories
                    if (event.categories.isNotEmpty()) {
                        findViewById<TextView>(R.id.eventCategories).text =
                            "Categories: ${event.categories.joinToString(", ")}"
                    }

                    // Set source
                    if (event.source.isNotBlank()) {
                        findViewById<TextView>(R.id.eventSource).text = "Source: ${event.source}"
                    }

                    // Set URL
                    if (event.url.isNotBlank()) {
                        findViewById<TextView>(R.id.eventUrl).text = event.url
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading event details", e)
            }
        }
    }
}

private val TAG = EventActivity::class.java.simpleName
