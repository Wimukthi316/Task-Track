package com.example.task_tacker.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.task_tacker.models.User
import java.text.SimpleDateFormat
import java.util.*

class UserPreferencesManager(context: Context) {

    companion object {
        private const val PREF_NAME = "TaskTrackerPrefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_CURRENT_USER_EMAIL = "current_user_email"
        private const val KEY_USERNAME_PREFIX = "user_username_"
        private const val KEY_EMAIL_PREFIX = "user_email_"
        private const val KEY_CONTACT_PREFIX = "user_contact_"
        private const val KEY_PASSWORD_PREFIX = "user_password_"
        private const val KEY_CREATED_AT_PREFIX = "user_created_at_"
        private const val KEY_LAST_LOGIN_PREFIX = "user_last_login_"
        private const val KEY_LOGIN_COUNT_PREFIX = "user_login_count_"
        private const val KEY_USER_COUNT = "user_count"
        private const val KEY_REGISTERED_EMAILS = "registered_emails"
    }

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    // Save user data during registration with timestamp
    fun registerUser(user: User): Boolean {
        if (isEmailRegistered(user.email)) {
            return false
        }

        val currentDateTime = getCurrentDateTime()
        val editor = sharedPreferences.edit()

        editor.putString(KEY_USERNAME_PREFIX + user.email, user.username)
        editor.putString(KEY_EMAIL_PREFIX + user.email, user.email)
        editor.putString(KEY_CONTACT_PREFIX + user.email, user.contactNumber)
        editor.putString(KEY_PASSWORD_PREFIX + user.email, user.password)
        editor.putString(KEY_CREATED_AT_PREFIX + user.email, currentDateTime)
        editor.putString(KEY_LAST_LOGIN_PREFIX + user.email, currentDateTime)
        editor.putInt(KEY_LOGIN_COUNT_PREFIX + user.email, 0)

        val registeredEmails = getRegisteredEmails().toMutableSet()
        registeredEmails.add(user.email)
        editor.putStringSet(KEY_REGISTERED_EMAILS, registeredEmails)

        val currentCount = sharedPreferences.getInt(KEY_USER_COUNT, 0)
        editor.putInt(KEY_USER_COUNT, currentCount + 1)

        return editor.commit()
    }

    // Authenticate user and update login information
    fun authenticateUser(email: String, password: String): Boolean {
        if (!isEmailRegistered(email)) {
            return false
        }

        val storedPassword = sharedPreferences.getString(KEY_PASSWORD_PREFIX + email, null)
        return storedPassword == password
    }

    // Login user and update last login time
    fun loginUser(email: String): Boolean {
        if (isEmailRegistered(email)) {
            val currentDateTime = getCurrentDateTime()
            val currentLoginCount = sharedPreferences.getInt(KEY_LOGIN_COUNT_PREFIX + email, 0)

            val editor = sharedPreferences.edit()
            editor.putBoolean(KEY_IS_LOGGED_IN, true)
            editor.putString(KEY_CURRENT_USER_EMAIL, email)
            editor.putString(KEY_LAST_LOGIN_PREFIX + email, currentDateTime)
            editor.putInt(KEY_LOGIN_COUNT_PREFIX + email, currentLoginCount + 1)

            return editor.commit()
        }
        return false
    }

