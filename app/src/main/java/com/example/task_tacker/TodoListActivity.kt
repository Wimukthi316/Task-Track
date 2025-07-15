package com.example.task_tacker

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.task_tacker.adapters.TasksAdapter
import com.example.task_tacker.models.Task
import com.example.task_tacker.models.TaskPriority
import com.example.task_tacker.utils.TaskFilter
import com.example.task_tacker.utils.TasksManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class TodoListActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var menuButton: ImageView
    private lateinit var taskInputField: EditText
    private lateinit var taskDescriptionField: EditText
    private lateinit var prioritySpinner: Spinner
    private lateinit var addTaskButton: MaterialButton
    private lateinit var filterChipGroup: ChipGroup
    private lateinit var chipAll: Chip
    private lateinit var chipPending: Chip
    private lateinit var chipCompleted: Chip
    private lateinit var tasksRecyclerView: RecyclerView
    private lateinit var emptyStateLayout: LinearLayout

    private lateinit var tasksManager: TasksManager
    private lateinit var tasksAdapter: TasksAdapter
    private var currentFilter = TaskFilter.ALL
    private var allTasks = mutableListOf<Task>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_todo_list)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        initializeTasksManager()
        setupSpinner()
        setupRecyclerView()
        setupClickListeners()
        setupFilterChips()
        loadTasks()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        menuButton = findViewById(R.id.menuButton)
        taskInputField = findViewById(R.id.taskInputField)
        taskDescriptionField = findViewById(R.id.taskDescriptionField)
        prioritySpinner = findViewById(R.id.prioritySpinner)
        addTaskButton = findViewById(R.id.addTaskButton)
        filterChipGroup = findViewById(R.id.filterChipGroup)
        chipAll = findViewById(R.id.chipAll)
        chipPending = findViewById(R.id.chipPending)
        chipCompleted = findViewById(R.id.chipCompleted)
        tasksRecyclerView = findViewById(R.id.tasksRecyclerView)
        emptyStateLayout = findViewById(R.id.emptyStateLayout)
    }

    private fun initializeTasksManager() {
        tasksManager = TasksManager(this)
    }

    private fun setupSpinner() {
        val priorities = listOf("Low Priority", "Medium Priority", "High Priority")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioritySpinner.adapter = adapter
        prioritySpinner.setSelection(1) // Default to Medium Priority
    }

    private fun setupRecyclerView() {
        tasksAdapter = TasksAdapter(
            tasks = mutableListOf(),
            onTaskClick = { task -> showTaskDetailsDialog(task) },
            onTaskEdit = { task -> showEditTaskDialog(task) },
            onTaskChecked = { task -> toggleTaskCompletion(task) }
        )

        tasksRecyclerView.layoutManager = LinearLayoutManager(this)
        tasksRecyclerView.adapter = tasksAdapter
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            navigateBack()
        }

        menuButton.setOnClickListener {
            showMenuDialog()
        }

        addTaskButton.setOnClickListener {
            addNewTask()
        }
    }

    private fun setupFilterChips() {
        // Set initial selection
        chipAll.isChecked = true

        filterChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            currentFilter = when {
                chipAll.id in checkedIds -> TaskFilter.ALL
                chipPending.id in checkedIds -> TaskFilter.PENDING
                chipCompleted.id in checkedIds -> TaskFilter.COMPLETED
                else -> {
                    // Fallback to ALL if nothing is selected
                    chipAll.isChecked = true
                    TaskFilter.ALL
                }
            }
            applyFilter()
        }

        // Add individual click listeners for better UX
        chipAll.setOnClickListener { selectFilter(TaskFilter.ALL) }
        chipPending.setOnClickListener { selectFilter(TaskFilter.PENDING) }
        chipCompleted.setOnClickListener { selectFilter(TaskFilter.COMPLETED) }
    }

    private fun selectFilter(filter: TaskFilter) {
        currentFilter = filter
        // Update chip selection
        chipAll.isChecked = (filter == TaskFilter.ALL)
        chipPending.isChecked = (filter == TaskFilter.PENDING)
        chipCompleted.isChecked = (filter == TaskFilter.COMPLETED)
        applyFilter()
    }

    private fun addNewTask() {
        val title = taskInputField.text.toString().trim()
        if (title.isEmpty()) {
            showToast("Please enter a task title")
            return
        }

        val description = taskDescriptionField.text.toString().trim()
        val priority = when (prioritySpinner.selectedItemPosition) {
            0 -> TaskPriority.LOW
            1 -> TaskPriority.MEDIUM
            2 -> TaskPriority.HIGH
            else -> TaskPriority.MEDIUM
        }

        val newTask = Task(
            title = title,
            description = description,
            priority = priority
        )

        if (tasksManager.addTask(newTask)) {
            taskInputField.text.clear()
            taskDescriptionField.text.clear()
            prioritySpinner.setSelection(1) // Reset to medium priority
            loadTasks()
            showToast("Task added successfully!")
        } else {
            showToast("Failed to add task")
        }
    }

    private fun loadTasks() {
        allTasks = tasksManager.loadTasks()
        applyFilter()
    }

    private fun applyFilter() {
        val filteredTasks = when (currentFilter) {
            TaskFilter.ALL -> allTasks
            TaskFilter.PENDING -> allTasks.filter { !it.isCompleted }
            TaskFilter.COMPLETED -> allTasks.filter { it.isCompleted }
        }

        tasksAdapter.updateTasks(filteredTasks)
        updateEmptyState(filteredTasks.isEmpty())
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            emptyStateLayout.visibility = View.VISIBLE
            tasksRecyclerView.visibility = View.GONE
        } else {
            emptyStateLayout.visibility = View.GONE
            tasksRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun toggleTaskCompletion(task: Task) {
        if (tasksManager.toggleTaskCompletion(task.id)) {
            loadTasks()
            val status = if (!task.isCompleted) "completed" else "pending"
            showToast("Task marked as $status")
        } else {
            showToast("Failed to update task")
        }
    }

    private fun showTaskDetailsDialog(task: Task) {
        val message = """
            Title: ${task.title}
            ${if (task.description.isNotEmpty()) "Description: ${task.description}\n" else ""}Priority: ${task.priority.displayName}
            Status: ${if (task.isCompleted) "Completed" else "Pending"}
            Created: ${task.getFormattedCreatedDate()} at ${task.getFormattedCreatedTime()}
            ${if (task.isCompleted && task.completedAt != null) "Completed: ${task.completedAt}" else ""}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Task Details")
            .setMessage(message)
            .setPositiveButton("Edit") { _, _ -> showEditTaskDialog(task) }
            .setNegativeButton("Close", null)
            .show()
    }

    private fun showEditTaskDialog(task: Task) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_task, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.editTaskTitle)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.editTaskDescription)
        val prioritySpinner = dialogView.findViewById<Spinner>(R.id.editTaskPriority)

        // Setup spinner
        val priorities = listOf("Low Priority", "Medium Priority", "High Priority")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, priorities)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        prioritySpinner.adapter = adapter

        // Pre-fill current values
        titleInput.setText(task.title)
        descriptionInput.setText(task.description)
        prioritySpinner.setSelection(task.priority.ordinal)

        AlertDialog.Builder(this)
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newTitle = titleInput.text.toString().trim()
                if (newTitle.isNotEmpty()) {
                    val updatedTask = task.copy(
                        title = newTitle,
                        description = descriptionInput.text.toString().trim(),
                        priority = TaskPriority.values()[prioritySpinner.selectedItemPosition]
                    )

                    if (tasksManager.updateTask(updatedTask)) {
                        loadTasks()
                        showToast("Task updated successfully!")
                    } else {
                        showToast("Failed to update task")
                    }
                } else {
                    showToast("Task title cannot be empty")
                }
            }
            .setNegativeButton("Delete") { _, _ ->
                AlertDialog.Builder(this)
                    .setTitle("Delete Task?")
                    .setMessage("Are you sure you want to delete \"${task.title}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        if (tasksManager.deleteTask(task.id)) {
                            loadTasks()
                            showToast("Task deleted!")
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .setNeutralButton("Cancel", null)
            .show()
    }

    private fun showMenuDialog() {
        val options = arrayOf("Clear All Tasks", "Task Statistics", "Filter Options")

        AlertDialog.Builder(this)
            .setTitle("Menu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> confirmClearAllTasks()
                    1 -> showTaskStatistics()
                    2 -> showFilterOptions()
                }
            }
            .show()
    }

    private fun showFilterOptions() {
        val options = arrayOf("Show All Tasks", "Show Pending Only", "Show Completed Only")
        val currentSelection = when (currentFilter) {
            TaskFilter.ALL -> 0
            TaskFilter.PENDING -> 1
            TaskFilter.COMPLETED -> 2
        }

        AlertDialog.Builder(this)
            .setTitle("Filter Tasks")
            .setSingleChoiceItems(options, currentSelection) { dialog, which ->
                when (which) {
                    0 -> selectFilter(TaskFilter.ALL)
                    1 -> selectFilter(TaskFilter.PENDING)
                    2 -> selectFilter(TaskFilter.COMPLETED)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmClearAllTasks() {
        AlertDialog.Builder(this)
            .setTitle("Clear All Tasks")
            .setMessage("Are you sure you want to delete all tasks? This action cannot be undone.")
            .setPositiveButton("Clear All") { _, _ ->
                if (tasksManager.clearAllTasks()) {
                    loadTasks()
                    showToast("All tasks cleared!")
                } else {
                    showToast("Failed to clear tasks")
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showTaskStatistics() {
        val stats = tasksManager.getTaskStatistics()
        val message = """
            📊 Task Statistics
            
            Total Tasks: ${stats.totalTasks}
            Completed Tasks: ${stats.completedTasks}
            Pending Tasks: ${stats.pendingTasks}
            High Priority Tasks: ${stats.highPriorityTasks}
            
            Completion Rate: ${if (stats.totalTasks > 0) "${(stats.completedTasks * 100) / stats.totalTasks}%" else "0%"}
            
            Current Filter: ${when (currentFilter) {
            TaskFilter.ALL -> "All Tasks"
            TaskFilter.PENDING -> "Pending Tasks"
            TaskFilter.COMPLETED -> "Completed Tasks"
        }}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Statistics")
            .setMessage(message)
            .setPositiveButton("OK", null)
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
        loadTasks() // Refresh tasks when returning to this screen
    }
}