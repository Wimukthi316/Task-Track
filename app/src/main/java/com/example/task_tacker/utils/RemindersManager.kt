package com.example.task_tacker.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.task_tacker.models.Reminder
import com.example.task_tacker.models.ReminderPriority
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class ReminderStatistics(
    val totalReminders: Int,
    val upcomingReminders: Int,
    val overdueReminders: Int,
    val completedReminders: Int,
    val highPriorityReminders: Int
)

class RemindersManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("reminders_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    companion object {
        private const val REMINDERS_KEY = "reminders_list"
    }

    fun loadReminders(): MutableList<Reminder> {
        val remindersJson = sharedPreferences.getString(REMINDERS_KEY, "[]")
        val type = object : TypeToken<List<Reminder>>() {}.type
        val remindersList: List<Reminder> = gson.fromJson(remindersJson, type) ?: emptyList()
        return remindersList.toMutableList()
    }

    private fun saveReminders(reminders: List<Reminder>): Boolean {
        return try {
            val remindersJson = gson.toJson(reminders)
            sharedPreferences.edit()
                .putString(REMINDERS_KEY, remindersJson)
                .apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun addReminder(reminder: Reminder): Boolean {
        val reminders = loadReminders()
        reminders.add(reminder)
        return saveReminders(reminders)
    }

    fun updateReminder(updatedReminder: Reminder): Boolean {
        val reminders = loadReminders()
        val index = reminders.indexOfFirst { it.id == updatedReminder.id }
        return if (index != -1) {
            reminders[index] = updatedReminder
            saveReminders(reminders)
        } else {
            false
        }
    }

    fun deleteReminder(reminderId: String): Boolean {
        val reminders = loadReminders()
        val removed = reminders.removeAll { it.id == reminderId }
        return if (removed) {
            saveReminders(reminders)
        } else {
            false
        }
    }

    fun toggleReminderCompletion(reminderId: String): Boolean {
        val reminders = loadReminders()
        val reminder = reminders.find { it.id == reminderId }
        return if (reminder != null) {
            val updatedReminder = reminder.copy(isCompleted = !reminder.isCompleted)
            updateReminder(updatedReminder)
        } else {
            false
        }
    }

    fun clearAllReminders(): Boolean {
        return saveReminders(emptyList())
    }

    fun getReminderStatistics(): ReminderStatistics {
        val reminders = loadReminders()
        val now = System.currentTimeMillis()

        return ReminderStatistics(
            totalReminders = reminders.size,
            upcomingReminders = reminders.count { !it.isCompleted && it.dateTime > now },
            overdueReminders = reminders.count { !it.isCompleted && it.dateTime < now },
            completedReminders = reminders.count { it.isCompleted },
            highPriorityReminders = reminders.count { it.priority == ReminderPriority.HIGH && !it.isCompleted }
        )
    }

    fun getFilteredReminders(filter: ReminderFilter): List<Reminder> {
        val reminders = loadReminders()
        val now = System.currentTimeMillis()

        return when (filter) {
            ReminderFilter.ALL -> reminders.sortedBy { it.dateTime }
            ReminderFilter.UPCOMING -> reminders.filter { !it.isCompleted && it.dateTime > now }
                .sortedBy { it.dateTime }
            ReminderFilter.OVERDUE -> reminders.filter { !it.isCompleted && it.dateTime < now }
                .sortedBy { it.dateTime }
            ReminderFilter.COMPLETED -> reminders.filter { it.isCompleted }
                .sortedByDescending { it.dateTime }
        }
    }

    fun getUpcomingReminders(hours: Int = 24): List<Reminder> {
        val reminders = loadReminders()
        val now = System.currentTimeMillis()
        val futureTime = now + (hours * 60 * 60 * 1000)

        return reminders.filter {
            !it.isCompleted && it.dateTime > now && it.dateTime <= futureTime
        }.sortedBy { it.dateTime }
    }
}