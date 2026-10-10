package com.trainnotify.app

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/**
 * Reads live train data from the Amtraker API (https://github.com/piemadd/amtrak), which covers
 * Amtrak, VIA Rail and Brightline. Its terms require a User-Agent naming the project and visible
 * attribution in the app.
 */
object AmtrakerClient {
    private const val BASE_URL = "https://api.amtraker.com/v3"
    private const val USER_AGENT = "TrainNotificationApp/0.1 (https://github.com/franciumqc2017/Train_Notification_App)"

    /** Returns every current run of [trainNum], or an empty list if none is active. */
    fun fetchRuns(trainNum: String): List<TrainRun> {
        val connection = URL("$BASE_URL/trains/$trainNum").openConnection() as HttpURLConnection
        connection.setRequestProperty("User-Agent", USER_AGENT)
        connection.connectTimeout = 15_000
        connection.readTimeout = 15_000
        try {
            if (connection.responseCode != HttpURLConnection.HTTP_OK) {
                throw IllegalStateException("Amtraker returned HTTP ${connection.responseCode}")
            }
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            return parseRuns(body)
        } finally {
            connection.disconnect()
        }
    }

    /** The response is `{ "<trainNum>": [Train, ...] }`, or `[]` when the train isn't running. */
    private fun parseRuns(body: String): List<TrainRun> {
        if (body.trimStart().startsWith("[")) return emptyList()
        val root = JSONObject(body)
        val runs = mutableListOf<TrainRun>()
        for (key in root.keys()) {
            val trains = root.optJSONArray(key) ?: continue
            for (i in 0 until trains.length()) {
                runs += parseTrain(trains.getJSONObject(i))
            }
        }
        return runs
    }

    private fun parseTrain(train: JSONObject): TrainRun {
        val stations = train.optJSONArray("stations") ?: JSONArray()
        val stops = (0 until stations.length()).map { i ->
            val s = stations.getJSONObject(i)
            Stop(
                code = s.optString("code"),
                name = s.optString("name"),
                status = s.optString("status"),
                scheduledDeparture = s.optStringOrNull("schDep"),
                actualDeparture = s.optStringOrNull("dep"),
            )
        }
        return TrainRun(
            trainId = train.optString("trainID"),
            trainNum = train.optString("trainNum"),
            routeName = train.optString("routeName"),
            stops = stops,
        )
    }

    private fun JSONObject.optStringOrNull(name: String): String? =
        if (isNull(name)) null else optString(name).ifBlank { null }
}
