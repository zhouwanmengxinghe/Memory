package com.example.myapplication1.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication1.data.Anniversary
import com.example.myapplication1.data.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class AnniversaryViewModel @Inject constructor(
    private val repository: MemoryRepository
) : ViewModel() {
    
    val allAnniversaries: Flow<List<Anniversary>> = repository.getAllAnniversaries()
    
    private val _uiState = MutableStateFlow(AnniversaryUiState())
    val uiState: StateFlow<AnniversaryUiState> = _uiState.asStateFlow()
    
    private val _currentAnniversary = MutableStateFlow<Anniversary?>(null)
    val currentAnniversary: StateFlow<Anniversary?> = _currentAnniversary.asStateFlow()
    
    fun insertAnniversary(anniversary: Anniversary) {
        viewModelScope.launch {
            repository.insertAnniversary(anniversary)
        }
    }
    
    fun updateAnniversary(anniversary: Anniversary) {
        viewModelScope.launch {
            repository.updateAnniversary(anniversary)
        }
    }
    
    fun deleteAnniversary(anniversary: Anniversary) {
        viewModelScope.launch {
            repository.deleteAnniversary(anniversary)
        }
    }
    
    fun getAnniversaryById(id: Int) {
        viewModelScope.launch {
            _currentAnniversary.value = repository.getAnniversaryById(id)
        }
    }
    
    fun updateEditingAnniversary(anniversary: Anniversary?) {
        _uiState.value = _uiState.value.copy(editingAnniversary = anniversary)
    }
    
    fun calculateDaysSince(date: LocalDate): Long {
        return ChronoUnit.DAYS.between(date, LocalDate.now())
    }
    
    fun calculateDaysUntil(date: LocalDate): Long {
        return ChronoUnit.DAYS.between(LocalDate.now(), date)
    }
}

data class AnniversaryUiState(
    val editingAnniversary: Anniversary? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)