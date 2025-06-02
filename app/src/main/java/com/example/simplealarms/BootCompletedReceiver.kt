package com.example.simplealarms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED ||
            intent?.action == "android.intent.action.QUICKBOOT_POWERON") {
            Log.d("BootCompletedReceiver", "Device boot completed, rescheduling alarms.")
            Toast.makeText(context, "Rescheduling alarms...", Toast.LENGTH_SHORT).show()

            val database = AppDatabase.getDatabase(context.applicationContext)
            val repository = AlarmRepository(database.alarmDao())
            val scheduler = AlarmScheduler(context)

            CoroutineScope(Dispatchers.IO).launch {
                val alarms = database.alarmDao().getAllAlarmsBlocking()

                alarms.filter { it.isEnabled }.forEach { alarm ->
                    scheduler.schedule(alarm)
                    Log.d("BootCompletedReceiver", "Rescheduled: ${alarm.label}")
                }
            }
        }
    }
}