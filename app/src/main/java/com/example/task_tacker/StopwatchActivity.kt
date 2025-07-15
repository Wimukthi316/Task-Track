package com.example.task_tacker

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.task_tacker.adapters.LapAdapter
import com.example.task_tacker.models.Lap
import com.google.android.material.button.MaterialButton

class StopwatchActivity : AppCompatActivity() {

    private lateinit var backButton: ImageView
    private lateinit var menuButton: ImageView
    private lateinit var statusText: TextView
    private lateinit var hoursText: TextView
    private lateinit var minutesText: TextView
    private lateinit var secondsText: TextView
    private lateinit var millisecondsText: TextView
    private lateinit var resetButton: MaterialButton
    private lateinit var startStopButton: MaterialButton
    private lateinit var stopButton: MaterialButton
    private lateinit var lapButton: MaterialButton
    private lateinit var lapTimesRecyclerView: RecyclerView
    private lateinit var totalLapsNumber: TextView
    private lateinit var fastestLapTime: TextView
    private lateinit var averageLapTime: TextView

    private lateinit var lapAdapter: LapAdapter
    private var handler = Handler(Looper.getMainLooper())
    private var startTime = 0L
    private var elapsedTime = 0L
    private var lastLapTime = 0L
    private var isRunning = false
    private var isPaused = false
    private var isStopped = true

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                elapsedTime = System.currentTimeMillis() - startTime
                updateTimeDisplay(elapsedTime)
                handler.postDelayed(this, 10) // Update every 10ms for smooth animation
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_stopwatch)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        updateUI()
    }

    private fun initializeViews() {
        backButton = findViewById(R.id.backButton)
        menuButton = findViewById(R.id.menuButton)
        statusText = findViewById(R.id.statusText)
        hoursText = findViewById(R.id.hoursText)
        minutesText = findViewById(R.id.minutesText)
        secondsText = findViewById(R.id.secondsText)
        millisecondsText = findViewById(R.id.millisecondsText)
        resetButton = findViewById(R.id.resetButton)
        startStopButton = findViewById(R.id.startStopButton)
        stopButton = findViewById(R.id.stopButton)
        lapButton = findViewById(R.id.lapButton)
        lapTimesRecyclerView = findViewById(R.id.lapTimesRecyclerView)
        totalLapsNumber = findViewById(R.id.totalLapsNumber)
        fastestLapTime = findViewById(R.id.fastestLapTime)
        averageLapTime = findViewById(R.id.averageLapTime)
    }

    private fun setupRecyclerView() {
        lapAdapter = LapAdapter(mutableListOf())
        lapTimesRecyclerView.layoutManager = LinearLayoutManager(this)
        lapTimesRecyclerView.adapter = lapAdapter
    }

    private fun setupClickListeners() {
        backButton.setOnClickListener {
            navigateBack()
        }

        menuButton.setOnClickListener {
            showMenuDialog()
        }

        startStopButton.setOnClickListener {
            toggleStopwatch()
        }

        stopButton.setOnClickListener {
            stopStopwatch()
        }

        resetButton.setOnClickListener {
            resetStopwatch()
        }

        lapButton.setOnClickListener {
            recordLap()
        }
    }

    private fun toggleStopwatch() {
        if (isStopped) {
            // Start from beginning
            startStopwatch()
        } else if (isRunning) {
            // Pause
            pauseStopwatch()
        } else if (isPaused) {
            // Resume
            resumeStopwatch()
        }
    }

    private fun startStopwatch() {
        startTime = System.currentTimeMillis()
        lastLapTime = 0L
        isRunning = true
        isPaused = false
        isStopped = false
        handler.post(updateRunnable)
        updateUI()
        showToast("Stopwatch started!")
    }

    private fun pauseStopwatch() {
        isRunning = false
        isPaused = true
        isStopped = false
        handler.removeCallbacks(updateRunnable)
        updateUI()
        showToast("Stopwatch paused")
    }

    private fun resumeStopwatch() {
        startTime = System.currentTimeMillis() - elapsedTime
        isRunning = true
        isPaused = false
        isStopped = false
        handler.post(updateRunnable)
        updateUI()
        showToast("Stopwatch resumed")
    }

    private fun stopStopwatch() {
        if (!isStopped) {
            AlertDialog.Builder(this)
                .setTitle("Stop Stopwatch")
                .setMessage("Are you sure you want to stop the stopwatch? The time will be preserved but you can only reset or view results.")
                .setPositiveButton("Stop") { _, _ ->
                    performStop()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun performStop() {
        isRunning = false
        isPaused = false
        isStopped = true
        handler.removeCallbacks(updateRunnable)
        updateUI()
        showToast("Stopwatch stopped - Final time: ${formatTime(elapsedTime)}")
    }

    private fun resetStopwatch() {
        if (!isStopped && (isRunning || isPaused)) {
            AlertDialog.Builder(this)
                .setTitle("Reset Stopwatch")
                .setMessage("Are you sure you want to reset the stopwatch? All lap data will be lost.")
                .setPositiveButton("Reset") { _, _ ->
                    performReset()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            performReset()
        }
    }

    private fun performReset() {
        isRunning = false
        isPaused = false
        isStopped = true
        elapsedTime = 0L
        lastLapTime = 0L
        handler.removeCallbacks(updateRunnable)
        updateTimeDisplay(0L)
        lapAdapter.clearLaps()
        updateStatistics()
        updateUI()
        showToast("Stopwatch reset")
    }

    private fun recordLap() {
        if (!isRunning) return

        val currentTime = elapsedTime
        val lapTime = currentTime - lastLapTime
        val lapNumber = lapAdapter.itemCount + 1

        val lap = Lap(
            lapNumber = lapNumber,
            lapTime = lapTime,
            totalTime = currentTime
        )

        lapAdapter.addLap(lap)
        lastLapTime = currentTime
        updateStatistics()

        // Scroll to top to show latest lap
        lapTimesRecyclerView.scrollToPosition(0)

        showToast("Lap $lapNumber recorded!")
    }

    private fun updateTimeDisplay(timeInMillis: Long) {
        val hours = timeInMillis / 3600000
        val minutes = (timeInMillis % 3600000) / 60000
        val seconds = (timeInMillis % 60000) / 1000
        val milliseconds = (timeInMillis % 1000) / 10

        hoursText.text = String.format("%02d", hours)
        minutesText.text = String.format("%02d", minutes)
        secondsText.text = String.format("%02d", seconds)
        millisecondsText.text = String.format(".%02d", milliseconds)
    }

    private fun updateUI() {
        when {
            isRunning -> {
                statusText.text = "Running..."
                startStopButton.text = "Pause"
                startStopButton.setIconResource(R.drawable.baseline_pause_24)
                stopButton.isEnabled = true
                lapButton.isEnabled = true
                resetButton.isEnabled = true
            }
            isPaused -> {
                statusText.text = "Paused"
                startStopButton.text = "Resume"
                startStopButton.setIconResource(R.drawable.baseline_play_arrow_24)
                stopButton.isEnabled = true
                lapButton.isEnabled = false
                resetButton.isEnabled = true
            }
            isStopped && elapsedTime > 0 -> {
                statusText.text = "Stopped - Final: ${formatTime(elapsedTime)}"
                startStopButton.text = "Start New"
                startStopButton.setIconResource(R.drawable.baseline_play_arrow_24)
                stopButton.isEnabled = false
                lapButton.isEnabled = false
                resetButton.isEnabled = true
            }
            else -> {
                statusText.text = "Ready to start"
                startStopButton.text = "Start"
                startStopButton.setIconResource(R.drawable.baseline_play_arrow_24)
                stopButton.isEnabled = false
                lapButton.isEnabled = false
                resetButton.isEnabled = (elapsedTime > 0)
            }
        }
    }

    private fun updateStatistics() {
        val totalLaps = lapAdapter.itemCount
        totalLapsNumber.text = totalLaps.toString()

        if (totalLaps > 0) {
            val fastest = lapAdapter.getFastestLap()
            val average = lapAdapter.getAverageLapTime()

            fastestLapTime.text = fastest?.getFormattedLapTime() ?: "--:--"
            averageLapTime.text = formatTime(average)
        } else {
            fastestLapTime.text = "--:--"
            averageLapTime.text = "--:--"
        }
    }

    private fun formatTime(timeInMillis: Long): String {
        val minutes = timeInMillis / 60000
        val seconds = (timeInMillis % 60000) / 1000
        val milliseconds = (timeInMillis % 1000) / 10
        return String.format("%02d:%02d.%02d", minutes, seconds, milliseconds)
    }

    private fun showMenuDialog() {
        val options = arrayOf(
            "Session Summary",
            "Clear All Laps"
        )

        AlertDialog.Builder(this)
            .setTitle("Stopwatch Menu")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> showSessionSummary()
                    1 -> clearAllLaps()
                }
            }
            .show()
    }

    private fun showSessionSummary() {
        val totalLaps = lapAdapter.itemCount
        val totalTime = formatTime(elapsedTime)
        val fastest = lapAdapter.getFastestLap()
        val average = lapAdapter.getAverageLapTime()

        val summary = """
            ⏱️ Session Summary
            
            Total Time: $totalTime
            Total Laps: $totalLaps
            
            ${if (fastest != null) "Fastest Lap: ${fastest.getFormattedLapTime()}" else "No laps recorded"}
            ${if (totalLaps > 0) "Average Lap: ${formatTime(average)}" else ""}
            
            Status: ${when {
            isRunning -> "Running"
            isPaused -> "Paused"
            isStopped && elapsedTime > 0 -> "Stopped"
            else -> "Ready"
        }}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Session Summary")
            .setMessage(summary)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun clearAllLaps() {
        if (lapAdapter.itemCount == 0) {
            showToast("No laps to clear")
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Clear All Laps")
            .setMessage("Are you sure you want to clear all lap times? This action cannot be undone.")
            .setPositiveButton("Clear") { _, _ ->
                lapAdapter.clearLaps()
                lastLapTime = 0L
                updateStatistics()
                showToast("All laps cleared!")
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun navigateBack() {
        if (isRunning) {
            AlertDialog.Builder(this)
                .setTitle("Stopwatch Running")
                .setMessage("The stopwatch is currently running. Do you want to pause it and go back?")
                .setPositiveButton("Pause & Go Back") { _, _ ->
                    pauseStopwatch()
                    finish()
                }
                .setNegativeButton("Keep Running") { _, _ ->
                    finish() // Let it run in background
                }
                .setNeutralButton("Cancel", null)
                .show()
        } else {
            finish()
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateRunnable)
    }

    override fun onPause() {
        super.onPause()
        // Keep running in background
    }

    override fun onResume() {
        super.onResume()
        // Resume UI updates if stopwatch is running
        if (isRunning) {
            handler.post(updateRunnable)
        }
    }
}