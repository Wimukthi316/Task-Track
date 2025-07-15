package com.example.task_tacker.adapters

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.task_tacker.R
import com.example.task_tacker.models.Task
import com.example.task_tacker.models.TaskPriority
import com.google.android.material.button.MaterialButton

class TasksAdapter(
    private var tasks: MutableList<Task>,
    private val onTaskClick: (Task) -> Unit,
    private val onTaskEdit: (Task) -> Unit,
    private val onTaskChecked: (Task) -> Unit
) : RecyclerView.Adapter<TasksAdapter.TaskViewHolder>() {

    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val taskCheckbox: CheckBox = itemView.findViewById(R.id.taskCheckbox)
        val taskTitle: TextView = itemView.findViewById(R.id.taskTitle)
        val taskDescription: TextView = itemView.findViewById(R.id.taskDescription)
        val taskPriority: TextView = itemView.findViewById(R.id.taskPriority)
        val taskDate: TextView = itemView.findViewById(R.id.taskDate)
        val priorityIndicator: View = itemView.findViewById(R.id.priorityIndicator)
        val editTaskButton: MaterialButton = itemView.findViewById(R.id.editTaskButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]

        // Set task title
        holder.taskTitle.text = task.title

        // Handle description visibility and content
        if (task.description.isNotEmpty() && task.description.trim().isNotEmpty()) {
            holder.taskDescription.text = task.description
            holder.taskDescription.visibility = View.VISIBLE
        } else {
            holder.taskDescription.visibility = View.GONE
        }

        // Set priority
        holder.taskPriority.text = task.priority.displayName

        // Set date with better formatting
        if (task.isCompleted && task.completedAt != null) {
            holder.taskDate.text = formatCompletedDate(task.completedAt!!)
        } else {
            holder.taskDate.text = task.getFormattedCreatedDate()
        }

        // Set checkbox state
        holder.taskCheckbox.isChecked = task.isCompleted

        // Set priority indicator color
        val priorityColor = when (task.priority) {
            TaskPriority.HIGH -> Color.parseColor("#F44336")
            TaskPriority.MEDIUM -> Color.parseColor("#FF9800")
            TaskPriority.LOW -> Color.parseColor("#4CAF50")
        }
        holder.priorityIndicator.setBackgroundColor(priorityColor)

        // Handle completed task styling
        if (task.isCompleted) {
            // Strike through and dim completed tasks
            holder.taskTitle.paintFlags = holder.taskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.taskTitle.alpha = 0.6f
            holder.taskDescription.alpha = 0.5f
            holder.taskPriority.alpha = 0.6f
            holder.taskDate.alpha = 0.6f
            holder.editTaskButton.alpha = 0.6f
        } else {
            // Remove strike through for pending tasks
            holder.taskTitle.paintFlags = holder.taskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            holder.taskTitle.alpha = 1.0f
            holder.taskDescription.alpha = 0.8f
            holder.taskPriority.alpha = 1.0f
            holder.taskDate.alpha = 1.0f
            holder.editTaskButton.alpha = 1.0f
        }

        // Set click listeners
        holder.itemView.setOnClickListener {
            onTaskClick(task)
        }

        holder.editTaskButton.setOnClickListener {
            onTaskEdit(task)
        }

        holder.taskCheckbox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked != task.isCompleted) {
                onTaskChecked(task)
            }
        }
    }

    override fun getItemCount(): Int = tasks.size

    fun updateTasks(newTasks: List<Task>) {
        tasks.clear()
        tasks.addAll(newTasks)
        notifyDataSetChanged()
    }

    private fun formatCompletedDate(completedAt: String): String {
        return try {
            val formatter = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
            val date = formatter.parse(completedAt)
            val displayFormatter = java.text.SimpleDateFormat("MMM dd", java.util.Locale.getDefault())
            "Completed ${displayFormatter.format(date ?: java.util.Date())}"
        } catch (e: Exception) {
            "Completed"
        }
    }
}