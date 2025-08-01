package com.example.myapplication1.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication1.data.*
import com.example.myapplication1.utils.AudioManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideMemoryDatabase(@ApplicationContext context: Context): MemoryDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            MemoryDatabase::class.java,
            "memory_database"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideMemoryEventDao(database: MemoryDatabase): MemoryEventDao {
        return database.memoryEventDao()
    }

    @Provides
    fun provideUserDao(database: MemoryDatabase): UserDao {
        return database.userDao()
    }

    @Provides
    fun provideSharedMemoryDao(database: MemoryDatabase): SharedMemoryDao {
        return database.sharedMemoryDao()
    }

    @Provides
    fun provideAnniversaryDao(database: MemoryDatabase): AnniversaryDao {
        return database.anniversaryDao()
    }

    @Provides
    @Singleton
    fun provideMemoryRepository(
        memoryEventDao: MemoryEventDao,
        anniversaryDao: AnniversaryDao
    ): MemoryRepository {
        return MemoryRepository(memoryEventDao, anniversaryDao)
    }

    @Provides
    @Singleton
    fun provideAudioManager(@ApplicationContext context: Context): AudioManager {
        return AudioManager(context)
    }
}