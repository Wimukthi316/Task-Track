package com.example.task_tacker.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.task_tacker.R
import com.example.task_tacker.models.Lap

class LapAdapter(private var laps: MutableList<Lap>) :
    RecyclerView.Adapter<LapAdapter.LapViewHolder>() {

    private var fastestLapIndex = -1
    private var slowestLapIndex = -1

    class LapViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val lapNumber: TextView = itemView.findViewById(R.id.lapNumber)
        val lapTime: TextView = itemView.findViewById(R.id.lapTime)
        val totalTime: TextView = itemView.findViewById(R.id.totalTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LapViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_lap, parent, false)
        return LapViewHolder(view)
    }

    override fun onBindViewHolder(holder: LapViewHolder, position: Int) {
        val lap = laps[position]
        val actualPosition = laps.size - position // Reverse order for display

        holder.lapNumber.text = "Lap ${actualPosition}"
        holder.lapTime.text = lap.getFormattedLapTime()
        holder.totalTime.text = lap.getFormattedTotalTime()

        // Highlight fastest and slowest laps
        when (position) {
            fastestLapIndex -> {
                holder.lapTime.setTextColor(Color.parseColor("#4CAF50")) // Green for fastest
                holder.lapNumber.setTextColor(Color.parseColor("#4CAF50"))
            }
            slowestLapIndex -> {
                holder.lapTime.setTextColor(Color.parseColor("#F44336")) // Red for slowest
                holder.lapNumber.setTextColor(Color.parseColor("#F44336"))
            }
            else -> {
                holder.lapTime.setTextColor(Color.parseColor("#2196F3")) // Blue default
                holder.lapNumber.setTextColor(Color.parseColor("#000000")) // Black default
            }
        }
    }

    override fun getItemCount(): Int = laps.size

    fun addLap(lap: Lap) {
        laps.add(0, lap) // Add to beginning for reverse chronological order
        updateFastestSlowestIndices()
        notifyItemInserted(0)
        notifyItemRangeChanged(0, laps.size)
    }

    fun clearLaps() {
        laps.clear()
        fastestLapIndex = -1
        slowestLapIndex = -1
        notifyDataSetChanged()
    }

    private fun updateFastestSlowestIndices() {
        if (laps.size < 2) {
            fastestLapIndex = -1
            slowestLapIndex = -1
            return
        }

        var fastestTime = Long.MAX_VALUE
        var slowestTime = 0L
        fastestLapIndex = -1
        slowestLapIndex = -1

        laps.forEachIndexed { index, lap ->
            if (lap.lapTime < fastestTime) {
                fastestTime = lap.lapTime
                fastestLapIndex = index
            }
            if (lap.lapTime > slowestTime) {
                slowestTime = lap.lapTime
                slowestLapIndex = index
            }
        }
    }

    fun getFastestLap(): Lap? {
        return if (laps.isNotEmpty()) {
            laps.minByOrNull { it.lapTime }
        } else null
    }

    fun getAverageLapTime(): Long {
        return if (laps.isNotEmpty()) {
            laps.map { it.lapTime }.average().toLong()
        } else 0L
    }
}