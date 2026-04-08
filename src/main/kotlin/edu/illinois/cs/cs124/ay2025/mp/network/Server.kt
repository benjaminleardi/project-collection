package edu.illinois.cs.cs124.ay2025.mp.network

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.JsonNode
import edu.illinois.cs.cs124.ay2025.mp.application.DEFAULT_SERVER_PORT
import edu.illinois.cs.cs124.ay2025.mp.application.SERVER_URL
import edu.illinois.cs.cs124.ay2025.mp.helpers.CHECK_SERVER_RESPONSE
import edu.illinois.cs.cs124.ay2025.mp.helpers.getTimeProvider
import edu.illinois.cs.cs124.ay2025.mp.helpers.objectMapper
import edu.illinois.cs.cs124.ay2025.mp.models.EventData
import edu.illinois.cs.cs124.ay2025.mp.models.Favorite
import edu.illinois.cs.cs124.ay2025.mp.models.Summary
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import java.io.IOException
import java.net.HttpURLConnection
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.logging.Level
import java.util.logging.Logger

/**
 * STARTUP STEP 2 (Server): Mock web server that handles API requests
 *
 * This server is started by EventableApplication.onCreate() in a separate thread.
 * It runs locally on http://localhost:8024 and responds to HTTP requests from the Client.
 *
 * The Server extends Dispatcher, which routes incoming HTTP requests to handler methods.
 */
object Server : Dispatcher() {
    private val logger: Logger = Logger.getLogger(Server::class.java.name)

    // In-memory storage of event summaries loaded from events.json
    private val summaries: MutableList<Summary> = mutableListOf()

    // Map for efficient event lookups by ID (avoids linear search on /event requests)
    private val eventsById: MutableMap<String, EventData> = mutableMapOf()

    // Map to store favorite events (eventId -> favorite status)
    private val favorites: MutableMap<String, Boolean> = mutableMapOf()

    /**
     * Handles GET /summary/ requests from the Client
     *
     * Filters events to only return those from today onward (using the time provider).
     * This ensures users only see current and future events, not past ones.
     *
     * @return MockResponse containing JSON array of filtered Summary objects
     */
    @Throws(JsonProcessingException::class)
    private fun getSummaries(): MockResponse {
        // Get current time from the mockable time provider
        val now = getTimeProvider().now()
        val nowZoned = now.atZone(ZoneId.of("America/Chicago"))
        val startOfToday = nowZoned.toLocalDate().atStartOfDay(ZoneId.of("America/Chicago"))

        // Filter out events that started before today
        val filteredSummaries = summaries
            .filter { summary ->
                try {
                    val eventStart = ZonedDateTime.parse(summary.start)
                    !eventStart.isBefore(startOfToday)
                } catch (_: Exception) {
                    // If date parsing fails, include the event
                    true
                }
            }

        // Serialize filtered summaries to JSON and return
        return makeOKJSONResponse(objectMapper.writeValueAsString(filteredSummaries))
    }

    /**
     * Handles GET /event/{id} requests from the Client
     *
     * Looks up a specific event by its ID and returns the full event details.
     * Uses the eventsById map for O(1) lookup instead of linear search.
     *
     * @param eventId The unique identifier of the event to retrieve
     * @return MockResponse containing JSON of the EventData, or 404 if not found
     */
    @Throws(JsonProcessingException::class)
    private fun getEvent(eventId: String): MockResponse {
        // Look up the event in the map (O(1) operation)
        val event = eventsById[eventId]

        // Return 404 if event doesn't exist
        if (event == null) {
            return httpNotFound
        }

        // Serialize the event to JSON and return
        return makeOKJSONResponse(objectMapper.writeValueAsString(event))
    }

    /**
     * Handles GET /favorite/{id} requests from the Client
     *
     * Retrieves the favorite status for a specific event.
     * Returns 404 if the event ID doesn't exist.
     * Returns false if the event exists but has never been favorited.
     *
     * @param eventId The unique identifier of the event
     * @return MockResponse containing JSON: { "id": "...", "favorite": true/false }
     */
    @Throws(JsonProcessingException::class)
    private fun getFavorite(eventId: String): MockResponse {
        // Check if event exists - return 404 if not found
        if (!eventsById.containsKey(eventId)) {
            return httpNotFound
        }

        // Get favorite status (default to false if never set)
        val isFavorite = favorites[eventId] ?: false
        val favorite = Favorite(eventId, isFavorite)
        return makeOKJSONResponse(objectMapper.writeValueAsString(favorite))
    }

