package com.trainnotify.app

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant

class DepartureCheckerTest {
    private val now = Instant.parse("2026-10-11T14:45:00Z") // 7:45 am in California

    private fun run(vararg stops: Triple<String, String, String?>) = TrainRun(
        trainId = "723-11",
        trainNum = "723",
        routeName = "Gold Runner",
        stops = stops.map { (code, status, dep) -> Stop(code, code, status, dep, dep) },
    )

    private fun stop(code: String, status: String, dep: String? = null) = Triple(code, status, dep)

    @Test
    fun notDepartedWhileEnroute() {
        val runs = listOf(run(stop("OKJ", "Departed"), stop("EMY", "Enroute"), stop("RIC", "Enroute")))
        assertEquals(emptyList<Departure>(), DepartureChecker.departures(runs, "EMY", now))
    }

    @Test
    fun departedWhenStopMarkedDeparted() {
        val runs = listOf(run(stop("OKJ", "Departed"), stop("EMY", "Departed", "2026-10-11T07:44:00-07:00")))
        assertEquals("723-11@EMY", DepartureChecker.departures(runs, "emy", now).single().key)
    }

    @Test
    fun departedWhenLaterStopReachedButStopNotMarked() {
        val runs = listOf(run(stop("EMY", "Enroute", "2026-10-11T07:40:00-07:00"), stop("RIC", "Station")))
        assertEquals(1, DepartureChecker.departures(runs, "EMY", now).size)
    }

    @Test
    fun ignoresDepartedStatusWithFutureTime() {
        // At 5 am the feed marked EMY departed with the 7:40 estimate: the train hadn't left yet.
        val fiveAm = Instant.parse("2026-10-11T12:00:00Z")
        val runs = listOf(run(stop("OKJ", "Enroute"), stop("EMY", "Departed", "2026-10-11T07:40:00-07:00")))
        assertEquals(0, DepartureChecker.departures(runs, "EMY", fiveAm).size)
    }

    @Test
    fun ignoresYesterdaysRun() {
        val runs = listOf(run(stop("EMY", "Departed", "2026-10-10T07:41:00-07:00"), stop("RIC", "Departed")))
        assertEquals(0, DepartureChecker.departures(runs, "EMY", now).size)
    }

    @Test
    fun ignoresRunsThatDoNotServeStation() {
        assertEquals(0, DepartureChecker.departures(listOf(run(stop("OKJ", "Departed"))), "PHL", now).size)
    }
}
