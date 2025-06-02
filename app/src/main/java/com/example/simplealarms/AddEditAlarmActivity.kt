package com.example.simplealarms

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.simplealarms.databinding.ActivityAddEditAlarmBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AddEditAlarmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddEditAlarmBinding
    private lateinit var alarmViewModel: AlarmViewModel
    private var currentAlarm: Alarm? = null
    private var currentAlarmId: Int = -1
    private lateinit var alarmScheduler: AlarmScheduler

    companion object {
        const val EXTRA_ALARM_ID = "com.example.simplealarms.EXTRA_ALARM_ID"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditAlarmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        alarmScheduler = AlarmScheduler(this)

        val database = AppDatabase.getDatabase(applicationContext)
        val alarmRepository = AlarmRepository(database.alarmDao())
        val viewModelFactory = AlarmViewModelFactory(alarmRepository)
        alarmViewModel = ViewModelProvider(this, viewModelFactory)[AlarmViewModel::class.java]

        currentAlarmId = intent.getIntExtra(EXTRA_ALARM_ID, -1)

        if (currentAlarmId != -1) {
            title = "Edit Alarm"
            binding.buttonDelete.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.IO).launch {
                currentAlarm = alarmViewModel.getAlarmById(currentAlarmId)
                withContext(Dispatchers.Main) {
                    currentAlarm?.let { populateUI(it) }
                }
            }
        } else {
            title = "Add Alarm"
            val calendar = Calendar.getInstance()
            binding.timePicker.hour = calendar.get(Calendar.HOUR_OF_DAY)
            binding.timePicker.minute = calendar.get(Calendar.MINUTE)
            binding.buttonDelete.visibility = View.GONE
        }

        binding.timePicker.setIs24HourView(false)

        binding.buttonSave.setOnClickListener {
            saveAlarm()
        }

        binding.buttonDelete.setOnClickListener {
            deleteAlarm()
        }
    }

    private fun populateUI(alarm: Alarm) {
        binding.timePicker.hour = alarm.hour
        binding.timePicker.minute = alarm.minute
        binding.editTextLabel.setText(alarm.label)
        binding.checkBoxMonday.isChecked = alarm.monday
        binding.checkBoxTuesday.isChecked = alarm.tuesday
        binding.checkBoxWednesday.isChecked = alarm.wednesday
        binding.checkBoxThursday.isChecked = alarm.thursday
        binding.checkBoxFriday.isChecked = alarm.friday
        binding.checkBoxSaturday.isChecked = alarm.saturday
        binding.checkBoxSunday.isChecked = alarm.sunday
    }

    private fun saveAlarm() {
        val hour = binding.timePicker.hour
        val minute = binding.timePicker.minute
        val label = binding.editTextLabel.text.toString().ifEmpty { "Alarm" }

        val monday = binding.checkBoxMonday.isChecked
        val tuesday = binding.checkBoxTuesday.isChecked
        val wednesday = binding.checkBoxWednesday.isChecked
        val thursday = binding.checkBoxThursday.isChecked
        val friday = binding.checkBoxFriday.isChecked
        val saturday = binding.checkBoxSaturday.isChecked
        val sunday = binding.checkBoxSunday.isChecked

        val isRecurring = listOf(monday, tuesday, wednesday, thursday, friday, saturday, sunday).any { it }


        val alarmToSave = currentAlarm?.apply {
            this.hour = hour
            this.minute = minute
            this.label = label
            this.monday = monday
            this.tuesday = tuesday
            this.wednesday = wednesday
            this.thursday = thursday
            this.friday = friday
            this.saturday = saturday
            this.sunday = sunday
            this.isRecurring = isRecurring
        } ?: Alarm(
            hour = hour,
            minute = minute,
            label = label,
            isEnabled = true,
            monday = monday,
            tuesday = tuesday,
            wednesday = wednesday,
            thursday = thursday,
            friday = friday,
            saturday = saturday,
            sunday = sunday,
            isRecurring = isRecurring
        )

        CoroutineScope(Dispatchers.IO).launch {
            val id = if (currentAlarmId != -1) {
                alarmViewModel.updateAlarm(alarmToSave)
                alarmToSave.id
            } else {
                alarmViewModel.insertAlarmAndGetId(alarmToSave).toInt()
            }

            val savedAlarm = alarmViewModel.getAlarmById(id) ?: alarmToSave.copy(id = id)

            withContext(Dispatchers.Main) {
                if (savedAlarm.isEnabled) {
                    alarmScheduler.schedule(savedAlarm)
                } else {
                    alarmScheduler.cancel(savedAlarm)
                }
                Toast.makeText(this@AddEditAlarmActivity, "Alarm saved", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    private fun deleteAlarm() {
        currentAlarm?.let {
            alarmViewModel.deleteAlarm(it)
            alarmScheduler.cancel(it)
            Toast.makeText(this, "Alarm deleted", Toast.LENGTH_SHORT).show()
            setResult(Activity.RESULT_OK)
            finish()
        }
    }
}