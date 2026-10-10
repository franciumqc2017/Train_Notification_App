package com.trainnotify.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object Notifications {
    const val CHANNEL_MONITOR = "monitor"
    const val CHANNEL_DEPARTURES = "departures"
    const val ID_MONITOR = 1

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_MONITOR, "Monitoring", NotificationManager.IMPORTANCE_LOW)
        )
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_DEPARTURES, "Train departures", NotificationManager.IMPORTANCE_HIGH)
        )
    }

    fun monitoring(context: Context, text: String) =
        NotificationCompat.Builder(context, CHANNEL_MONITOR)
            .setSmallIcon(R.drawable.ic_train)
            .setContentTitle("Watching trains")
            .setContentText(text)
            .setOngoing(true)
            .setContentIntent(openApp(context))
            .build()

    fun departed(context: Context, departure: Departure) {
        val time = departure.actualDeparture?.let(::formatTime)
        val text = buildString {
            append("Left ${departure.stationName}")
            if (time != null) append(" at $time")
        }
        val notification = NotificationCompat.Builder(context, CHANNEL_DEPARTURES)
            .setSmallIcon(R.drawable.ic_train)
            .setContentTitle("Train ${departure.trainNum} ${departure.routeName}".trim())
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(openApp(context))
            .build()
        context.getSystemService(NotificationManager::class.java)
            .notify(departure.key.hashCode(), notification)
    }

    private fun openApp(context: Context): PendingIntent =
        PendingIntent.getActivity(
            context, 0, Intent(context, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )

    private fun formatTime(iso: String): String =
        runCatching { OffsetDateTime.parse(iso).format(DateTimeFormatter.ofPattern("h:mm a")) }
            .getOrDefault(iso)
}
