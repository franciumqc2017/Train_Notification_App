package com.trainnotify.app

import android.content.Context

/** What the user is watching, plus which departures were already announced. */
class Settings(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)

    var trainNumbers: List<String>
        get() = prefs.getString(KEY_TRAINS, "")!!.split(",").map { it.trim() }.filter { it.isNotEmpty() }
        set(value) = prefs.edit().putString(KEY_TRAINS, value.joinToString(",")).apply()

    var stationCode: String
        get() = prefs.getString(KEY_STATION, "")!!
        set(value) = prefs.edit().putString(KEY_STATION, value.trim().uppercase()).apply()

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
        const val KEY_TRAINS = "trains"
        const val KEY_STATION = "station"
        const val KEY_POLL = "poll_seconds"
        const val KEY_MONITORING = "monitoring"
        const val KEY_SEEN = "seen"
        const val MAX_SEEN = 200
    }
}
