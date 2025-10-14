package com.example.newdeveopmen

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.sliit.myapplication.R
import java.text.SimpleDateFormat
import java.util.*

class HistoryAdapter(
    private val onComplete: (String) -> Unit,
    private val onCancel: (String) -> Unit
) : ListAdapter<BookingHistory, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback()) {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scan_history, parent, false)
        return HistoryViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvReservationId: TextView = itemView.findViewById(R.id.tvReservationId)
        private val tvStationId: TextView = itemView.findViewById(R.id.tvStationId)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvTimeSlot: TextView = itemView.findViewById(R.id.tvTimeSlot)
        private val tvDateScanned: TextView = itemView.findViewById(R.id.tvDateScanned)
        private val tvTimeScanned: TextView = itemView.findViewById(R.id.tvTimeScanned)
        private val btnComplete: Button = itemView.findViewById(R.id.btnComplete)
        private val btnCancel: Button = itemView.findViewById(R.id.btnCancel)
        
        fun bind(history: BookingHistory) {
            tvReservationId.text = "ID: ${history.reservationId}"
            tvStationId.text = "Station: ${history.stationId}"
            tvStatus.text = getDisplayStatus(history)
            tvTimeSlot.text = "${history.startTime} - ${history.endTime}"
            
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            tvDateScanned.text = dateFormat.format(Date(history.scanTime))
            tvTimeScanned.text = "Scanned at ${timeFormat.format(Date(history.scanTime))}"
            
            // Set status color
            val statusColor = when {
                history.isCompleted -> R.color.status_completed
                history.isCancelled -> R.color.status_cancelled
                history.status.equals("In-Progress", ignoreCase = true) -> R.color.status_in_progress
                history.status.equals("Approved", ignoreCase = true) -> R.color.status_approved
                else -> R.color.status_pending
            }
            tvStatus.setTextColor(ContextCompat.getColor(itemView.context, statusColor))
            
            // Show/hide buttons based on status
            if (history.isCompleted || history.isCancelled) {
                btnComplete.visibility = View.GONE
                btnCancel.visibility = View.GONE
            } else {
                btnComplete.visibility = View.VISIBLE
                btnCancel.visibility = View.VISIBLE
                
                btnComplete.setOnClickListener {
                    onComplete(history.reservationId)
                }
                
                btnCancel.setOnClickListener {
                    onCancel(history.reservationId)
                }
            }
        }
        
        private fun getDisplayStatus(history: BookingHistory): String {
            return when {
                history.isCompleted -> "Completed"
                history.isCancelled -> "Cancelled"
                else -> history.status
            }
        }
    }
    
    class HistoryDiffCallback : DiffUtil.ItemCallback<BookingHistory>() {
        override fun areItemsTheSame(oldItem: BookingHistory, newItem: BookingHistory): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: BookingHistory, newItem: BookingHistory): Boolean {
            return oldItem == newItem
        }
    }
}
