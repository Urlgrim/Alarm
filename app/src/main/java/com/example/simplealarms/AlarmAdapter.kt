package com.example.simplealarms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class AlarmAdapter(
    private val onAlarmClicked: (Alarm) -> Unit,
    private val onAlarmToggled: (Alarm, Boolean) -> Unit
) : ListAdapter<Alarm, AlarmAdapter.AlarmViewHolder>(AlarmDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.list_item_alarm, parent, false)
        return AlarmViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
        val alarm = getItem(position)
        holder.bind(alarm, onAlarmClicked, onAlarmToggled)
    }

    class AlarmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val timeTextView: TextView = itemView.findViewById(R.id.textViewAlarmTime)
        private val labelTextView: TextView = itemView.findViewById(R.id.textViewAlarmLabel)
        private val daysTextView: TextView = itemView.findViewById(R.id.textViewAlarmDays)
        private val enabledSwitch: SwitchCompat = itemView.findViewById(R.id.switchAlarmEnabled)

        fun bind(
            alarm: Alarm,
            onAlarmClicked: (Alarm) -> Unit,
            onAlarmToggled: (Alarm, Boolean) -> Unit
        ) {
            timeTextView.text = alarm.getFormattedTime()
            labelTextView.text = alarm.label
            daysTextView.text = alarm.getRepeatingDaysString()

            enabledSwitch.isChecked = alarm.isEnabled
            enabledSwitch.setOnCheckedChangeListener(null)
            enabledSwitch.setOnCheckedChangeListener { _, isChecked ->
                onAlarmToggled(alarm, isChecked)
            }

            itemView.setOnClickListener {
                onAlarmClicked(alarm)
            }
        }
    }
}

class AlarmDiffCallback : DiffUtil.ItemCallback<Alarm>() {
    override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
        return oldItem == newItem
    }
}