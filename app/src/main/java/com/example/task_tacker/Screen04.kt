package com.example.task_tacker

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.task_tacker.models.User
import com.example.task_tacker.utils.UserPreferencesManager
import com.example.task_tacker.utils.ValidationUtils
import com.google.android.material.button.MaterialButton

class Screen04 : AppCompatActivity() {

    private lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var contactEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var confirmPasswordEditText: EditText
    private lateinit var createAccountButton: MaterialButton
    private lateinit var backButton: ImageView
    private lateinit var alreadyHaveAccountText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_screen04)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        initializeUserManager()
        setupClickListeners()
    }

    private fun initializeViews() {
        usernameEditText = findViewById(R.id.usernameEditText)
        emailEditText = findViewById(R.id.emailEditText)
        contactEditText = findViewById(R.id.contactEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText)
        createAccountButton = findViewById(R.id.createAccountButton)
        backButton = findViewById(R.id.backButton)
        alreadyHaveAccountText = findViewById(R.id.alreadyHaveAccountText)
    }

    private fun initializeUserManager() {
        userPreferencesManager = UserPreferencesManager(this)
    }

    private fun setupClickListeners() {
        createAccountButton.setOnClickListener {
            handleSignUp()
        }

        backButton.setOnClickListener {
            finish()
        }

        alreadyHaveAccountText.setOnClickListener {
            navigateToSignIn()
        }
    }

    private fun handleSignUp() {
        val username = usernameEditText.text.toString().trim()
        val email = emailEditText.text.toString().trim()
        val contact = contactEditText.text.toString().trim()
        val password = passwordEditText.text.toString()
        val confirmPassword = confirmPasswordEditText.text.toString()

        // Validate inputs
        if (!validateInputs(username, email, contact, password, confirmPassword)) {
            return
        }

        // Check if email is already registered
        if (userPreferencesManager.isEmailRegistered(email)) {
            showToast("Email is already registered. Please use a different email.")
            return
        }

        // Create user object
        val user = User(username, email, contact, password)

        // Register user
        val isRegistered = userPreferencesManager.registerUser(user)

        if (isRegistered) {
            showToast("Account created successfully! Please sign in with your credentials.")

            // Clear the form fields
            clearFormFields()

            // Navigate to Sign In screen with the email pre-filled
            navigateToSignInWithEmail(email)
        } else {
            showToast("Failed to create account. Please try again.")
        }
    }

    private fun validateInputs(
        username: String,
        email: String,
        contact: String,
        password: String,
        confirmPassword: String
    ): Boolean {

        // Validate username
        ValidationUtils.getUsernameValidationMessage(username)?.let {
            showToast(it)
            usernameEditText.requestFocus()
            return false
        }

        // Validate email
        ValidationUtils.getEmailValidationMessage(email)?.let {
            showToast(it)
            emailEditText.requestFocus()
            return false
        }

        // Validate contact number
        ValidationUtils.getPhoneValidationMessage(contact)?.let {
            showToast(it)
            contactEditText.requestFocus()
            return false
        }

        // Validate password
        ValidationUtils.getPasswordValidationMessage(password)?.let {
            showToast(it)
            passwordEditText.requestFocus()
            return false
        }

        // Validate password confirmation
        if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
            showToast("Passwords do not match")
            confirmPasswordEditText.requestFocus()
            return false
        }

        return true
    }

    private fun clearFormFields() {
        usernameEditText.text.clear()
        emailEditText.text.clear()
        contactEditText.text.clear()
        passwordEditText.text.clear()
        confirmPasswordEditText.text.clear()
    }

    private fun navigateToSignIn() {
        val intent = Intent(this, Screen05::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToSignInWithEmail(email: String) {
        val intent = Intent(this, Screen05::class.java)
        intent.putExtra("pre_fill_email", email)
        intent.putExtra("account_created", true)
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}