package com.trainnotify.app

import android.content.Context
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper

/**
 * Plays the phone's alarm sound on the alarm stream, which keeps sounding when the phone is on
 * silent or vibrate. Its loudness follows the alarm volume, not the ringer volume.
 */
object AlarmSound {
    private const val PLAY_MILLIS = 15_000L
    private val handler = Handler(Looper.getMainLooper())
    private var current: Ringtone? = null

    fun play(context: Context) {
        val uri = RingtoneManager.getActualDefaultRingtoneUri(context, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        handler.post {
            current?.stop()
            current = RingtoneManager.getRingtone(context.applicationContext, uri)?.apply {
                audioAttributes = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
                play()
            }
            handler.removeCallbacksAndMessages(null)
            handler.postDelayed({ stop() }, PLAY_MILLIS)
        }
    }

    fun stop() {
        handler.post {
            current?.stop()
            current = null
        }
    }
}
