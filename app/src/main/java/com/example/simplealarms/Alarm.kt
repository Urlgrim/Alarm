package com.example.simplealarms

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    var hour: Int,
    var minute: Int,
    var isEnabled: Boolean = true,
    var label: String = "Alarm",
    var isRecurring: Boolean = false,
    var monday: Boolean = false,
    var tuesday: Boolean = false,
    var wednesday: Boolean = false,
    var thursday: Boolean = false,
    var friday: Boolean = false,
    var saturday: Boolean = false,
    var sunday: Boolean = false,
    var createdTimestamp: Long = System.currentTimeMillis()
) : Serializable {
    fun getFormattedTime(): String {
        val period = if (hour < 12) "AM" else "PM"
        val displayHour = if (hour == 0 || hour == 12) 12 else hour % 12
        return String.format("%02d:%02d %s", displayHour, minute, period)
    }

    fun getRepeatingDaysString(): String {
        if (!isRecurring && !listOf(monday, tuesday, wednesday, thursday, friday, saturday, sunday).any { it }) {
            return "Once"
        }
        val days = mutableListOf<String>()
        if (monday) days.add("Mon")
        if (tuesday) days.add("Tue")
        if (wednesday) days.add("Wed")
        if (thursday) days.add("Thu")
        if (friday) days.add("Fri")
        if (saturday) days.add("Sat")
        if (sunday) days.add("Sun")

        return if (days.isEmpty()) "Once" else days.joinToString(", ")
    }
}