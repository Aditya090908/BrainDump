package com.example.braindump

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update

@Dao
interface DumpDao {

    @Insert
    suspend fun insertDump(dump: Dump): Long

    @Query("SELECT * FROM Dump ORDER BY id DESC")
    suspend fun getAllDumps(): List<Dump>

    @Update
    suspend fun updateDump(dump: Dump)

    @Delete
    suspend fun deleteDump(dump: Dump)
}