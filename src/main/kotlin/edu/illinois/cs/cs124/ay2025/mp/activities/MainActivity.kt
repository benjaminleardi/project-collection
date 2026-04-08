package edu.illinois.cs.cs124.ay2025.mp.activities

import android.app.Activity
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import android.widget.SearchView
import android.widget.ToggleButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.illinois.cs.cs124.ay2025.mp.R
import edu.illinois.cs.cs124.ay2025.mp.adapters.SummaryListAdapter
import edu.illinois.cs.cs124.ay2025.mp.helpers.getTimeProvider
import edu.illinois.cs.cs124.ay2025.mp.models.Summary
import edu.illinois.cs.cs124.ay2025.mp.models.filterTime
import edu.illinois.cs.cs124.ay2025.mp.models.filterVirtual
import edu.illinois.cs.cs124.ay2025.mp.models.search
import edu.illinois.cs.cs124.ay2025.mp.network.Client
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * STARTUP STEP 4: MainActivity - The main screen of the app
 *
 * MainActivity is the first Activity that launches (defined in AndroidManifest.xml).
 * It displays a list of event summaries fetched from the Server.
 *
 * Android Activity Lifecycle:
 * onCreate() -> onStart() -> onResume() -> [Activity Running] -> onPause() -> onStop() -> onDestroy()
 */
