package com.example.braindump

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build

class ReminderReceiver : BroadcastReceiver() {

    companion object {

        private const val CHANNEL_ID =
            "brain_dump_reminders"

        private const val CHANNEL_NAME =
            "Brain Dump Reminders"
    }

    @SuppressLint("MissingPermission")
    override fun onReceive(
        context: Context,
        intent: Intent
    ) {

        val title =
            intent.getStringExtra("title")
                ?: "Brain Dump Reminder"

        val text =
            intent.getStringExtra("text")
                ?: "You have a reminder."

        // Android 13+ notification permission check
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            context.checkSelfPermission(
                android.Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createNotificationChannel(context)

        val openIntent =
            Intent(
                context,
                MainActivity::class.java
            ).apply {
                flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TOP
            }

        val pendingIntent =
            PendingIntent.getActivity(
                context,
                0,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            if (
                Build.VERSION.SDK_INT >=
                Build.VERSION_CODES.O
            ) {

                android.app.Notification.Builder(
                    context,
                    CHANNEL_ID
                )

            } else {

                android.app.Notification.Builder(
                    context
                )
            }
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(
                    "🧠 $title"
                )
                .setContentText(
                    text
                )
                .setStyle(
                    android.app.Notification.BigTextStyle()
                        .bigText(text)
                )
                .setContentIntent(
                    pendingIntent
                )
                .setAutoCancel(true)
                .setPriority(
                    android.app.Notification.PRIORITY_HIGH
                )
                .build()

        val notificationManager =
            context.getSystemService(
                NotificationManager::class.java
            )

        notificationManager.notify(
            System.currentTimeMillis().toInt(),
            notification
        )
    }

    private fun createNotificationChannel(
        context: Context
    ) {

        if (
            Build.VERSION.SDK_INT >=
            Build.VERSION_CODES.O
        ) {

            val channel =
                NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                )

            val manager =
                context.getSystemService(
                    NotificationManager::class.java
                )

            manager.createNotificationChannel(
                channel
            )
        }
    }
}