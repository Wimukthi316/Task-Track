package com.example.task_tacker.models

import java.text.SimpleDateFormat
import java.util.*

data class Lap(
    val lapNumber: Int,
    val lapTime: Long, // Time for this specific lap in milliseconds
    val totalTime: Long, // Total elapsed time when this lap was recorded
    val timestamp: String = getCurrentDateTime()
) {
    companion object {
        private fun getCurrentDateTime(): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return formatter.format(Date())
        }
    }

    fun getFormattedLapTime(): String {
        return formatTime(lapTime)
    }

    fun getFormattedTotalTime(): String {
        return formatTime(totalTime)
    }

    private fun formatTime(timeInMillis: Long): String {
        val hours = timeInMillis / 3600000
        val minutes = (timeInMillis % 3600000) / 60000
        val seconds = (timeInMillis % 60000) / 1000
        val milliseconds = (timeInMillis % 1000) / 10

        return if (hours > 0) {
            String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, milliseconds)
        } else {
            String.format("%02d:%02d.%02d", minutes, seconds, milliseconds)
        }
    }
}