    // Logout user
    fun logoutUser(): Boolean {
        val editor = sharedPreferences.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, false)
        editor.remove(KEY_CURRENT_USER_EMAIL)
        return editor.commit()
    }

    // Check if user is currently logged in
    fun isUserLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    // Get current logged in user with all profile data
    fun getCurrentUser(): User? {
        if (!isUserLoggedIn()) {
            return null
        }

        val email = sharedPreferences.getString(KEY_CURRENT_USER_EMAIL, null) ?: return null
        return getUserByEmail(email)
    }

    // Get user by email with complete profile data
    fun getUserByEmail(email: String): User? {
        if (!isEmailRegistered(email)) {
            return null
        }

        val username = sharedPreferences.getString(KEY_USERNAME_PREFIX + email, null) ?: return null
        val contact = sharedPreferences.getString(KEY_CONTACT_PREFIX + email, null) ?: return null
        val password = sharedPreferences.getString(KEY_PASSWORD_PREFIX + email, null) ?: return null
        val createdAt = sharedPreferences.getString(KEY_CREATED_AT_PREFIX + email, getCurrentDateTime()) ?: getCurrentDateTime()
        val lastLoginAt = sharedPreferences.getString(KEY_LAST_LOGIN_PREFIX + email, getCurrentDateTime()) ?: getCurrentDateTime()
        val loginCount = sharedPreferences.getInt(KEY_LOGIN_COUNT_PREFIX + email, 0)

        return User(username, email, contact, password, createdAt, lastLoginAt, loginCount)
    }

    // Update user profile information including email
    fun updateUserProfile(oldEmail: String, newUsername: String, newEmail: String, newContact: String): Boolean {
        if (!isEmailRegistered(oldEmail)) {
            return false
        }

        // If email is changing, check if new email is already taken
        if (oldEmail != newEmail && isEmailRegistered(newEmail)) {
            return false
        }

        val editor = sharedPreferences.edit()

        if (oldEmail != newEmail) {
            // Move all user data to new email key
            val username = sharedPreferences.getString(KEY_USERNAME_PREFIX + oldEmail, null)
            val contact = sharedPreferences.getString(KEY_CONTACT_PREFIX + oldEmail, null)
            val password = sharedPreferences.getString(KEY_PASSWORD_PREFIX + oldEmail, null)
            val createdAt = sharedPreferences.getString(KEY_CREATED_AT_PREFIX + oldEmail, null)
            val lastLoginAt = sharedPreferences.getString(KEY_LAST_LOGIN_PREFIX + oldEmail, null)
            val loginCount = sharedPreferences.getInt(KEY_LOGIN_COUNT_PREFIX + oldEmail, 0)

            // Save data with new email key
            editor.putString(KEY_USERNAME_PREFIX + newEmail, newUsername)
            editor.putString(KEY_EMAIL_PREFIX + newEmail, newEmail)
            editor.putString(KEY_CONTACT_PREFIX + newEmail, newContact)
            editor.putString(KEY_PASSWORD_PREFIX + newEmail, password)
            editor.putString(KEY_CREATED_AT_PREFIX + newEmail, createdAt)
            editor.putString(KEY_LAST_LOGIN_PREFIX + newEmail, lastLoginAt)
            editor.putInt(KEY_LOGIN_COUNT_PREFIX + newEmail, loginCount)

            // Remove old email data
            editor.remove(KEY_USERNAME_PREFIX + oldEmail)
            editor.remove(KEY_EMAIL_PREFIX + oldEmail)
            editor.remove(KEY_CONTACT_PREFIX + oldEmail)
            editor.remove(KEY_PASSWORD_PREFIX + oldEmail)
            editor.remove(KEY_CREATED_AT_PREFIX + oldEmail)
            editor.remove(KEY_LAST_LOGIN_PREFIX + oldEmail)
            editor.remove(KEY_LOGIN_COUNT_PREFIX + oldEmail)

            // Update registered emails list
            val registeredEmails = getRegisteredEmails().toMutableSet()
            registeredEmails.remove(oldEmail)
            registeredEmails.add(newEmail)
            editor.putStringSet(KEY_REGISTERED_EMAILS, registeredEmails)

            // Update current user email if this user is logged in
            val currentUserEmail = sharedPreferences.getString(KEY_CURRENT_USER_EMAIL, null)
            if (currentUserEmail == oldEmail) {
                editor.putString(KEY_CURRENT_USER_EMAIL, newEmail)
            }
        } else {
            // Just update username and contact
            editor.putString(KEY_USERNAME_PREFIX + oldEmail, newUsername)
            editor.putString(KEY_CONTACT_PREFIX + oldEmail, newContact)
        }

        return editor.commit()
    }

    // Change user password
    fun changePassword(email: String, currentPassword: String, newPassword: String): Boolean {
        if (!isEmailRegistered(email)) {
            return false
        }

        val storedPassword = sharedPreferences.getString(KEY_PASSWORD_PREFIX + email, null)
        if (storedPassword != currentPassword) {
            return false
        }

        val editor = sharedPreferences.edit()
        editor.putString(KEY_PASSWORD_PREFIX + email, newPassword)

        return editor.commit()
    }

    // Check if email is already registered
    fun isEmailRegistered(email: String): Boolean {
        val registeredEmails = getRegisteredEmails()
        return registeredEmails.contains(email)
    }

    // Get all registered emails
    private fun getRegisteredEmails(): Set<String> {
        return sharedPreferences.getStringSet(KEY_REGISTERED_EMAILS, emptySet()) ?: emptySet()
    }

    // Get total user count
    fun getUserCount(): Int {
        return sharedPreferences.getInt(KEY_USER_COUNT, 0)
    }

    // Clear all user data
    fun clearAllData(): Boolean {
        return sharedPreferences.edit().clear().commit()
    }

    // Helper function to get current date time
    private fun getCurrentDateTime(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return formatter.format(Date())
    }
}