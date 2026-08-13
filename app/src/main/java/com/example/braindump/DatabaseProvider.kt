package com.example.braindump

import android.content.Context
import androidx.room3.Room
import androidx.room3.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import androidx.sqlite.driver.AndroidSQLiteDriver

object DatabaseProvider {

    @Volatile
    private var INSTANCE: BrainDumpDatabase? = null

    private val MIGRATION_2_3 =
        object : Migration(2, 3) {

            override suspend fun migrate(
                connection: SQLiteConnection
            ) {
                connection.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS reminders (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        thoughtId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        originalText TEXT NOT NULL,
                        reminderTime INTEGER NOT NULL,
                        completed INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

    fun getDatabase(
        context: Context
    ): BrainDumpDatabase {

        return INSTANCE ?: synchronized(this) {

            val instance =
                Room.databaseBuilder<BrainDumpDatabase>(
                    context.applicationContext,
                    "brain_dump_database"
                )
                    .setDriver(AndroidSQLiteDriver())
                    .addMigrations(
                        MIGRATION_2_3
                    )
                    .build()

            INSTANCE = instance

            instance
        }
    }
}