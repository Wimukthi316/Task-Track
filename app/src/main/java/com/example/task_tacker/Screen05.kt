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
import com.example.task_tacker.utils.UserPreferencesManager
import com.example.task_tacker.utils.ValidationUtils
import com.google.android.material.button.MaterialButton

class Screen05 : AppCompatActivity() {

    private lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var loginButton: MaterialButton
    private lateinit var backButton: ImageView
    private lateinit var forgotPasswordText: TextView
    private lateinit var signUpText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_screen05)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        initializeUserManager()
        setupClickListeners()
        handleIncomingIntent()
        // Remove or modify the automatic login check
        // checkIfUserLoggedIn() // ← Comment out or remove this line
    }

    private fun initializeViews() {
        usernameEditText = findViewById(R.id.usernameEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        loginButton = findViewById(R.id.loginButton)
        backButton = findViewById(R.id.backButton)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
        signUpText = findViewById(R.id.signsUpText)
    }

    private fun initializeUserManager() {
        userPreferencesManager = UserPreferencesManager(this)
    }

    private fun setupClickListeners() {
        loginButton.setOnClickListener {
            handleSignIn()
        }

        backButton.setOnClickListener {
            finish()
        }

        signUpText.setOnClickListener {
            navigateToSignUp()
        }

        forgotPasswordText.setOnClickListener {
            handleForgotPassword()
        }
    }

    private fun handleIncomingIntent() {
        // Check if this activity was started from successful registration
        val preFillEmail = intent.getStringExtra("pre_fill_email")
        val accountCreated = intent.getBooleanExtra("account_created", false)

        if (accountCreated && !preFillEmail.isNullOrEmpty()) {
            usernameEditText.setText(preFillEmail)
            passwordEditText.requestFocus()
            showToast("Account created successfully! Please sign in.")
        }
    }

    private fun handleSignIn() {
        val email = usernameEditText.text.toString().trim()
        val password = passwordEditText.text.toString()

        // Validate inputs
        if (!validateInputs(email, password)) {
            return
        }

        // Authenticate user
        val isAuthenticated = userPreferencesManager.authenticateUser(email, password)

        if (isAuthenticated) {
            // Login user
            val isLoggedIn = userPreferencesManager.loginUser(email)

            if (isLoggedIn) {
                val user = userPreferencesManager.getCurrentUser()
                showToast("Welcome back, ${user?.username ?: "User"}!")
                navigateToMainScreen()
            } else {
                showToast("Failed to login. Please try again.")
            }
        } else {
            showToast("Invalid email or password. Please try again.")
            passwordEditText.text.clear()
            passwordEditText.requestFocus()
        }
    }

    private fun validateInputs(email: String, password: String): Boolean {
        // Validate email
        ValidationUtils.getEmailValidationMessage(email)?.let {
            showToast(it)
            usernameEditText.requestFocus()
            return false
        }

        // Validate password
        if (password.isEmpty()) {
            showToast("Password cannot be empty")
            passwordEditText.requestFocus()
            return false
        }

        return true
    }

    private fun handleForgotPassword() {
        val email = usernameEditText.text.toString().trim()

        if (email.isEmpty()) {
            showToast("Please enter your email address first")
            usernameEditText.requestFocus()
            return
        }

        if (!ValidationUtils.isValidEmail(email)) {
            showToast("Please enter a valid email address")
            usernameEditText.requestFocus()
            return
        }

        if (userPreferencesManager.isEmailRegistered(email)) {
            val user = userPreferencesManager.getUserByEmail(email)
            showToast("Your password is: ${user?.password}")
            // Note: In a real app, you would send a password reset email instead
        } else {
            showToast("No account found with this email address")
        }
    }

    private fun navigateToSignUp() {
        val intent = Intent(this, Screen04::class.java)
        startActivity(intent)
        finish()
    }

    private fun navigateToMainScreen() {
        val intent = Intent(this, Screen06::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}