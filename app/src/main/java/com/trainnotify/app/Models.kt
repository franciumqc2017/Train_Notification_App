package com.trainnotify.app

/** One run of a train (a train number on a given origin day), as reported by Amtraker. */
data class TrainRun(
    val trainId: String,
    val trainNum: String,
    val routeName: String,
    val stops: List<Stop>,
)

data class Stop(
    val code: String,
    val name: String,
    val status: String,
    val scheduledDeparture: String?,
    val actualDeparture: String?,
)

/** A train run that has left the watched station. */
data class Departure(
    val key: String,
    val trainNum: String,
    val routeName: String,
    val stationName: String,
    val actualDeparture: String?,
)

/** One train number to watch at one station. */
data class Watch(val trainNum: String, val stationCode: String)
