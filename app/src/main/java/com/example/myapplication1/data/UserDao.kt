package com.example.myapplication1.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
    
    @Query("SELECT * FROM users WHERE isCurrentUser = 1 LIMIT 1")
    fun getCurrentUser(): Flow<User?>
    
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)
    
    @Update
    suspend fun updateUser(user: User)
    
    @Delete
    suspend fun deleteUser(user: User)
    
    @Query("UPDATE users SET isCurrentUser = 0")
    suspend fun clearCurrentUser()
    
    @Query("UPDATE users SET isCurrentUser = 1 WHERE id = :userId")
    suspend fun setCurrentUser(userId: String)
}

@Dao
interface SharedMemoryDao {
    @Query("SELECT * FROM shared_memories WHERE memoryId = :memoryId")
    fun getSharedMemoriesForEvent(memoryId: String): Flow<List<SharedMemory>>
    
    @Query("SELECT * FROM shared_memories WHERE sharedWithUserId = :userId OR sharedByUserId = :userId")
    fun getSharedMemoriesForUser(userId: String): Flow<List<SharedMemory>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedMemory(sharedMemory: SharedMemory)
    
    @Delete
    suspend fun deleteSharedMemory(sharedMemory: SharedMemory)
    
    @Query("DELETE FROM shared_memories WHERE memoryId = :memoryId")
    suspend fun deleteSharedMemoriesForEvent(memoryId: String)
}