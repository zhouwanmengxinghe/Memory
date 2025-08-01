package com.example.myapplication1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication1.data.MemoryEvent
import com.example.myapplication1.data.MemoryRepository
import com.example.myapplication1.utils.AudioManager
import com.example.myapplication1.utils.FileUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val repository: MemoryRepository,
    private val audioManager: AudioManager
) : ViewModel() {
    
    val allEvents: Flow<List<MemoryEvent>> = repository.getAllEvents()
    
    private val _uiState = MutableStateFlow(MemoryUiState())
    val uiState: StateFlow<MemoryUiState> = _uiState.asStateFlow()
    
    private val _currentEvent = MutableStateFlow<MemoryEvent?>(null)
    val currentEvent: StateFlow<MemoryEvent?> = _currentEvent.asStateFlow()
    
    fun insertEvent(event: MemoryEvent) {
        viewModelScope.launch {
            repository.insertEvent(event)
        }
    }
    
    fun updateEvent(event: MemoryEvent) {
        viewModelScope.launch {
            repository.updateEvent(event)
        }
    }
    
    fun deleteEvent(event: MemoryEvent) {
        viewModelScope.launch {
            // 删除关联的文件
            event.photoPaths.forEach { photoPath ->
                FileUtils.deleteFile(photoPath)
            }
            FileUtils.deleteFile(event.audioPath)
            repository.deleteEvent(event)
        }
    }
    
    fun getEventById(id: Int) {
        viewModelScope.launch {
            _currentEvent.value = repository.getEventById(id)
        }
    }
    
    fun getRandomEvent() {
        viewModelScope.launch {
            val randomEvent = repository.getRandomEvent()
            _currentEvent.value = randomEvent
            _uiState.value = _uiState.value.copy(showRandomEvent = randomEvent != null)
        }
    }
    
    fun startRecording(outputPath: String) {
        val success = audioManager.startRecording(outputPath)
        _uiState.value = _uiState.value.copy(
            isRecording = success,
            recordingPath = if (success) outputPath else null
        )
    }
    
    fun stopRecording() {
        audioManager.stopRecording()
        _uiState.value = _uiState.value.copy(
            isRecording = false,
            recordingPath = null
        )
    }
    
    suspend fun playAudio(audioPath: String) {
        _uiState.value = _uiState.value.copy(isPlayingAudio = true)
        audioManager.playAudio(audioPath) {
            _uiState.value = _uiState.value.copy(isPlayingAudio = false)
        }
    }
    
    fun stopAudio() {
        audioManager.stopAudio()
        _uiState.value = _uiState.value.copy(isPlayingAudio = false)
    }
    
    fun updateEditingEvent(event: MemoryEvent?) {
        _uiState.value = _uiState.value.copy(editingEvent = event)
    }
    
    fun hideRandomEvent() {
        _uiState.value = _uiState.value.copy(showRandomEvent = false)
    }
    
    fun clearAllData() {
        viewModelScope.launch {
            // 删除所有文件
            allEvents.first().forEach { event ->
                event.photoPaths.forEach { photoPath ->
                    FileUtils.deleteFile(photoPath)
                }
                FileUtils.deleteFile(event.audioPath)
            }
            repository.deleteAllEvents()
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        audioManager.release()
    }
}

data class MemoryUiState(
    val isRecording: Boolean = false,
    val isPlayingAudio: Boolean = false,
    val recordingPath: String? = null,
    val editingEvent: MemoryEvent? = null,
    val showRandomEvent: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)