    /**
     * Handles POST /favorite requests from the Client
     *
     * Saves or updates the favorite status for an event.
     * Expects JSON body: { "id": "EventID", "favorite": true/false }
     *
     * Error codes:
     * - 400 Bad Request: Malformed JSON, missing fields, or empty id
     * - 404 Not Found: Event ID doesn't exist
     *
     * @param request The HTTP request containing the favorite data
     * @return MockResponse with 302 redirect to /favorite/{id} on success
     */
    @Throws(JsonProcessingException::class)
    private fun setFavorite(request: RecordedRequest): MockResponse {
        val body = request.body.readUtf8()

        // Parse as JsonNode first to validate required fields are present
        val jsonNode: JsonNode
        try {
            jsonNode = objectMapper.readTree(body)
        } catch (e: Exception) {
            // Malformed JSON
            return httpBadRequest
        }

        // Check that both required fields are present
        if (!jsonNode.has("id") || !jsonNode.has("favorite")) {
            return httpBadRequest
        }

        // Now deserialize to Favorite object
        val favorite: Favorite
        try {
            favorite = objectMapper.treeToValue(jsonNode, Favorite::class.java)
        } catch (e: Exception) {
            return httpBadRequest
        }

        // Check for empty id - return 400
        if (favorite.id.isEmpty()) {
            return httpBadRequest
        }

        // Check if event exists - return 404 if not found
        if (!eventsById.containsKey(favorite.id)) {
            return httpNotFound
        }

        // Save the favorite status
        favorites[favorite.id] = favorite.favorite

        // Return 302 redirect to /favorite/{id}
        return MockResponse()
            .setResponseCode(302)
            .setHeader("Location", "/favorite/${favorite.id}")
    }

    /**
     * Routes incoming HTTP requests to appropriate handler methods
     *
     * Called automatically by MockWebServer for each incoming request.
     * Supports these routes:
     * - GET /           -> Server health check
     * - GET /reset/     -> Reset server state (for testing)
     * - GET /summary/   -> Get filtered event summaries
     * - GET /event/{id} -> Get full details for a specific event
     * - GET /favorite/{id} -> Get favorite status for an event
     * - POST /favorite  -> Save favorite status for an event
     */
    @Suppress("ReturnCount")
    override fun dispatch(request: RecordedRequest): MockResponse {
        // Validate request has required fields
        if (request.path == null || request.method == null) {
            return httpBadRequest
        }

        // Normalize the path (remove trailing slashes, collapse multiple slashes)
        val path = request.path!!.replaceFirst("/*$".toRegex(), "").replace("/+".toRegex(), "/")
        val method = request.method!!.uppercase()

        try {
            // Route to appropriate handler based on path and HTTP method
            return when {
                path.isEmpty() && method == "GET" ->
                    makeOKJSONResponse(CHECK_SERVER_RESPONSE)
                path == "/reset" && method == "GET" -> {
                    favorites.clear()
                    makeOKJSONResponse("200: OK")
                }
                path == "/summary" && method == "GET" -> getSummaries()
                // Handle /event/{id} - extract the event ID from the path
                path.startsWith("/event/") && method == "GET" -> {
                    // Extract event ID from path (everything after "/event/")
                    val eventId = path.removePrefix("/event/")
                    // Return 404 if no ID provided (just "/event")
                    if (eventId.isEmpty()) {
                        httpNotFound
                    } else {
                        getEvent(eventId)
                    }
                }
                // Handle /favorite/{id} - extract the event ID from the path
                path.startsWith("/favorite/") && method == "GET" -> {
                    // Extract event ID from path (everything after "/favorite/")
                    val eventId = path.removePrefix("/favorite/")
                    // Return 404 if no ID provided (just "/favorite")
                    if (eventId.isEmpty()) {
                        httpNotFound
                    } else {
                        getFavorite(eventId)
                    }
                }
                // Handle POST /favorite - save favorite status
                path == "/favorite" && method == "POST" -> setFavorite(request)
                else ->
                    httpNotFound
            }
        } catch (e: Exception) {
            // Log any errors and return 500 Internal Server Error
            logger.log(Level.SEVERE, "Server internal error for path: $path", e)
            return MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
                .setBody("500: Internal Error")
        }
    }

