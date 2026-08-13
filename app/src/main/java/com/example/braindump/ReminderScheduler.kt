package com.example.braindump

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings

object ReminderScheduler {

    private const val ACTION_REMINDER =
        "com.example.braindump.ACTION_REMINDER"

    fun canScheduleExactAlarms(
        context: Context
    ): Boolean {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return true
        }

        val alarmManager =
            context.getSystemService(
                AlarmManager::class.java
            )

        return alarmManager.canScheduleExactAlarms()
    }

    fun openExactAlarmSettings(
        context: Context
    ) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            val intent =
                Intent(
                    Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
                ).apply {

                    data =
                        android.net.Uri.parse(
                            "package:${context.packageName}"
                        )
                }

            context.startActivity(intent)
        }
    }

    fun schedule(
        context: Context,
        reminder: Reminder
    ): Boolean {

        val alarmManager =
            context.getSystemService(
                AlarmManager::class.java
            )

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            !alarmManager.canScheduleExactAlarms()
        ) {
            return false
        }

        val intent =
            Intent(
                context,
                ReminderReceiver::class.java
            ).apply {

                action = ACTION_REMINDER

                putExtra(
                    "reminder_id",
                    reminder.id
                )

                putExtra(
                    "title",
                    reminder.title
                )

                putExtra(
                    "text",
                    reminder.originalText
                )
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                reminder.id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminder.reminderTime,
            pendingIntent
        )

        return true
    }

    fun cancel(
        context: Context,
        reminderId: Int
    ) {

        val alarmManager =
            context.getSystemService(
                AlarmManager::class.java
            )

        val intent =
            Intent(
                context,
                ReminderReceiver::class.java
            ).apply {

                action = ACTION_REMINDER
            }

        val pendingIntent =
            PendingIntent.getBroadcast(
                context,
                reminderId,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        alarmManager.cancel(
            pendingIntent
        )
    }
}