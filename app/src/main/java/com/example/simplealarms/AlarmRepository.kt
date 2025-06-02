package com.example.simplealarms

import androidx.lifecycle.LiveData

class AlarmRepository(private val alarmDao: AlarmDao) {
    val allAlarms: LiveData<List<Alarm>> = alarmDao.getAllAlarms()

    suspend fun insert(alarm: Alarm): Long {
        return alarmDao.insert(alarm)
    }

    suspend fun update(alarm: Alarm) {
        alarmDao.update(alarm)
    }

    suspend fun delete(alarm: Alarm) {
        alarmDao.delete(alarm)
    }

    suspend fun getAlarmById(id: Int): Alarm? {
        return alarmDao.getAlarmById(id)
    }
}