package com.example.testproj

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.core.app.NotificationCompat

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        val channelId = "reminder_channel"
        val title = "Daily Reminder"
        val message = "Don't forget to workout and track your calories! 💪🍽️"

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        val channel = NotificationChannel(channelId, "Daily Reminders", NotificationManager.IMPORTANCE_DEFAULT)
        notificationManager.createNotificationChannel(channel)

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_notification)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }
}
