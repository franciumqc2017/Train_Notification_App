package com.trainnotify.app

object DepartureChecker {
    /**
     * Returns the runs that have left [stationCode]. A stop counts as left when its own status is
     * "Departed", or when a later stop has been reached, which covers feed gaps where the watched
     * stop is never marked.
     */
    fun departures(runs: List<TrainRun>, stationCode: String): List<Departure> =
        runs.mapNotNull { run ->
            val index = run.stops.indexOfFirst { it.code.equals(stationCode, ignoreCase = true) }
            if (index < 0) return@mapNotNull null
            val stop = run.stops[index]
            val passedLaterStop = run.stops.drop(index + 1).any { it.status == "Departed" || it.status == "Station" }
            if (stop.status != "Departed" && !passedLaterStop) return@mapNotNull null
            Departure(
                key = "${run.trainId}@${stop.code}",
                trainNum = run.trainNum,
                routeName = run.routeName,
                stationName = stop.name,
                actualDeparture = stop.actualDeparture,
            )
        }
}