    /**
     * Loads event data from resources/events.json into memory
     *
     * Called once during Server initialization (in the init block).
     * Parses the JSON file and creates Summary objects for each event.
     * Also populates the eventsById map for efficient /event route lookups.
     */
    private fun loadData() {
        // Read the events.json file from the resources directory
        val json = readEventDataFile()

        try {
            // Parse JSON and extract the "events" array
            val root: JsonNode = objectMapper.readTree(json)
            val eventsArray = root.get("events")

            // Convert each JSON event object to EventData, then to Summary
            // Also store full EventData in map for efficient /event lookups
            for (node in eventsArray) {
                val eventData = objectMapper.readValue(node.toString(), EventData::class.java)

                // Store full event data in map for /event route (O(1) lookup by ID)
                eventsById[eventData.id] = eventData

                val summary = Summary(eventData)
                summaries.add(summary)
            }
        } catch (e: JsonProcessingException) {
            logger.log(Level.SEVERE, "Loading data failed", e)
            throw IllegalStateException(e)
        }
    }

    // ///////////////////////////////////////////////////////////////////////////////////////////////
    // YOU SHOULD NOT NEED TO MODIFY THE CODE BELOW
    // ///////////////////////////////////////////////////////////////////////////////////////////////

    private var mockWebServer: MockWebServer? = null

    /**
     * STARTUP STEP 2a: Initialize the Server object
     *
     * This init block runs once when the Server object is first accessed.
     * It loads event data from events.json before the server starts accepting requests.
     */
    init {
        Logger.getLogger(MockWebServer::class.java.name).level = Level.SEVERE
        loadData()
    }

    /**
     * STARTUP STEP 2b: Start the MockWebServer
     *
     * Called by EventableApplication.onCreate() in a separate thread.
     * Starts the server on port 8024 and begins listening for HTTP requests.
     *
     * This method is synchronized to prevent race conditions if called multiple times.
     */
    @Synchronized
    fun startServer() {
        // If server is already running, do nothing
        if (mockWebServer != null && serverIsRunning(false)) {
            return
        }

        try {
            // Close any existing server instance
            mockWebServer?.close()

            // Create new MockWebServer and configure it
            mockWebServer = MockWebServer()
            mockWebServer!!.dispatcher = this // Route requests to this Server object

            // Start listening on port 8024 (http://localhost:8024)
            mockWebServer!!.start(DEFAULT_SERVER_PORT)
        } catch (e: IOException) {
            logger.log(Level.SEVERE, "Startup failed", e)
            throw e
        }

        // Verify the server is responding to requests
        check(serverIsRunning(true)) { "Server should be running" }
    }

    @Synchronized
    fun stopServer() {
        mockWebServer?.close()
        mockWebServer = null
    }
}

private const val RETRY_COUNT = 8

private const val RETRY_DELAY = 512

fun serverIsRunning(wait: Boolean): Boolean = serverIsRunning(wait, RETRY_COUNT, RETRY_DELAY.toLong())

fun serverIsRunning(wait: Boolean, retryCount: Int, retryDelay: Long): Boolean {
    repeat(retryCount) {
        val client = OkHttpClient()
        val request = Request.Builder().url(SERVER_URL).get().build()
        try {
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    check(response.body.string() == CHECK_SERVER_RESPONSE) {
                        "Another server is running on port $DEFAULT_SERVER_PORT"
                    }
                    return true
                }
            }
        } catch (_: IOException) {
            if (!wait) {
                return@repeat
            }
            try {
                Thread.sleep(retryDelay)
            } catch (_: InterruptedException) {
            }
        }
    }
    return false
}

@Suppress("unused")
fun resetServer(): Boolean {
    val client = OkHttpClient()
    val request =
        Request.Builder().url("$SERVER_URL/reset/").get().build()
    client.newCall(request).execute().use { response ->
        return response.isSuccessful
    }
}

private fun makeOKJSONResponse(body: String): MockResponse = MockResponse()
    .setResponseCode(HttpURLConnection.HTTP_OK)
    .setBody(body)
    .setHeader("Content-Type", "application/json; charset=utf-8")

private val httpNotFound = MockResponse()
    .setResponseCode(HttpURLConnection.HTTP_NOT_FOUND)
    .setBody("404: Not Found")

private val httpBadRequest = MockResponse()
    .setResponseCode(HttpURLConnection.HTTP_BAD_REQUEST)
    .setBody("400: Bad Request")

private fun readEventDataFile(): String = Server::class.java.getResource("/events.json")!!.readText()
