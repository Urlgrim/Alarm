package com.example.simplealarms

import androidx.lifecycle.*
import kotlinx.coroutines.launch

class AlarmViewModel(private val repository: AlarmRepository) : ViewModel() {

    val allAlarms: LiveData<List<Alarm>> = repository.allAlarms

    fun insertAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.insert(alarm)
    }
    // For getting the ID back
    suspend fun insertAlarmAndGetId(alarm: Alarm): Long {
        return repository.insert(alarm)
    }


    fun updateAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.update(alarm)
    }

    fun deleteAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.delete(alarm)
    }

    suspend fun getAlarmById(id: Int): Alarm? {
        return repository.getAlarmById(id)
    }
}
