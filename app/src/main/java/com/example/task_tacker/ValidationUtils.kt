package com.example.task_tacker.utils

import android.util.Patterns

object ValidationUtils {

    /**
     * Validates email format using Android's built-in email pattern
     */
    fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validates password strength - minimum 6 characters
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }

    /**
     * Validates username - minimum 3 characters and not empty
     */
    fun isValidUsername(username: String): Boolean {
        return username.isNotEmpty() && username.length >= 3
    }

    /**
     * Validates phone number - minimum 10 digits and only contains numbers
     */
    fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return phoneNumber.isNotEmpty() &&
                phoneNumber.length >= 10 &&
                phoneNumber.all { it.isDigit() }
    }

    /**
     * Checks if password and confirm password match
     */
    fun doPasswordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword
    }

    /**
     * Returns specific error message for password validation
     */
    fun getPasswordValidationMessage(password: String): String? {
        return when {
            password.isEmpty() -> "Password cannot be empty"
            password.length < 6 -> "Password must be at least 6 characters"
            !password.any { it.isUpperCase() } -> "Password must contain at least one uppercase letter"
            !password.any { it.isLowerCase() } -> "Password must contain at least one lowercase letter"
            !password.any { it.isDigit() } -> "Password must contain at least one number"
            else -> null // Valid password
        }
    }

    /**
     * Returns specific error message for email validation
     */
    fun getEmailValidationMessage(email: String): String? {
        return when {
            email.isEmpty() -> "Email cannot be empty"
            !isValidEmail(email) -> "Please enter a valid email address"
            else -> null // Valid email
        }
    }

    /**
     * Returns specific error message for username validation
     */
    fun getUsernameValidationMessage(username: String): String? {
        return when {
            username.isEmpty() -> "Username cannot be empty"
            username.length < 3 -> "Username must be at least 3 characters"
            username.length > 20 -> "Username must be less than 20 characters"
            !username.all { it.isLetterOrDigit() || it == '_' } -> "Username can only contain letters, numbers, and underscores"
            else -> null // Valid username
        }
    }

    /**
     * Returns specific error message for phone validation
     */
    fun getPhoneValidationMessage(phone: String): String? {
        return when {
            phone.isEmpty() -> "Contact number cannot be empty"
            phone.length < 10 -> "Contact number must be at least 10 digits"
            phone.length > 15 -> "Contact number must be less than 15 digits"
            !phone.all { it.isDigit() } -> "Contact number must contain only digits"
            else -> null // Valid phone number
        }
    }

    /**
     * Advanced password validation with multiple criteria
     */
    fun isStrongPassword(password: String): Boolean {
        return password.length >= 8 &&
                password.any { it.isUpperCase() } &&
                password.any { it.isLowerCase() } &&
                password.any { it.isDigit() } &&
                password.any { !it.isLetterOrDigit() } // Special character
    }

    /**
     * Validates full name
     */
    fun isValidFullName(name: String): Boolean {
        return name.isNotEmpty() &&
                name.length >= 2 &&
                name.all { it.isLetter() || it.isWhitespace() }
    }

    /**
     * Removes extra whitespaces and trims input
     */
    fun sanitizeInput(input: String): String {
        return input.trim().replace("\\s+".toRegex(), " ")
    }

    /**
     * Validates if string contains only alphabetic characters
     */
    fun isAlphabetic(text: String): Boolean {
        return text.isNotEmpty() && text.all { it.isLetter() || it.isWhitespace() }
    }

    /**
     * Validates if string contains only numeric characters
     */
    fun isNumeric(text: String): Boolean {
        return text.isNotEmpty() && text.all { it.isDigit() }
    }

    /**
     * Checks if input is not empty after trimming
     */
    fun isNotEmpty(input: String): Boolean {
        return input.trim().isNotEmpty()
    }
}