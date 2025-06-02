package com.example.simplealarms

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import android.widget.Toast
import java.util.Calendar
import java.util.TimeZone

class AlarmScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: Alarm) {
        if (!alarm.isEnabled) {
            Log.d("AlarmScheduler", "Alarm ${alarm.id} is disabled, not scheduling.")
            return
        }

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.ALARM_ID, alarm.id)
            putExtra(AlarmReceiver.ALARM_LABEL, alarm.label)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, alarm.hour)
            set(Calendar.MINUTE, alarm.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        var nextTriggerTime = getNextTriggerTimeInMillis(alarm, calendar)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTriggerTime,
                        pendingIntent
                    )
                    Log.d("AlarmScheduler", "Scheduled alarm ${alarm.id} (${alarm.label}) for ${formatTime(nextTriggerTime)}")
                    Toast.makeText(context, "Alarm set for ${formatTime(nextTriggerTime)}", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("AlarmScheduler", "Cannot schedule exact alarms. Permission needed.")
                    Toast.makeText(context, "Exact alarm permission needed.", Toast.LENGTH_LONG).show()
                    return
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTriggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        nextTriggerTime,
                        pendingIntent
                    )
                }
                Log.d("AlarmScheduler", "Scheduled alarm ${alarm.id} (${alarm.label}) for ${formatTime(nextTriggerTime)}")
                Toast.makeText(context, "Alarm set for ${formatTime(nextTriggerTime)}", Toast.LENGTH_SHORT).show()
            }
        } catch (e: SecurityException) {
            Log.e("AlarmScheduler", "SecurityException: ${e.message}")
            Toast.makeText(context, "Could not schedule alarm due to security policy.", Toast.LENGTH_LONG).show()
        }
    }

    private fun getNextTriggerTimeInMillis(alarm: Alarm, baseCalendar: Calendar): Long {
        var calendar = baseCalendar.clone() as Calendar

        if (!alarm.isRecurring && !listOf(alarm.monday, alarm.tuesday, alarm.wednesday, alarm.thursday, alarm.friday, alarm.saturday, alarm.sunday).any { it }){
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendar.timeInMillis
        }

        val today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK) // Sunday = 1, ..., Saturday = 7

        for (i in 0..7) {
            val dayToCheck = (today -1 + i) % 7
            calendar = baseCalendar.clone() as Calendar
            calendar.add(Calendar.DAY_OF_YEAR, i)

            val isDaySelected = when (calendar.get(Calendar.DAY_OF_WEEK)) {
                Calendar.MONDAY -> alarm.monday
                Calendar.TUESDAY -> alarm.tuesday
                Calendar.WEDNESDAY -> alarm.wednesday
                Calendar.THURSDAY -> alarm.thursday
                Calendar.FRIDAY -> alarm.friday
                Calendar.SATURDAY -> alarm.saturday
                Calendar.SUNDAY -> alarm.sunday
                else -> false
            }

            if (isDaySelected) {
                if (calendar.timeInMillis > System.currentTimeMillis()) {
                    return calendar.timeInMillis
                }
            }
        }

        calendar = baseCalendar.clone() as Calendar // Reset
        while(true) {
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            val isDaySelected = when (dayOfWeek) {
                Calendar.MONDAY -> alarm.monday
                Calendar.TUESDAY -> alarm.tuesday
                Calendar.WEDNESDAY -> alarm.wednesday
                Calendar.THURSDAY -> alarm.thursday
                Calendar.FRIDAY -> alarm.friday
                Calendar.SATURDAY -> alarm.saturday
                Calendar.SUNDAY -> alarm.sunday
                else -> false
            }
            if (isDaySelected && calendar.timeInMillis > System.currentTimeMillis()) {
                return calendar.timeInMillis
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
    }


    fun cancel(alarm: Alarm) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d("AlarmScheduler", "Cancelled alarm ${alarm.id} (${alarm.label})")
        Toast.makeText(context, "Alarm cancelled: ${alarm.label}", Toast.LENGTH_SHORT).show()
    }

    private fun formatTime(timeInMillis: Long): String {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timeInMillis
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val period = if (hour < 12) "AM" else "PM"
        val displayHour = if (hour == 0 || hour == 12) 12 else hour % 12
        return String.format("%02d:%02d %s on %tA, %<tb %<td", displayHour, minute, period, cal)
    }
}