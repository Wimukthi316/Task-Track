package com.example.task_tacker.models

import java.text.SimpleDateFormat
import java.util.*

data class User(
    val username: String,
    val email: String,
    val contactNumber: String,
    val password: String,
    val createdAt: String = getCurrentDateTime(),
    val lastLoginAt: String = getCurrentDateTime(),
    val loginCount: Int = 0
) {
    companion object {
        private fun getCurrentDateTime(): String {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            return formatter.format(Date())
        }
    }

    fun getFormattedJoinDate(): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = formatter.parse(createdAt)
            val displayFormatter = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
            "Member since ${displayFormatter.format(date ?: Date())}"
        } catch (e: Exception) {
            "Member since July 2025"
        }
    }

    fun getFormattedLastLogin(): String {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = formatter.parse(lastLoginAt)
            val displayFormatter = SimpleDateFormat("HH:mm", Locale.getDefault())
            displayFormatter.format(date ?: Date())
        } catch (e: Exception) {
            "Unknown"
        }
    }

    fun getDaysActive(): Int {
        return try {
            val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val createdDate = formatter.parse(createdAt)
            val currentDate = Date()
            val diffInMillis = currentDate.time - (createdDate?.time ?: currentDate.time)
            val diffInDays = (diffInMillis / (1000 * 60 * 60 * 24)).toInt()
            maxOf(1, diffInDays) // At least 1 day
        } catch (e: Exception) {
            1
        }
    }
}