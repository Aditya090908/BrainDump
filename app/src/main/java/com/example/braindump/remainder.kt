package com.example.braindump

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "reminders")
data class Reminder(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val thoughtId: Int,

    val title: String,

    val originalText: String,

    val reminderTime: Long,

    val completed: Boolean = false
)