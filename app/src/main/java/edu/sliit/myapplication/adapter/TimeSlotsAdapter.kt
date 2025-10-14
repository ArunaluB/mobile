package edu.sliit.myapplication.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.sliit.myapplication.R
import edu.sliit.myapplication.data.model.TimeSlot

class TimeSlotsAdapter(
    private val onSlotClick: (TimeSlot) -> Unit
) : ListAdapter<TimeSlot, TimeSlotsAdapter.SlotViewHolder>(SlotDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_time_slot, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvStationName: TextView = itemView.findViewById(R.id.tvStationName)
        private val tvTimeRange: TextView = itemView.findViewById(R.id.tvTimeRange)
        private val tvSlotId: TextView = itemView.findViewById(R.id.tvSlotId)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val statusBadge: LinearLayout = itemView.findViewById(R.id.statusBadge)

        fun bind(slot: TimeSlot) {
            tvStationName.text = slot.stationName
            tvTimeRange.text = "${slot.startTime} - ${slot.endTime}"
            tvSlotId.text = "Slot ID: ${slot.id}"

            // Update status badge
            if (slot.isActive) {
                tvStatus.text = "Active"
                statusBadge.setBackgroundColor(Color.parseColor("#4CAF50"))
            } else {
                tvStatus.text = "Inactive"
                statusBadge.setBackgroundColor(Color.parseColor("#9E9E9E"))
            }

            // Enable/disable click based on availability
            itemView.isEnabled = slot.isAvailable && slot.isActive
            itemView.alpha = if (slot.isAvailable && slot.isActive) 1.0f else 0.5f

            if (slot.isAvailable && slot.isActive) {
                itemView.setOnClickListener {
                    onSlotClick(slot)
                }
            } else {
                itemView.setOnClickListener(null)
            }
        }
    }

    class SlotDiffCallback : DiffUtil.ItemCallback<TimeSlot>() {
        override fun areItemsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TimeSlot, newItem: TimeSlot): Boolean {
            return oldItem == newItem
        }
    }
}
