package com.example.myapplication1.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "anniversaries")
data class Anniversary(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val date: LocalDate,
    val description: String = "",
    val isImportant: Boolean = false, // 是否为重要纪念日
    val createdAt: Long = System.currentTimeMillis()
)