package com.example.myapplication1.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryEventDao {
    @Query("SELECT * FROM memory_events ORDER BY date DESC")
    fun getAllEvents(): Flow<List<MemoryEvent>>

    @Query("SELECT * FROM memory_events WHERE id = :id")
    suspend fun getEventById(id: Int): MemoryEvent?

    @Insert
    suspend fun insertEvent(event: MemoryEvent): Long

    @Update
    suspend fun updateEvent(event: MemoryEvent)

    @Delete
    suspend fun deleteEvent(event: MemoryEvent)

    @Query("DELETE FROM memory_events")
    suspend fun deleteAllEvents()

    @Query("SELECT * FROM memory_events ORDER BY RANDOM() LIMIT 1")
    suspend fun getRandomEvent(): MemoryEvent?
}