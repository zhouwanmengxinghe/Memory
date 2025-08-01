package com.example.myapplication1.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "memory_events")
data class MemoryEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val date: LocalDate,
    val message: String,
    val photoPaths: List<String> = emptyList(), // 支持多张照片
    val audioPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)