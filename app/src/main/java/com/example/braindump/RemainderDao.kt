package com.example.braindump

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update

@Dao
interface ReminderDao {

    @Insert
    suspend fun insertReminder(reminder: Reminder): Long

    @Query("SELECT * FROM reminders ORDER BY reminderTime ASC")
    suspend fun getAllReminders(): List<Reminder>

    @Query("SELECT * FROM reminders WHERE id = :id LIMIT 1")
    suspend fun getReminder(id: Int): Reminder?

    @Query(
        "SELECT * FROM reminders " +
                "WHERE completed = 0 " +
                "ORDER BY reminderTime ASC"
    )
    suspend fun getPendingReminders(): List<Reminder>

    @Update
    suspend fun updateReminder(reminder: Reminder)

    @Delete
    suspend fun deleteReminder(reminder: Reminder)

    @Query(
        "UPDATE reminders SET completed = 1 WHERE id = :id"
    )
    suspend fun completeReminder(id: Int)
}