package com.trainnotify.app

import java.time.Duration
import java.time.Instant
import java.time.OffsetDateTime

object DepartureChecker {
    /** Amtraker's own clock and ours can disagree slightly. */
    private val CLOCK_TOLERANCE: Duration = Duration.ofMinutes(2)

    /** Older departures come from runs that finished hours ago (e.g. yesterday's train), which the feed can still list. */
    private val MAX_AGE: Duration = Duration.ofHours(3)

    /**
     * Returns the runs that have left [stationCode]. A stop counts as left when its own status is
     * "Departed", or when a later stop has been reached, which covers feed gaps where the watched
     * stop is never marked. Either way the departure time at the stop must be in the recent past:
     * a time still in the future means the feed status is wrong, and a time hours ago means a stale run.
     */
    fun departures(runs: List<TrainRun>, stationCode: String, now: Instant = Instant.now()): List<Departure> =
        runs.mapNotNull { run ->
            val index = run.stops.indexOfFirst { it.code.equals(stationCode, ignoreCase = true) }
            if (index < 0) return@mapNotNull null
            val stop = run.stops[index]
            val passedLaterStop = run.stops.drop(index + 1).any { it.status == "Departed" || it.status == "Station" }
            if (stop.status != "Departed" && !passedLaterStop) return@mapNotNull null

            val departedAt = parse(stop.actualDeparture) ?: parse(stop.scheduledDeparture)
            if (departedAt != null) {
                if (departedAt.isAfter(now.plus(CLOCK_TOLERANCE))) return@mapNotNull null
                if (departedAt.isBefore(now.minus(MAX_AGE))) return@mapNotNull null
            }

            Departure(
                key = "${run.trainId}@${stop.code}",
                trainNum = run.trainNum,
                routeName = run.routeName,
                stationName = stop.name,
                actualDeparture = stop.actualDeparture,
            )
        }

    private fun parse(iso: String?): Instant? =
        iso?.let { runCatching { OffsetDateTime.parse(it).toInstant() }.getOrNull() }
}