class MainActivity :
    Activity(),
    SearchView.OnQueryTextListener {

    // Stores the raw list of event summaries loaded from the server
    private var summaries: List<Summary> = emptyList()

    // Adapter that connects the data to the RecyclerView
    private lateinit var listAdapter: SummaryListAdapter

    // Track whether to show only today's events (true) or all future events (false)
    // Starts as true because todayButton is initially checked in the layout
    private var showOnlyToday = true

    // Track whether to show only virtual events (false by default)
    // Starts as false because virtualButton is initially unchecked in the layout
    private var showOnlyVirtual = false

    // Track whether to show only starred/favorited events (false by default)
    // Starts as false because starredButton is initially unchecked in the layout
    private var showOnlyStarred = false

    // Track the current search query entered by the user
    private var searchQuery = ""

    // Map to store favorite statuses (eventId -> isFavorite)
    private val favorites: MutableMap<String, Boolean> = mutableMapOf()

    /**
     * STARTUP STEP 4: onCreate - Build and configure the UI
     *
     * Called when the Activity is first created. This is where you:
     * - Inflate the layout from XML
     * - Find views by ID
     * - Set up adapters and listeners
     *
     * After this method completes, the UI is visible but data hasn't been loaded yet.
     */
    override fun onCreate(bundle: Bundle?) {
        super.onCreate(bundle)

        // Made a comment to push again
        // Inflate the layout from activity_main.xml and set it as the content view
        setContentView(R.layout.activity_main)
        title = "Discover Events"

        // Create the adapter with initially empty data and a click handler that launches EventActivity
        listAdapter = SummaryListAdapter(summaries, this) { summary ->
            val intent = Intent(this, EventActivity::class.java)
            intent.putExtra("id", summary.id)
            startActivity(intent)
        }

        // Find the RecyclerView (scrollable list) in the layout and configure it
        val recyclerView: RecyclerView = findViewById(R.id.recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this) // Display items vertically
        recyclerView.adapter = listAdapter // Connect the adapter to the RecyclerView

        // Set up the toolbar (top bar with search and filters)
        setActionBar(findViewById(R.id.toolbar))

        // Set up search view listener
        val searchView: SearchView = findViewById(R.id.search)
        searchView.setOnQueryTextListener(this)

        // Set up filter buttons
        setupFilterButtons()

        // Restore saved filter preferences
        restoreFilterState()

        // Handle system UI insets (screen notches, navigation bars, status bars)
        findViewById<android.view.View>(R.id.container).setOnApplyWindowInsetsListener { v, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsets.Type.systemBars())
            v.setPadding(insets.left, insets.top, insets.right, insets.bottom)
            WindowInsets.CONSUMED
        }
    }

    private fun setupFilterButtons() {
        // Set up calendar button click handler
        val todayButton: ToggleButton = findViewById(R.id.todayButton)
        todayButton.setOnCheckedChangeListener { button, isChecked ->
            // Update button appearance (full opacity when checked, dimmed when unchecked)
            button.alpha = if (isChecked) {
                1.0f
            } else {
                0.3f
            }

            // Update filter state
            // When checked: show only today's events
            // When unchecked: show all future events (from today onward)
            showOnlyToday = isChecked

            // Log the state change for debugging
            val filterState = if (isChecked) {
                "enabled"
            } else {
                "disabled"
            }
            Log.d(TAG, "Today filter $filterState")

            // Reapply filters and update the displayed list
            updateDisplayedSummaries()
        }

        // Set up virtual button click handler
        val virtualButton: ToggleButton = findViewById(R.id.virtualButton)
        virtualButton.setOnCheckedChangeListener { button, isChecked ->
            // Update button appearance (full opacity when checked, dimmed when unchecked)
            button.alpha = if (isChecked) {
                1.0f
            } else {
                0.3f
            }

            // Update filter state
            // When checked: show only virtual events
            // When unchecked: show all events (virtual and in-person)
            showOnlyVirtual = isChecked

            // Log the state change for debugging
            val filterState = if (isChecked) {
                "enabled"
            } else {
                "disabled"
            }
            Log.d(TAG, "Virtual filter $filterState")

            // Reapply filters and update the displayed list
            updateDisplayedSummaries()
        }

        // Set up starred button click handler
        val starredButton: ToggleButton = findViewById(R.id.starredButton)
        starredButton.setOnCheckedChangeListener { button, isChecked ->
            // Update button appearance (full opacity when checked, dimmed when unchecked)
            button.alpha = if (isChecked) {
                1.0f
            } else {
                0.3f
            }

            // Update filter state
            // When checked: show only starred/favorited events
            // When unchecked: show all events
            showOnlyStarred = isChecked

            // Log the state change for debugging
            val filterState = if (isChecked) {
                "enabled"
            } else {
                "disabled"
            }
            Log.d(TAG, "Starred filter $filterState")

            // Reapply filters and update the displayed list
            updateDisplayedSummaries()
        }
    }

    /**
     * Synchronize button checked states and appearance with current filter values.
     * Updates both the checked state and alpha (opacity) for visual consistency.
     */
    private fun syncButtonStates() {
        val todayButton: ToggleButton = findViewById(R.id.todayButton)
        todayButton.isChecked = showOnlyToday
        todayButton.alpha = if (showOnlyToday) {
            1.0f
        } else {
            0.3f
        }

        val virtualButton: ToggleButton = findViewById(R.id.virtualButton)
        virtualButton.isChecked = showOnlyVirtual
        virtualButton.alpha = if (showOnlyVirtual) {
            1.0f
        } else {
            0.3f
        }

        val starredButton: ToggleButton = findViewById(R.id.starredButton)
        starredButton.isChecked = showOnlyStarred
        starredButton.alpha = if (showOnlyStarred) {
            1.0f
        } else {
            0.3f
        }
    }

    /**
     * Restore filter state from SharedPreferences.
     * Called during onCreate to restore user's previous filter preferences.
     */
    private fun restoreFilterState() {
        val preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        // Restore filter state with defaults matching initial values
        showOnlyToday = preferences.getBoolean(KEY_SHOW_ONLY_TODAY, true)
        showOnlyVirtual = preferences.getBoolean(KEY_SHOW_ONLY_VIRTUAL, false)
        showOnlyStarred = preferences.getBoolean(KEY_SHOW_ONLY_STARRED, false)

        // Sync UI button states to match restored preferences
        syncButtonStates()
    }

    /**
     * STARTUP STEP 5: onResume - Load data when the Activity becomes visible
     *
     * Called when the Activity is about to become visible and interactive.
     * This happens after onCreate() during startup, and also when returning from another app.
     *
     * This is a good place to start loading fresh data.
     */
    override fun onResume() {
        super.onResume()
        // If we already have summaries, just reload favorites (for when returning from EventActivity)
        if (summaries.isNotEmpty()) {
            loadFavorites()
        } else {
            // First time loading - fetch summaries and favorites
            loadSummaries()
        }
    }

    /**
     * Called when the activity is about to lose focus.
     * Save current filter state to persist across app restarts.
     */
    override fun onPause() {
        super.onPause()

        // Save filter state to SharedPreferences
        val preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val editor = preferences.edit()

        editor.putBoolean(KEY_SHOW_ONLY_TODAY, showOnlyToday)
        editor.putBoolean(KEY_SHOW_ONLY_VIRTUAL, showOnlyVirtual)
        editor.putBoolean(KEY_SHOW_ONLY_STARRED, showOnlyStarred)

        // Apply changes asynchronously (faster than commit())
        editor.apply()
    }

    /**
     * STARTUP STEP 6: Load event summaries from the Server
     *
     * Makes an asynchronous network request to fetch event data.
     * The callback runs on a background thread when the request completes.
     */
    private fun loadSummaries() {
        // Call the Client to fetch summaries (runs on background thread)
        Client.getSummaries { result ->
            try {
                // Extract the list of summaries from the result
                summaries = result.value

                // Load favorite status for all events
                loadFavorites()

                // Switch back to the main UI thread to update the display
                // (You can only modify UI elements from the main thread)
                runOnUiThread(this::updateDisplayedSummaries)
            } catch (e: Exception) {
                // Log any errors that occur during data loading
                Log.e(TAG, "Error updating summary list", e)
            }
        }
    }

    /**
     * Load favorite status for all event summaries
     *
     * Fetches the favorite status for each event from the server.
     * This allows the starred filter to work correctly.
     */
    private fun loadFavorites() {
        // Track how many favorites have been loaded to update UI only once at the end
        val loadedCount = AtomicInteger(0)
        val totalCount = summaries.size

        // Handle empty case
        if (totalCount == 0) {
            runOnUiThread(this::updateDisplayedSummaries)
            return
        }

        for (summary in summaries) {
            Client.getFavorite(summary.id) { favoriteResult ->
                try {
                    val isFavorite = favoriteResult.value
                    synchronized(favorites) {
                        favorites[summary.id] = isFavorite
                    }

                    // Only update UI after all favorites have been loaded
                    if (loadedCount.incrementAndGet() == totalCount) {
                        runOnUiThread(this::updateDisplayedSummaries)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading favorite for ${summary.id}", e)
                    // Still update UI if this was the last one, even if it failed
                    if (loadedCount.incrementAndGet() == totalCount) {
                        runOnUiThread(this::updateDisplayedSummaries)
                    }
                }
            }
        }
    }

    /**
     * STARTUP STEP 7: Update the UI with loaded data
     *
     * This method runs on the main UI thread and applies filters (time and search),
     * sorts the results, and updates the RecyclerView to display the filtered summaries.
     */
    private fun updateDisplayedSummaries() {
        // Don't update if there's no data loaded yet
        if (summaries.isEmpty()) {
            return
        }

        // Start with the full list of summaries from the server
        var filteredSummaries = summaries

        // STEP 1: Apply time filtering based on the today button state
        // Get the current time using the testable time provider
        val currentTime = getTimeProvider().now()

        // Convert to Chicago timezone (the app's default timezone)
        val currentTimeInChicago = currentTime.atZone(ZoneId.of("America/Chicago"))

        // Get the start of today (midnight) in Chicago timezone
        val startOfToday = currentTimeInChicago.toLocalDate()
            .atStartOfDay(ZoneId.of("America/Chicago"))
            .toInstant()

        if (showOnlyToday) {
            // Show only events happening today (from start of today to start of tomorrow)
            val startOfTomorrow = startOfToday.plus(1, ChronoUnit.DAYS)

            // Filter to events within today's time range
            // We subtract 1 nanosecond from start of tomorrow to get end of today
            filteredSummaries = filteredSummaries.filterTime(
                startOfToday,
                startOfTomorrow.minusNanos(1),
            )
        } else {
            // Show all future events (from start of today onwards, no end limit)
            filteredSummaries = filteredSummaries.filterTime(startOfToday, null)
        }

        // STEP 2: Apply virtual event filtering based on the virtual button state
        if (showOnlyVirtual) {
            // Show only virtual/online events
            filteredSummaries = filteredSummaries.filterVirtual(true)
        }
        // If showOnlyVirtual is false, show all events (no filtering needed)

        // STEP 2.5: Apply starred/favorited event filtering based on the starred button state
        if (showOnlyStarred) {
            // Show only events that are favorited
            filteredSummaries = filteredSummaries.filter { summary ->
                favorites[summary.id] == true
            }
        }
        // If showOnlyStarred is false, show all events (no filtering needed)

        // STEP 3: Apply search filtering using the search extension function
        // The search function handles empty queries and special filters like "location:" and "virtual:"
        filteredSummaries = filteredSummaries.search(searchQuery)

        // STEP 4: Sort the filtered results using the default Summary comparator
        // Summary.compareTo() sorts by start time (ascending), then by title (alphabetically)
        filteredSummaries = filteredSummaries.sorted()

        // STEP 5: Tell the adapter about the filtered and sorted data
        // The adapter will automatically notify the RecyclerView to refresh the display
        listAdapter.setSummaries(filteredSummaries)
    }

    /**
     * Called when the user submits the search query (presses Enter/Search button).
     */
    override fun onQueryTextSubmit(query: String?): Boolean {
        // No additional action needed on submit - filtering happens in onQueryTextChange
        return true
    }

    /**
     * Called when the search query text changes.
     * Updates the search query and reapplies all filters.
     */
    override fun onQueryTextChange(newText: String?): Boolean {
        // Save the current search query (use empty string if null)
        searchQuery = newText ?: ""

        // Reapply filters and update the displayed list
        updateDisplayedSummaries()

        return true
    }

    companion object {
        private const val PREFS_NAME = "EventDiscoveryPrefs"
        private const val KEY_SHOW_ONLY_TODAY = "filter_show_only_today"
        private const val KEY_SHOW_ONLY_VIRTUAL = "filter_show_only_virtual"
        private const val KEY_SHOW_ONLY_STARRED = "filter_show_only_starred"
    }
}

private val TAG = MainActivity::class.java.simpleName
