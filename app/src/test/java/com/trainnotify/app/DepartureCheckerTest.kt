package com.trainnotify.app

import org.junit.Assert.assertEquals
import org.junit.Test

class DepartureCheckerTest {
    private fun run(vararg statuses: Pair<String, String>) = TrainRun(
        trainId = "5-9",
        trainNum = "5",
        routeName = "California Zephyr",
        stops = statuses.map { (code, status) -> Stop(code, code, status, null, null) },
    )

    @Test
    fun notDepartedWhileEnroute() {
        val runs = listOf(run("CHI" to "Departed", "NPV" to "Enroute", "PCT" to "Enroute"))
        assertEquals(emptyList<Departure>(), DepartureChecker.departures(runs, "NPV"))
    }

    @Test
    fun departedWhenStopMarkedDeparted() {
        val runs = listOf(run("CHI" to "Departed", "NPV" to "Departed", "PCT" to "Enroute"))
        assertEquals("5-9@NPV", DepartureChecker.departures(runs, "npv").single().key)
    }

    @Test
    fun departedWhenLaterStopReachedButStopNotMarked() {
        val runs = listOf(run("CHI" to "Departed", "NPV" to "Enroute", "PCT" to "Station"))
        assertEquals(1, DepartureChecker.departures(runs, "NPV").size)
    }

    @Test
    fun ignoresRunsThatDoNotServeStation() {
        assertEquals(0, DepartureChecker.departures(listOf(run("CHI" to "Departed")), "PHL").size)
    }
}
