package com.example.task_tacker.models

import java.text.SimpleDateFormat
import java.util.*

data class Reminder(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String = "",
    val dateTime: Long, // Timestamp in milliseconds
    val priority: ReminderPriority = ReminderPriority.MEDIUM,
    val isCompleted: Boolean = false,
    val createdAt: String = getCurrentDateTime(),
    val notificationId: Int = id.hashCode()
) {
    companion object {
        private fun getCurrentDateTime(): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return formatter.format(Date())
        }
    }

    fun getFormattedDate(): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return formatter.format(Date(dateTime))
    }

    fun getFormattedTime(): String {
        val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return formatter.format(Date(dateTime))
    }

    fun getFormattedDateTime(): String {
        val formatter = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
        return formatter.format(Date(dateTime))
    }

    fun isOverdue(): Boolean {
        return !isCompleted && dateTime < System.currentTimeMillis()
    }

    fun isUpcoming(): Boolean {
        return !isCompleted && dateTime > System.currentTimeMillis()
    }

    fun getTimeUntilReminder(): String {
        val timeDiff = dateTime - System.currentTimeMillis()

        if (timeDiff <= 0) {
            val overdueDiff = Math.abs(timeDiff)
            return when {
                overdueDiff < 60000 -> "Just now"
                overdueDiff < 3600000 -> "${overdueDiff / 60000}m overdue"
                overdueDiff < 86400000 -> "${overdueDiff / 3600000}h overdue"
                else -> "${overdueDiff / 86400000}d overdue"
            }
        }

        return when {
            timeDiff < 60000 -> "In ${timeDiff / 1000}s"
            timeDiff < 3600000 -> "In ${timeDiff / 60000}m"
            timeDiff < 86400000 -> "In ${timeDiff / 3600000}h"
            timeDiff < 604800000 -> "In ${timeDiff / 86400000}d"
            else -> "In ${timeDiff / 604800000}w"
        }
    }
}

enum class ReminderPriority(val displayName: String) {
    LOW("Low Priority"),
    MEDIUM("Medium Priority"),
    HIGH("High Priority")
}