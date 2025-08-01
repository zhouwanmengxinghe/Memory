package com.example.myapplication1.data

import kotlinx.coroutines.flow.Flow

class MemoryRepository(
    private val memoryEventDao: MemoryEventDao,
    private val anniversaryDao: AnniversaryDao
) {
    
    // 回忆事件相关
    fun getAllEvents(): Flow<List<MemoryEvent>> = memoryEventDao.getAllEvents()
    
    suspend fun getEventById(id: Int): MemoryEvent? = memoryEventDao.getEventById(id)
    
    suspend fun insertEvent(event: MemoryEvent): Long = memoryEventDao.insertEvent(event)
    
    suspend fun updateEvent(event: MemoryEvent) = memoryEventDao.updateEvent(event)
    
    suspend fun deleteEvent(event: MemoryEvent) = memoryEventDao.deleteEvent(event)
    
    suspend fun deleteAllEvents() = memoryEventDao.deleteAllEvents()
    
    suspend fun getRandomEvent(): MemoryEvent? = memoryEventDao.getRandomEvent()
    
    // 纪念日相关
    fun getAllAnniversaries(): Flow<List<Anniversary>> = anniversaryDao.getAllAnniversaries()
    
    suspend fun getAnniversaryById(id: Int): Anniversary? = anniversaryDao.getAnniversaryById(id)
    
    suspend fun insertAnniversary(anniversary: Anniversary) = anniversaryDao.insertAnniversary(anniversary)
    
    suspend fun updateAnniversary(anniversary: Anniversary) = anniversaryDao.updateAnniversary(anniversary)
    
    suspend fun deleteAnniversary(anniversary: Anniversary) = anniversaryDao.deleteAnniversary(anniversary)
    
    suspend fun deleteAnniversaryById(id: Int) = anniversaryDao.deleteAnniversaryById(id)
}