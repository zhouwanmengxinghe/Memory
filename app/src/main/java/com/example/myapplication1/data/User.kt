package com.example.myapplication1.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val id: String,
    val name: String,
    val avatar: String? = null,
    val isCurrentUser: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "shared_memories")
data class SharedMemory(
    @PrimaryKey
    val id: String,
    val memoryId: String,
    val sharedWithUserId: String,
    val sharedByUserId: String,
    val sharedAt: Long = System.currentTimeMillis(),
    val canEdit: Boolean = true
)

data class UserProfile(
    val user: User,
    val sharedMemoriesCount: Int = 0,
    val totalMemoriesCount: Int = 0
)