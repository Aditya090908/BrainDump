package com.example.braindump

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [
        Dump::class,
        Reminder::class
    ],
    version = 3,
    exportSchema = false
)
abstract class BrainDumpDatabase : RoomDatabase() {

    abstract fun dumpDao(): DumpDao

    abstract fun reminderDao(): ReminderDao
}