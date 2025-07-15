package com.example.task_tacker.adapters

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.task_tacker.R
import com.example.task_tacker.models.Reminder
import com.example.task_tacker.models.ReminderPriority
import com.google.android.material.button.MaterialButton

class RemindersAdapter(
    private var reminders: MutableList<Reminder>,
    private val onReminderClick: (Reminder) -> Unit,
    private val onReminderEdit: (Reminder) -> Unit,
    private val onReminderChecked: (Reminder) -> Unit
) : RecyclerView.Adapter<RemindersAdapter.ReminderViewHolder>() {

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val reminderCheckbox: CheckBox = itemView.findViewById(R.id.reminderCheckbox)
        val reminderTitle: TextView = itemView.findViewById(R.id.reminderTitle)
        val reminderDescription: TextView = itemView.findViewById(R.id.reminderDescription)
        val reminderDateTime: TextView = itemView.findViewById(R.id.reminderDateTime)
        val reminderTimeUntil: TextView = itemView.findViewById(R.id.reminderTimeUntil)
        val reminderPriority: TextView = itemView.findViewById(R.id.reminderPriority)
        val priorityIndicator: View = itemView.findViewById(R.id.priorityIndicator)
        val editReminderButton: MaterialButton = itemView.findViewById(R.id.editReminderButton)
        val statusIcon: ImageView = itemView.findViewById(R.id.statusIcon)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminders[position]

        // Set reminder details
        holder.reminderTitle.text = reminder.title
        holder.reminderDateTime.text = reminder.getFormattedDateTime()
        holder.reminderTimeUntil.text = reminder.getTimeUntilReminder()
        holder.reminderPriority.text = reminder.priority.displayName

        // Handle description visibility
        if (reminder.description.isNotEmpty()) {
            holder.reminderDescription.text = reminder.description
            holder.reminderDescription.visibility = View.VISIBLE
        } else {
            holder.reminderDescription.visibility = View.GONE
        }

        // Set checkbox state
        holder.reminderCheckbox.isChecked = reminder.isCompleted

        // Set priority indicator color
        val priorityColor = when (reminder.priority) {
            ReminderPriority.HIGH -> Color.parseColor("#F44336")
            ReminderPriority.MEDIUM -> Color.parseColor("#FF9800")
            ReminderPriority.LOW -> Color.parseColor("#4CAF50")
        }
        holder.priorityIndicator.setBackgroundColor(priorityColor)

        // Set status icon and colors based on reminder state
        when {
            reminder.isCompleted -> {
                holder.statusIcon.setImageResource(R.drawable.baseline_check_circle_24)
                holder.statusIcon.setColorFilter(Color.parseColor("#4CAF50"))
                holder.reminderTimeUntil.text = "Completed"
                holder.reminderTimeUntil.setTextColor(Color.parseColor("#4CAF50"))

                // Strike through completed reminders
                holder.reminderTitle.paintFlags = holder.reminderTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                holder.reminderTitle.alpha = 0.6f
                holder.reminderDescription.alpha = 0.5f
            }
            reminder.isOverdue() -> {
                holder.statusIcon.setImageResource(R.drawable.baseline_warning_24)
                holder.statusIcon.setColorFilter(Color.parseColor("#F44336"))
                holder.reminderTimeUntil.setTextColor(Color.parseColor("#F44336"))

                // Remove strike through and reset alpha
                holder.reminderTitle.paintFlags = holder.reminderTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                holder.reminderTitle.alpha = 1.0f
                holder.reminderDescription.alpha = 0.8f
            }
            else -> {
                holder.statusIcon.setImageResource(R.drawable.baseline_schedule_24)
                holder.statusIcon.setColorFilter(Color.parseColor("#2196F3"))
                holder.reminderTimeUntil.setTextColor(Color.parseColor("#2196F3"))

                // Remove strike through and reset alpha
                holder.reminderTitle.paintFlags = holder.reminderTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                holder.reminderTitle.alpha = 1.0f
                holder.reminderDescription.alpha = 0.8f
            }
        }

        // Set click listeners
        holder.itemView.setOnClickListener {
            onReminderClick(reminder)
        }

        holder.editReminderButton.setOnClickListener {
            onReminderEdit(reminder)
        }

        holder.reminderCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != reminder.isCompleted) {
                onReminderChecked(reminder)
            }
        }
    }

    override fun getItemCount(): Int = reminders.size

    fun updateReminders(newReminders: List<Reminder>) {
        reminders.clear()
        reminders.addAll(newReminders)
        notifyDataSetChanged()
    }
}