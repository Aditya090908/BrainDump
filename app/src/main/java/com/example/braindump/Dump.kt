package com.example.braindump

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity
data class Dump(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val text: String
)