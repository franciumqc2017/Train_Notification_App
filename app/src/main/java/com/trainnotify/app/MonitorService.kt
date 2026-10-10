package com.trainnotify.app

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/** Foreground service that polls Amtraker and notifies when a watched train leaves the station. */
class MonitorService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var loop: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val settings = Settings(this)
        if (intent?.action == ACTION_STOP || !settings.monitoring) {
            settings.monitoring = false
            stopSelf()
            return START_NOT_STICKY
        }

        Notifications.createChannels(this)
        ServiceCompat.startForeground(
            this,
            Notifications.ID_MONITOR,
            Notifications.monitoring(this, describe(settings)),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE,
        )

        if (loop?.isActive != true) {
            loop = scope.launch { pollLoop(settings, baseline = intent?.action == ACTION_START) }
        }
        return START_STICKY
    }

    private suspend fun pollLoop(settings: Settings, baseline: Boolean) {
        // On a fresh start, departures that already happened are recorded silently so only new ones notify.
        var silent = baseline
        while (scope.isActive) {
            try {
                poll(settings, silent)
                silent = false
            } catch (e: Exception) {
                Log.w(TAG, "Poll failed", e)
            }
            delay(settings.pollSeconds * 1000L)
        }
    }

    private fun poll(settings: Settings, silent: Boolean) {
        val station = settings.stationCode
        val seen = settings.seenDepartures.toMutableList()
        for (trainNum in settings.trainNumbers) {
            val runs = AmtrakerClient.fetchRuns(trainNum)
            for (departure in DepartureChecker.departures(runs, station)) {
                if (departure.key in seen) continue
                seen += departure.key
                if (!silent) Notifications.departed(this, departure)
            }
        }
        settings.seenDepartures = seen
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private fun describe(settings: Settings) =
        "Train ${settings.trainNumbers.joinToString(", ")} at ${settings.stationCode}"

    companion object {
        private const val TAG = "MonitorService"
        private const val ACTION_START = "start"
        private const val ACTION_STOP = "stop"

        fun start(context: Context) {
            Settings(context).monitoring = true
            ContextCompat.startForegroundService(
                context, Intent(context, MonitorService::class.java).setAction(ACTION_START)
            )
        }

        fun stop(context: Context) {
            Settings(context).monitoring = false
            context.stopService(Intent(context, MonitorService::class.java))
        }
    }
}
