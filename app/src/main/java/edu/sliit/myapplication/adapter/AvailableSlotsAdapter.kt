package edu.sliit.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import edu.sliit.myapplication.R
import edu.sliit.myapplication.data.model.AvailableSlot

class AvailableSlotsAdapter(
    private val onSlotClick: (AvailableSlot) -> Unit
) : ListAdapter<AvailableSlot, AvailableSlotsAdapter.SlotViewHolder>(SlotDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_available_slot, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        val slot = getItem(position)
        holder.bind(slot)
    }

    inner class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val card: MaterialCardView = itemView.findViewById(R.id.slotCard)
        private val tvSlotLabel: TextView = itemView.findViewById(R.id.tvSlotLabel)
        private val tvSlotId: TextView = itemView.findViewById(R.id.tvSlotId)
        private val tvAvailability: TextView = itemView.findViewById(R.id.tvAvailability)

        fun bind(slot: AvailableSlot) {
            tvSlotLabel.text = slot.label
            tvSlotId.text = "ID: ${slot.slotId}"
            
            if (slot.available) {
                tvAvailability.text = "✓ Available"
                tvAvailability.setTextColor(itemView.context.getColor(android.R.color.holo_green_dark))
                card.alpha = 1.0f
                card.setOnClickListener { onSlotClick(slot) }
            } else {
                tvAvailability.text = "✗ Not Available"
                tvAvailability.setTextColor(itemView.context.getColor(android.R.color.holo_red_dark))
                card.alpha = 0.5f
                card.setOnClickListener(null)
            }
        }
    }

    private class SlotDiffCallback : DiffUtil.ItemCallback<AvailableSlot>() {
        override fun areItemsTheSame(oldItem: AvailableSlot, newItem: AvailableSlot): Boolean {
            return oldItem.slotId == newItem.slotId
        }

        override fun areContentsTheSame(oldItem: AvailableSlot, newItem: AvailableSlot): Boolean {
            return oldItem == newItem
        }
    }
}
