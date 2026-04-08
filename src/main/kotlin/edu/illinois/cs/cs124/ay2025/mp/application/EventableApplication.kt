package edu.illinois.cs.cs124.ay2025.mp.application

import android.app.Application
import android.os.Build
import edu.illinois.cs.cs124.ay2025.mp.network.Server

const val DEFAULT_SERVER_PORT = 8024

const val SERVER_URL = "http://localhost:$DEFAULT_SERVER_PORT"

private const val SERVER_STARTUP_TIMEOUT_MS = 8000L

/**
 * STARTUP STEP 2: Custom Application class
 *
 * This is the first code that runs when your app launches (after AndroidManifest.xml is read).
 * The Application class is created before any Activity, Service, or other components.
 *
 * Purpose: Start the mock server that will handle API requests from the Client.
 */
class EventableApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Only start the server when running on a real device/emulator (not during testing)
        if (Build.FINGERPRINT != "robolectric") {
            // Create a new thread to run the server (network operations must be off the main thread)
            val serverThread = Thread(Server::startServer)
            serverThread.start()

            try {
                // Wait up to 8 seconds for the server to finish starting up
                // This ensures the server is ready before MainActivity tries to make requests
                serverThread.join(SERVER_STARTUP_TIMEOUT_MS)

                // Verify the server thread has completed (not still running)
                check(!serverThread.isAlive) {
                    "Server failed to start within ${SERVER_STARTUP_TIMEOUT_MS / 1000} seconds"
                }
            } catch (e: InterruptedException) {
                throw IllegalStateException("Server startup interrupted", e)
            }
        }
    }
}
