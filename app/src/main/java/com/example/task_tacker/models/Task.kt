package com.example.task_tacker.models

import java.text.SimpleDateFormat
import java.util.*

data class Task(
    val id: String = generateId(),
    var title: String,
    var description: String = "",
    var priority: TaskPriority = TaskPriority.MEDIUM,
    var isCompleted: Boolean = false,
    val createdAt: String = getCurrentDateTime(),
    var completedAt: String? = null,
    var dueDate: String? = null
) {
    companion object {
        private fun generateId(): String {
            return "task_${System.currentTimeMillis()}_${(1000..9999).random()}"
        }

        private fun getCurrentDateTime(): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return formatter.format(Date())
        }
    }

    fun getFormattedCreatedDate(): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = formatter.parse(createdAt)
            val displayFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            displayFormatter.format(date ?: Date())
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun getFormattedCreatedTime(): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = formatter.parse(createdAt)
            val displayFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            displayFormatter.format(date ?: Date())
        } catch (e: Exception) {
            "Unknown"
        }
    }
}

enum class TaskPriority(val displayName: String, val color: String) {
    LOW("Low Priority", "#4CAF50"),
    MEDIUM("Medium Priority", "#FF9800"),
    HIGH("High Priority", "#F44336")
}