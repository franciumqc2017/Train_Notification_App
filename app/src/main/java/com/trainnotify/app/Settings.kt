package com.trainnotify.app

import android.content.Context

/** What the user is watching, plus which departures were already announced. */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    /** Train/station pairs, stored as "5:CHI,7:PHL". */
    var watches: List<Watch>
        get() {
            val stored = prefs.getString(KEY_WATCHES, null) ?: return legacyWatches()
            return stored.split(",").mapNotNull { entry ->
                val parts = entry.split(":")
                if (parts.size == 2 && parts[0].isNotBlank() && parts[1].isNotBlank()) Watch(parts[0], parts[1]) else null
            }
        }
        set(value) = prefs.edit()
            .putString(KEY_WATCHES, value.joinToString(",") { "${it.trainNum.trim()}:${it.stationCode.trim().uppercase()}" })
            .apply()

    /** Settings saved by the first version held several trains and one station. */
    private fun legacyWatches(): List<Watch> {
        val station = prefs.getString(KEY_LEGACY_STATION, "")!!
        if (station.isBlank()) return emptyList()
        return prefs.getString(KEY_LEGACY_TRAINS, "")!!.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            .map { Watch(it, station) }
    }

    var pollSeconds: Int
        get() = prefs.getInt(KEY_POLL, 60)
        set(value) = prefs.edit().putInt(KEY_POLL, value.coerceAtLeast(30)).apply()

    var monitoring: Boolean
        get() = prefs.getBoolean(KEY_MONITORING, false)
        set(value) = prefs.edit().putBoolean(KEY_MONITORING, value).apply()

    /** Departure keys already notified (or already past when monitoring started). */
    var seenDepartures: List<String>
        get() = prefs.getString(KEY_SEEN, "")!!.split("|").filter { it.isNotEmpty() }
        set(value) = prefs.edit().putString(KEY_SEEN, value.takeLast(MAX_SEEN).joinToString("|")).apply()

    private companion object {
        const val KEY_WATCHES = "watches"
        const val KEY_LEGACY_TRAINS = "trains"
        const val KEY_LEGACY_STATION = "station"
        const val KEY_POLL = "poll_seconds"
        const val KEY_MONITORING = "monitoring"
        const val KEY_SEEN = "seen"
        const val MAX_SEEN = 200
    }
}
