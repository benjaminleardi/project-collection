@file:Suppress("detekt:all")

package edu.illinois.cs.cs124.ay2025.mp.test.helpers

import java.io.PrintStream

private val IGNORED_TAGS: List<String> = listOf(
    "LifecycleMonitor",
    "ActivityScenario",
    "AppCompatDelegate",
    "ViewInteraction",
    "Tracing",
    "EventInjectionStrategy",
    "VirtualDeviceManager",
    "AutofillManager",
    "WindowOnBackDispatcher",
    "FileTestStorage",
    "OverlayConfig",
    "Configuration",
    "ViewRootImpl",
    "FeatureFlagsImplExport",
    "DesktopModeFlags",
    "DisplayManager",
)

class FilteringPrintStream : PrintStream(nullOutputStream()) {
    override fun println(line: String) {
        val parts = line.split(": ")
        if (parts.size < 2) {
            kotlin.io.println(line)
            return
        }
        val tagParts = parts[0].split("/")
        if (tagParts.size != 2) {
            kotlin.io.println(line)
            return
        }
        if (IGNORED_TAGS.contains(tagParts[1])) {
            return
        }
        kotlin.io.println(line)
    }
}
