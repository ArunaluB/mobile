package edu.sliit.myapplication.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import edu.sliit.myapplication.R
import edu.sliit.myapplication.data.model.SlotDetail

class SlotManagementAdapter(
    private val onToggleClick: (SlotDetail, Boolean) -> Unit
) : ListAdapter<SlotDetail, SlotManagementAdapter.SlotViewHolder>(SlotDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlotViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_slot_management, parent, false)
        return SlotViewHolder(view)
    }

    override fun onBindViewHolder(holder: SlotViewHolder, position: Int) {
        holder.bind(getItem(position), onToggleClick)
    }

    class SlotViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvSlotLabel: TextView = itemView.findViewById(R.id.tvSlotLabel)
        private val tvSlotId: TextView = itemView.findViewById(R.id.tvSlotId)
        private val tvAvailabilityStatus: TextView = itemView.findViewById(R.id.tvAvailabilityStatus)
        private val tvActiveStatus: TextView = itemView.findViewById(R.id.tvActiveStatus)
        private val statusIndicator: View = itemView.findViewById(R.id.statusIndicator)
        private val switchActive: SwitchMaterial = itemView.findViewById(R.id.switchActive)

        fun bind(slot: SlotDetail, onToggleClick: (SlotDetail, Boolean) -> Unit) {
            tvSlotLabel.text = slot.label
            tvSlotId.text = "ID: ${slot.slotId}"
            
            // Set availability status
            tvAvailabilityStatus.text = if (slot.available) "Available" else "Unavailable"
            tvAvailabilityStatus.setTextColor(
                if (slot.available) 
                    itemView.context.getColor(R.color.success)
                else 
                    itemView.context.getColor(R.color.error)
            )
            
            // Update status indicator color
            statusIndicator.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(
                    if (slot.available)
                        itemView.context.getColor(R.color.success)
                    else
                        itemView.context.getColor(R.color.error)
                )
            )
            
            // Set active status
            tvActiveStatus.text = if (slot.active) "Active" else "Inactive"
            
            // Set switch state based on availability
            switchActive.isChecked = slot.available
            
            // Handle switch toggle
            switchActive.setOnCheckedChangeListener(null) // Remove previous listener
            switchActive.setOnCheckedChangeListener { _, isChecked ->
                onToggleClick(slot, isChecked)
            }
        }
    }

    class SlotDiffCallback : DiffUtil.ItemCallback<SlotDetail>() {
        override fun areItemsTheSame(oldItem: SlotDetail, newItem: SlotDetail): Boolean {
            return oldItem.slotId == newItem.slotId
        }

        override fun areContentsTheSame(oldItem: SlotDetail, newItem: SlotDetail): Boolean {
            return oldItem == newItem
        }
    }
}
