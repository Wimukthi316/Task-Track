package com.example.task_tacker

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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
import com.google.android.material.card.MaterialCardView

class ProfileActivity : AppCompatActivity() {

    private lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var backButton: ImageView
    private lateinit var editProfileButton: ImageView
    private lateinit var profileUserName: TextView
    private lateinit var profileUserEmail: TextView
    private lateinit var memberSinceText: TextView
    private lateinit var displayUsername: TextView
    private lateinit var displayEmail: TextView
    private lateinit var displayContact: TextView
    private lateinit var changePasswordButton: MaterialButton
    private lateinit var logoutButton: MaterialButton
    private lateinit var personalInfoCard: MaterialCardView

    private var currentUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        initializeUserManager()
        loadUserProfile()
        setupClickListeners()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        editProfileButton = findViewById(R.id.editProfileButton)
        profileUserName = findViewById(R.id.profileUserName)
        profileUserEmail = findViewById(R.id.profileUserEmail)
        memberSinceText = findViewById(R.id.memberSinceText)
        displayUsername = findViewById(R.id.displayUsername)
        displayEmail = findViewById(R.id.displayEmail)
        displayContact = findViewById(R.id.displayContact)
        changePasswordButton = findViewById(R.id.changePasswordButton)
        logoutButton = findViewById(R.id.logoutButton)
        personalInfoCard = findViewById(R.id.personalInfoCard)
    }

    private fun initializeUserManager() {
        userPreferencesManager = UserPreferencesManager(this)
    }

    private fun loadUserProfile() {
        currentUser = userPreferencesManager.getCurrentUser()

        if (currentUser == null) {
            showToast("User data not found")
            finish()
            return
        }

        displayUserInformation(currentUser!!)
    }

    private fun displayUserInformation(user: User) {
        // Header section
        profileUserName.text = formatUserName(user.username)
        profileUserEmail.text = user.email
        memberSinceText.text = user.getFormattedJoinDate()

        // Personal information card
        displayUsername.text = user.username
        displayEmail.text = user.email
        displayContact.text = user.contactNumber
    }

    private fun formatUserName(username: String): String {
        return username.split(" ").joinToString(" ") { word ->
            word.replaceFirstChar { it.uppercase() }
        }
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            finish()
        }

        editProfileButton.setOnClickListener {
            showEditProfileDialog()
        }

        personalInfoCard.setOnClickListener {
            showEditProfileDialog()
        }

        changePasswordButton.setOnClickListener {
            showChangePasswordDialog()
        }

        logoutButton.setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showEditProfileDialog() {
        val user = currentUser ?: return

        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val usernameInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editUsernameInput)
        val emailInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editEmailInput)
        val contactInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.editContactInput)

        usernameInput.setText(user.username)
        emailInput.setText(user.email)
        contactInput.setText(user.contactNumber)

        AlertDialog.Builder(this)
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newUsername = usernameInput.text.toString().trim()
                val newEmail = emailInput.text.toString().trim()
                val newContact = contactInput.text.toString().trim()
                updateUserProfile(newUsername, newEmail, newContact)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateUserProfile(newUsername: String, newEmail: String, newContact: String) {
        val user = currentUser ?: return

        // Validate inputs
        ValidationUtils.getUsernameValidationMessage(newUsername)?.let {
            showToast(it)
            return
        }

        ValidationUtils.getEmailValidationMessage(newEmail)?.let {
            showToast(it)
            return
        }

        ValidationUtils.getPhoneValidationMessage(newContact)?.let {
            showToast(it)
            return
        }

        // Update user profile
        val isUpdated = userPreferencesManager.updateUserProfile(user.email, newUsername, newEmail, newContact)

        if (isUpdated) {
            showToast("Profile updated successfully!")
            loadUserProfile() // Reload the profile data
        } else {
            showToast("Failed to update profile. Email might already be in use.")
        }
    }

    private fun showChangePasswordDialog() {
        val user = currentUser ?: return

        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)
        val currentPasswordInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.currentPasswordInput)
        val newPasswordInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.newPasswordInput)
        val confirmPasswordInput = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.confirmPasswordInput)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Change") { _, _ ->
                val currentPassword = currentPasswordInput.text.toString()
                val newPassword = newPasswordInput.text.toString()
                val confirmPassword = confirmPasswordInput.text.toString()
                changePassword(user.email, currentPassword, newPassword, confirmPassword)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun changePassword(email: String, currentPassword: String, newPassword: String, confirmPassword: String) {
        // Validate inputs
        if (currentPassword.isEmpty()) {
            showToast("Please enter your current password")
            return
        }

        ValidationUtils.getPasswordValidationMessage(newPassword)?.let {
            showToast(it)
            return
        }

        if (newPassword != confirmPassword) {
            showToast("New passwords do not match")
            return
        }

        // Change password
        val isChanged = userPreferencesManager.changePassword(email, currentPassword, newPassword)

        if (isChanged) {
            showToast("Password changed successfully!")
        } else {
            showToast("Current password is incorrect")
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                logout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun logout() {
        val isLoggedOut = userPreferencesManager.logoutUser()
        if (isLoggedOut) {
            showToast("Logged out successfully")
            val intent = Intent(this, Screen05::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } else {
            showToast("Failed to logout")
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        loadUserProfile() // Refresh profile data when returning to screen
    }
}