package com.example.simplealarms

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AlarmDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alarm: Alarm): Long

    @Update
    suspend fun update(alarm: Alarm)

    @Delete
    suspend fun delete(alarm: Alarm)

    @Query("SELECT * FROM alarms ORDER BY createdTimestamp DESC")
    fun getAllAlarms(): LiveData<List<Alarm>>

    @Query("SELECT * FROM alarms WHERE id = :alarmId")
    suspend fun getAlarmById(alarmId: Int): Alarm?

    @Query("SELECT * FROM alarms")
    fun getAllAlarmsBlocking(): List<Alarm>
}