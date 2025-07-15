package com.example.task_tacker

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.task_tacker.adapters.RemindersAdapter
import com.example.task_tacker.models.Reminder
import com.example.task_tacker.models.ReminderPriority
import com.example.task_tacker.utils.ReminderFilter
import com.example.task_tacker.utils.RemindersManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.*

class RemindersActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var menuButton: ImageView
    private lateinit var reminderTitleField: EditText
    private lateinit var reminderDescriptionField: EditText
    private lateinit var selectDateButton: MaterialButton
    private lateinit var selectTimeButton: MaterialButton
    private lateinit var selectedDateTimeLayout: LinearLayout
    private lateinit var selectedDateText: TextView
    private lateinit var selectedTimeText: TextView
    private lateinit var prioritySpinner: Spinner
    private lateinit var addReminderButton: MaterialButton
    private lateinit var filterChipGroup: ChipGroup
    private lateinit var chipAll: Chip
    private lateinit var chipUpcoming: Chip
    private lateinit var chipOverdue: Chip
    private lateinit var chipCompleted: Chip
    private lateinit var quickReminderCard1: MaterialCardView
    private lateinit var quickReminderCard2: MaterialCardView
    private lateinit var quickReminderCard3: MaterialCardView
    private lateinit var remindersRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout

    private lateinit var remindersManager: RemindersManager
    private lateinit var remindersAdapter: RemindersAdapter
    private var currentFilter = ReminderFilter.ALL
    private var selectedDate: Calendar = Calendar.getInstance()
    private var selectedTime: Calendar = Calendar.getInstance()
    private var isDateSelected = false
    private var isTimeSelected = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reminders)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        initializeRemindersManager()
        setupSpinner()
        setupRecyclerView()
        setupClickListeners()
        setupFilterChips()
        loadReminders()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        menuButton = findViewById(R.id.menuButton)
        reminderTitleField = findViewById(R.id.reminderTitleField)
        reminderDescriptionField = findViewById(R.id.reminderDescriptionField)
        selectDateButton = findViewById(R.id.selectDateButton)
        selectTimeButton = findViewById(R.id.selectTimeButton)
        selectedDateTimeLayout = findViewById(R.id.selectedDateTimeLayout)
        selectedDateText = findViewById(R.id.selectedDateText)
        selectedTimeText = findViewById(R.id.selectedTimeText)
        prioritySpinner = findViewById(R.id.prioritySpinner)
        addReminderButton = findViewById(R.id.addReminderButton)
        filterChipGroup = findViewById(R.id.filterChipGroup)
        chipAll = findViewById(R.id.chipAll)
        chipUpcoming = findViewById(R.id.chipUpcoming)
        chipOverdue = findViewById(R.id.chipOverdue)
        chipCompleted = findViewById(R.id.chipCompleted)
        quickReminderCard1 = findViewById(R.id.quickReminderCard1)
        quickReminderCard2 = findViewById(R.id.quickReminderCard2)
        quickReminderCard3 = findViewById(R.id.quickReminderCard3)
        remindersRecyclerView = findViewById(R.id.remindersRecyclerView)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
    }

    private fun initializeRemindersManager() {
        remindersManager = RemindersManager(this)
    }

    private fun setupSpinner() {
        val priorities = listOf("Low Priority", "Medium Priority", "High Priority")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioritySpinner.adapter = adapter
        prioritySpinner.setSelection(1) // Default to Medium Priority
    }

    private fun setupRecyclerView() {
        remindersAdapter = RemindersAdapter(
            reminders = mutableListOf(),
            onReminderClick = { reminder -> showReminderDetailsDialog(reminder) },
            onReminderEdit = { reminder -> showEditReminderDialog(reminder) },
            onReminderChecked = { reminder -> toggleReminderCompletion(reminder) }
        )

        remindersRecyclerView.layoutManager = LinearLayoutManager(this)
        remindersRecyclerView.adapter = remindersAdapter
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener { navigateBack() }
        menuButton.setOnClickListener { showMenuDialog() }
        selectDateButton.setOnClickListener { showDatePicker() }
        selectTimeButton.setOnClickListener { showTimePicker() }
        addReminderButton.setOnClickListener { addNewReminder() }

        quickReminderCard1.setOnClickListener { createQuickReminder(15) }
        quickReminderCard2.setOnClickListener { createQuickReminder(60) }
        quickReminderCard3.setOnClickListener { createQuickReminder(24 * 60) }
    }

    private fun setupFilterChips() {
        chipAll.isChecked = true

        chipAll.setOnClickListener {
            selectFilter(ReminderFilter.ALL)
        }

        chipUpcoming.setOnClickListener {
            selectFilter(ReminderFilter.UPCOMING)
        }

        chipOverdue.setOnClickListener {
            selectFilter(ReminderFilter.OVERDUE)
        }

        chipCompleted.setOnClickListener {
            selectFilter(ReminderFilter.COMPLETED)
        }
    }

    private fun selectFilter(filter: ReminderFilter) {
        currentFilter = filter

        chipAll.isChecked = (filter == ReminderFilter.ALL)
        chipUpcoming.isChecked = (filter == ReminderFilter.UPCOMING)
        chipOverdue.isChecked = (filter == ReminderFilter.OVERDUE)
        chipCompleted.isChecked = (filter == ReminderFilter.COMPLETED)

        applyFilter()
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                selectedDate.set(year, month, dayOfMonth)
                isDateSelected = true
                updateDateTimeDisplay()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.datePicker.minDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                selectedTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                selectedTime.set(Calendar.MINUTE, minute)
                isTimeSelected = true
                updateDateTimeDisplay()
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            false
        )
        timePickerDialog.show()
    }

    private fun updateDateTimeDisplay() {
        if (isDateSelected || isTimeSelected) {
            selectedDateTimeLayout.visibility = View.VISIBLE

            if (isDateSelected) {
                val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                selectedDateText.text = "Date: ${dateFormat.format(selectedDate.time)}"
            }

            if (isTimeSelected) {
                val timeFormat = java.text.SimpleDateFormat("hh:mm a", Locale.getDefault())
                selectedTimeText.text = "Time: ${timeFormat.format(selectedTime.time)}"
            }
        }
    }

    private fun addNewReminder() {
        val title = reminderTitleField.text.toString().trim()
        if (title.isEmpty()) {
            showToast("Please enter a reminder title")
            return
        }

        if (!isDateSelected || !isTimeSelected) {
            showToast("Please select both date and time")
            return
        }

        val description = reminderDescriptionField.text.toString().trim()
        val priority = when (prioritySpinner.selectedItemPosition) {
            0 -> ReminderPriority.LOW
            1 -> ReminderPriority.MEDIUM
            2 -> ReminderPriority.HIGH
            else -> ReminderPriority.MEDIUM
        }

        val reminderDateTime = Calendar.getInstance()
        reminderDateTime.set(Calendar.YEAR, selectedDate.get(Calendar.YEAR))
        reminderDateTime.set(Calendar.MONTH, selectedDate.get(Calendar.MONTH))
        reminderDateTime.set(Calendar.DAY_OF_MONTH, selectedDate.get(Calendar.DAY_OF_MONTH))
        reminderDateTime.set(Calendar.HOUR_OF_DAY, selectedTime.get(Calendar.HOUR_OF_DAY))
        reminderDateTime.set(Calendar.MINUTE, selectedTime.get(Calendar.MINUTE))
        reminderDateTime.set(Calendar.SECOND, 0)

        if (reminderDateTime.timeInMillis <= System.currentTimeMillis()) {
            showToast("Please select a future date and time")
            return
        }

        val newReminder = Reminder(
            title = title,
            description = description,
            dateTime = reminderDateTime.timeInMillis,
            priority = priority
        )

        if (remindersManager.addReminder(newReminder)) {
            clearForm()
            loadReminders()
            showToast("Reminder added successfully!")
        } else {
            showToast("Failed to add reminder")
        }
    }

    private fun createQuickReminder(minutes: Int) {
        val title = reminderTitleField.text.toString().trim()
        if (title.isEmpty()) {
            showToast("Please enter a reminder title first")
            return
        }

        val reminderTime = Calendar.getInstance()
        reminderTime.add(Calendar.MINUTE, minutes)

        val quickReminder = Reminder(
            title = title,
            description = reminderDescriptionField.text.toString().trim(),
            dateTime = reminderTime.timeInMillis,
            priority = when (prioritySpinner.selectedItemPosition) {
                0 -> ReminderPriority.LOW
                1 -> ReminderPriority.MEDIUM
                2 -> ReminderPriority.HIGH
                else -> ReminderPriority.MEDIUM
            }
        )

        if (remindersManager.addReminder(quickReminder)) {
            clearForm()
            loadReminders()
            val timeText = when (minutes) {
                15 -> "15 minutes"
                60 -> "1 hour"
                else -> "${minutes / 60} hours"
            }
            showToast("Quick reminder set for $timeText from now!")
        } else {
            showToast("Failed to create quick reminder")
        }
    }

    private fun clearForm() {
        reminderTitleField.text.clear()
        reminderDescriptionField.text.clear()
        prioritySpinner.setSelection(1)
        selectedDateTimeLayout.visibility = View.GONE
        isDateSelected = false
        isTimeSelected = false
        selectedDate = Calendar.getInstance()
        selectedTime = Calendar.getInstance()
    }

    private fun loadReminders() {
        applyFilter()
    }

    private fun applyFilter() {
        try {
            val filteredReminders = remindersManager.getFilteredReminders(currentFilter)
            remindersAdapter.updateReminders(filteredReminders)
            updateEmptyState(filteredReminders.isEmpty())

            val filterName = when (currentFilter) {
                ReminderFilter.ALL -> "All"
                ReminderFilter.UPCOMING -> "Upcoming"
                ReminderFilter.OVERDUE -> "Overdue"
                ReminderFilter.COMPLETED -> "Completed"
            }

            if (filteredReminders.isNotEmpty()) {
                showToast("Showing $filterName reminders (${filteredReminders.size})")
            }

        } catch (e: Exception) {
            showToast("Error filtering reminders: ${e.message}")
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            emptyStateLayout.visibility = View.VISIBLE
            remindersRecyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            remindersRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun toggleReminderCompletion(reminder: Reminder) {
        if (remindersManager.toggleReminderCompletion(reminder.id)) {
            loadReminders()
            val status = if (!reminder.isCompleted) "completed" else "pending"
            showToast("Reminder marked as $status")
        } else {
            showToast("Failed to update reminder")
        }
    }

    private fun showReminderDetailsDialog(reminder: Reminder) {
        val message = """
            Title: ${reminder.title}
            ${if (reminder.description.isNotEmpty()) "Description: ${reminder.description}\n" else ""}Date & Time: ${reminder.getFormattedDateTime()}
            Priority: ${reminder.priority.displayName}
            Status: ${if (reminder.isCompleted) "Completed" else if (reminder.isOverdue()) "Overdue" else "Upcoming"}
            ${if (!reminder.isCompleted) "Time Until: ${reminder.getTimeUntilReminder()}" else ""}
            Created: ${reminder.createdAt}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Reminder Details")
            .setMessage(message)
            .setPositiveButton("Edit") { _, _ -> showEditReminderDialog(reminder) }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showEditReminderDialog(reminder: Reminder) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_reminder, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.editReminderTitle)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.editReminderDescription)
        val dateButton = dialogView.findViewById<MaterialButton>(R.id.editSelectDateButton)
        val timeButton = dialogView.findViewById<MaterialButton>(R.id.editSelectTimeButton)
        val prioritySpinner = dialogView.findViewById<Spinner>(R.id.editReminderPriority)

        val priorities = listOf("Low Priority", "Medium Priority", "High Priority")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioritySpinner.adapter = adapter

        titleInput.setText(reminder.title)
        descriptionInput.setText(reminder.description)
        prioritySpinner.setSelection(reminder.priority.ordinal)

        val editCalendar = Calendar.getInstance()
        editCalendar.timeInMillis = reminder.dateTime

        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeFormat = java.text.SimpleDateFormat("hh:mm a", Locale.getDefault())
        dateButton.text = dateFormat.format(editCalendar.time)
        timeButton.text = timeFormat.format(editCalendar.time)

        dateButton.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    editCalendar.set(year, month, dayOfMonth)
                    dateButton.text = dateFormat.format(editCalendar.time)
                },
                editCalendar.get(Calendar.YEAR),
                editCalendar.get(Calendar.MONTH),
                editCalendar.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }

        timeButton.setOnClickListener {
            val timePickerDialog = TimePickerDialog(
                this,
                { _, hourOfDay, minute ->
                    editCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                    editCalendar.set(Calendar.MINUTE, minute)
                    timeButton.text = timeFormat.format(editCalendar.time)
                },
                editCalendar.get(Calendar.HOUR_OF_DAY),
                editCalendar.get(Calendar.MINUTE),
                false
            )
            timePickerDialog.show()
        }

        AlertDialog.Builder(this)
            .setTitle("Edit Reminder")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newTitle = titleInput.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    val updatedReminder = reminder.copy(
                        title = newTitle,
                        description = descriptionInput.text.toString().trim(),
                        dateTime = editCalendar.timeInMillis,
                        priority = ReminderPriority.values()[prioritySpinner.selectedItemPosition]
                    )

                    if (remindersManager.updateReminder(updatedReminder)) {
                        loadReminders()
                        showToast("Reminder updated successfully!")
                    } else {
                        showToast("Failed to update reminder")
                    }
                } else {
                    showToast("Reminder title cannot be empty")
                }
            }
            .setNegativeButton("Delete") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Reminder?")
                    .setMessage("Are you sure you want to delete \"${reminder.title}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        if (remindersManager.deleteReminder(reminder.id)) {
                            loadReminders()
                            showToast("Reminder deleted!")
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun showMenuDialog() {
        val options = arrayOf(
            "Reminder Statistics",
            "Upcoming Reminders",
            "Clear All Reminders"
        )

        AlertDialog.Builder(this)
            .setTitle("Reminders Menu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showReminderStatistics()
                    1 -> showUpcomingReminders()
                    2 -> confirmClearAllReminders()
                }
            }
            .show()
    }

    private fun showReminderStatistics() {
        val stats = remindersManager.getReminderStatistics()
        val message = """
            📊 Reminder Statistics
            
            Total Reminders: ${stats.totalReminders}
            Upcoming Reminders: ${stats.upcomingReminders}
            Overdue Reminders: ${stats.overdueReminders}
            Completed Reminders: ${stats.completedReminders}
            High Priority Reminders: ${stats.highPriorityReminders}
            
            Current Filter: ${when (currentFilter) {
            ReminderFilter.ALL -> "All Reminders"
            ReminderFilter.UPCOMING -> "Upcoming"
            ReminderFilter.OVERDUE -> "Overdue"
            ReminderFilter.COMPLETED -> "Completed"
        }}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Statistics")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun showUpcomingReminders() {
        val upcoming = remindersManager.getUpcomingReminders(24)
        val message = if (upcoming.isEmpty()) {
            "No reminders in the next 24 hours."
        } else {
            val remindersList = upcoming.take(5).joinToString("\n\n") { reminder ->
                "• ${reminder.title}\n  ${reminder.getFormattedDateTime()}\n  ${reminder.getTimeUntilReminder()}"
            }
            "Upcoming reminders in the next 24 hours:\n\n$remindersList"
        }

        AlertDialog.Builder(this)
            .setTitle("Upcoming Reminders")
            .setMessage(message)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun confirmClearAllReminders() {
        AlertDialog.Builder(this)
            .setTitle("Clear All Reminders")
            .setMessage("Are you sure you want to delete all reminders? This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                if (remindersManager.clearAllReminders()) {
                    loadReminders()
                    showToast("All reminders cleared!")
                } else {
                    showToast("Failed to clear reminders")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateBack() {
        finish()
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        loadReminders()
    }
}