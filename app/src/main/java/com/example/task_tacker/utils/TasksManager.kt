package com.example.task_tacker.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.task_tacker.models.Task
import com.example.task_tacker.models.TaskPriority
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TasksManager(context: Context) {

    companion object {
        private const val PREF_NAME = "TasksPrefs"
        private const val KEY_TASKS = "tasks_list"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // Save tasks to SharedPreferences
    fun saveTasks(tasks: List<Task>): Boolean {
        return try {
            val json = gson.toJson(tasks)
            sharedPreferences.edit().putString(KEY_TASKS, json).apply()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Load tasks from SharedPreferences
    fun loadTasks(): MutableList<Task> {
        return try {
            val json = sharedPreferences.getString(KEY_TASKS, null)
            if (json != null) {
                val type = object : TypeToken<MutableList<Task>>() {}.type
                gson.fromJson(json, type) ?: mutableListOf()
            } else {
                mutableListOf()
            }
        } catch (e: Exception) {
            mutableListOf()
        }
    }

    // Add a new task
    fun addTask(task: Task): Boolean {
        val tasks = loadTasks()
        tasks.add(task)
        return saveTasks(tasks)
    }

    // Update existing task
    fun updateTask(updatedTask: Task): Boolean {
        val tasks = loadTasks()
        val index = tasks.indexOfFirst { it.id == updatedTask.id }
        return if (index != -1) {
            tasks[index] = updatedTask
            saveTasks(tasks)
        } else {
            false
        }
    }

    // Delete task
    fun deleteTask(taskId: String): Boolean {
        val tasks = loadTasks()
        val index = tasks.indexOfFirst { it.id == taskId }
        return if (index != -1) {
            tasks.removeAt(index)
            saveTasks(tasks)
        } else {
            false
        }
    }

    // Mark task as completed/uncompleted
    fun toggleTaskCompletion(taskId: String): Boolean {
        val tasks = loadTasks()
        val task = tasks.find { it.id == taskId }
        return if (task != null) {
            task.isCompleted = !task.isCompleted
            task.completedAt = if (task.isCompleted) {
                java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                    .format(java.util.Date())
            } else {
                null
            }
            saveTasks(tasks)
        } else {
            false
        }
    }

    // Get filtered tasks
    fun getFilteredTasks(filter: TaskFilter): List<Task> {
        val tasks = loadTasks()
        return when (filter) {
            TaskFilter.ALL -> tasks
            TaskFilter.PENDING -> tasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
        }
    }

    // Get tasks by priority
    fun getTasksByPriority(priority: TaskPriority): List<Task> {
        return loadTasks().filter { it.priority == priority }
    }

    // Get task statistics
    fun getTaskStatistics(): TaskStatistics {
        val tasks = loadTasks()
        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.isCompleted }
        val pendingTasks = totalTasks - completedTasks
        val highPriorityTasks = tasks.count { it.priority == TaskPriority.HIGH && !it.isCompleted }

        return TaskStatistics(totalTasks, completedTasks, pendingTasks, highPriorityTasks)
    }

    // Clear all tasks
    fun clearAllTasks(): Boolean {
        return sharedPreferences.edit().remove(KEY_TASKS).commit()
    }
}

enum class TaskFilter {
    ALL, PENDING, COMPLETED
}

data class TaskStatistics(
    val totalTasks: Int,
    val completedTasks: Int,
    val pendingTasks: Int,
    val highPriorityTasks: Int
)