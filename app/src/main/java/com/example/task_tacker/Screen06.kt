package com.example.task_tacker

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.task_tacker.utils.UserPreferencesManager
import com.example.task_tacker.utils.TasksManager
import com.example.task_tacker.utils.RemindersManager
import com.google.android.material.card.MaterialCardView
import java.util.*

class Screen06 : AppCompatActivity() {

    private lateinit var userPreferencesManager: UserPreferencesManager
    private lateinit var tasksManager: TasksManager
    private lateinit var remindersManager: RemindersManager

    // UI elements that exist in your layout
    private lateinit var userNameText: TextView
    private lateinit var profileButton: ImageView
    private lateinit var todoListCard: MaterialCardView
    private lateinit var stopWatchCard: MaterialCardView
    private lateinit var remindersCard: MaterialCardView
    private lateinit var totalTasksNumber: TextView
    private lateinit var completedTasksNumber: TextView
    private lateinit var activeRemindersNumber: TextView

    // Stats for tracking
    private var totalTasks = 0
    private var completedTasks = 0
    private var activeReminders = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_screen06)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeManagers()
        initializeViews()
        setupUserProfile()
        updateStats()
        setupClickListeners()
        setupBackPressedCallback()
    }

    private fun initializeManagers() {
        userPreferencesManager = UserPreferencesManager(this)
        tasksManager = TasksManager(this)
        remindersManager = RemindersManager(this)
    }

    private fun initializeViews() {
        // Initialize UI elements that exist in your layout
        userNameText = findViewById(R.id.userNameText)
        profileButton = findViewById(R.id.profileButton)
        todoListCard = findViewById(R.id.todoListCard)
        stopWatchCard = findViewById(R.id.stopWatchCard)
        remindersCard = findViewById(R.id.remindersCard)
        totalTasksNumber = findViewById(R.id.totalTasksNumber)
        completedTasksNumber = findViewById(R.id.completedTasksNumber)
        activeRemindersNumber = findViewById(R.id.activeRemindersNumber)
    }

    private fun setupUserProfile() {
        // Check if user is logged in
        if (!userPreferencesManager.isUserLoggedIn()) {
            redirectToLogin()
            return
        }

        val currentUser = userPreferencesManager.getCurrentUser()
        if (currentUser != null) {
            // Display personalized greeting based on current time
            val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            val greeting = when (currentHour) {
                in 5..11 -> "Good morning"
                in 12..16 -> "Good afternoon"
                in 17..20 -> "Good evening"
                else -> "Hello"
            }

            // Current time is 16:38 (4:38 PM), so it's afternoon
            userNameText.text = "$greeting, ${currentUser.username}!"
        } else {
            showToast("User data not found. Please login again.")
            redirectToLogin()
        }
    }

    private fun updateStats() {
        try {
            // Get real task and reminder statistics
            val allTasks = tasksManager.loadTasks()
            totalTasks = allTasks.size
            completedTasks = allTasks.count { it.isCompleted }

            val allReminders = remindersManager.loadReminders()
            activeReminders = allReminders.count {
                !it.isCompleted && it.dateTime > System.currentTimeMillis()
            }

            // Update UI with real data
            totalTasksNumber.text = totalTasks.toString()
            completedTasksNumber.text = completedTasks.toString()
            activeRemindersNumber.text = activeReminders.toString()

            // Show user feedback about their progress
            showProgressFeedback(totalTasks, completedTasks, activeReminders)

        } catch (e: Exception) {
            // Fallback values if there's an error
            totalTasksNumber.text = "0"
            completedTasksNumber.text = "0"
            activeRemindersNumber.text = "0"
            showToast("Error loading data: ${e.message}")
        }
    }

    private fun showProgressFeedback(totalTasks: Int, completedTasks: Int, activeReminders: Int) {
        // Show appropriate feedback based on user's progress
        when {
            totalTasks == 0 && activeReminders == 0 -> {
                showToast("Welcome to TASK-Track! Start by adding your first task or reminder.")
            }
            completedTasks == totalTasks && totalTasks > 0 -> {
                showToast(" Amazing! All $totalTasks tasks completed!")
            }
            totalTasks > 0 -> {
                val percentage = ((completedTasks.toDouble() / totalTasks.toDouble()) * 100).toInt()
                showToast(" Progress: $completedTasks/$totalTasks tasks completed ($percentage%)")
            }
        }
    }

    private fun setupClickListeners() {
        profileButton.setOnClickListener {
            navigateToProfile()
        }

        todoListCard.setOnClickListener {
            navigateToTodoList()
        }

        stopWatchCard.setOnClickListener {
            navigateToStopwatch()
        }

        remindersCard.setOnClickListener {
            navigateToReminders()
        }
    }

    private fun setupBackPressedCallback() {
        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitConfirmationDialog()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun showExitConfirmationDialog() {
        val progressMessage = when {
            totalTasks == 0 && activeReminders == 0 ->
                "No tasks or reminders yet. Start organizing your day!"
            completedTasks == totalTasks && totalTasks > 0 ->
                "All tasks completed! Great work!"
            activeReminders > 0 ->
                "You have $activeReminders active reminders"
            else ->
                " $completedTasks of $totalTasks tasks completed"
        }

        AlertDialog.Builder(this)
            .setTitle("Exit TASK-Track")
            .setMessage("$progressMessage\n\nAre you sure you want to exit?")
            .setPositiveButton("Exit") { _, _ ->
                finishAffinity()
            }
            .setNegativeButton("Stay", null)
            .show()
    }

    private fun navigateToProfile() {
        val currentUser = userPreferencesManager.getCurrentUser()
        if (currentUser != null) {
            try {
                val intent = Intent(this, ProfileActivity::class.java)
                startActivity(intent)
                showToast("Opening profile...")
            } catch (e: Exception) {
                showToast("Profile not available: ${e.message}")
            }
        } else {
            showToast("User data not found. Please login again.")
            redirectToLogin()
        }
    }

    private fun navigateToTodoList() {
        try {
            val intent = Intent(this, TodoListActivity::class.java)
            startActivity(intent)
            showToast("Opening your tasks...")
        } catch (e: Exception) {
            showToast("Failed to open To-Do List: ${e.message}")
        }
    }

    private fun navigateToStopwatch() {
        try {
            val intent = Intent(this, StopwatchActivity::class.java)
            startActivity(intent)
            showToast("Opening stopwatch...")
        } catch (e: Exception) {
            showToast("Failed to open Stopwatch: ${e.message}")
        }
    }

    private fun navigateToReminders() {
        try {
            val intent = Intent(this, RemindersActivity::class.java)
            startActivity(intent)
            showToast("Opening reminders...")
        } catch (e: Exception) {
            showToast("Failed to open Reminders: ${e.message}")
        }
    }

    private fun redirectToLogin() {
        val intent = Intent(this, Screen05::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        setupUserProfile()
        updateStats()
    }

    override fun onStart() {
        super.onStart()
        updateStats()
    }
}