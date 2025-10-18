package edu.sliit.myapplication

import android.animation.ObjectAnimator
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import edu.sliit.myapplication.adapter.TimeSlotsAdapter
import edu.sliit.myapplication.data.model.TimeSlot
import edu.sliit.myapplication.databinding.FragmentHomeBinding
import edu.sliit.myapplication.viewmodel.HomeViewModel

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private var quickBookingDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupAnimations()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupAnimations() {
        // Fade in animations for cards
        binding.headerCard.alpha = 0f
        binding.featureCardsLayout.alpha = 0f
        binding.statsLayout.alpha = 0f
        binding.quickActionsGrid.alpha = 0f
        binding.recentActivityCard.alpha = 0f

        binding.headerCard.animate()
            .alpha(1f)
            .setDuration(800)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        binding.featureCardsLayout.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(200)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        binding.statsLayout.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(400)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        binding.quickActionsGrid.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        binding.recentActivityCard.animate()
            .alpha(1f)
            .setDuration(800)
            .setStartDelay(800)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Floating animation for feature cards
        animateFeatureCards()
    }

    private fun animateFeatureCards() {
        // Floating animation for Quick Booking
        val translateY1 = ObjectAnimator.ofFloat(binding.quickBookingCard, "translationY", 0f, -10f).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        
        // Floating animation for Slot Management (offset timing)
        val translateY2 = ObjectAnimator.ofFloat(binding.slotManagementCard, "translationY", -10f, 0f).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        
        // Scale animations for icons
        val scaleIcons = listOf(
            binding.iconQuickBooking,
            binding.iconSlotManagement,
            binding.iconActive,
            binding.iconAvailable
        )
        
        scaleIcons.forEachIndexed { index, icon ->
            val scale = ObjectAnimator.ofFloat(icon, "scaleX", 1f, 1.1f).apply {
                duration = 1500
                startDelay = (index * 200).toLong()
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
            }
            val scaleY = ObjectAnimator.ofFloat(icon, "scaleY", 1f, 1.1f).apply {
                duration = 1500
                startDelay = (index * 200).toLong()
                repeatCount = ObjectAnimator.INFINITE
                repeatMode = ObjectAnimator.REVERSE
            }
            scale.start()
            scaleY.start()
        }
        
        translateY1.start()
        translateY2.start()
    }
    
    private fun showSlotManagementDialog() {
        val slotDialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_slot_management)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        val rvTimeSlots = slotDialog.findViewById<RecyclerView>(R.id.rvTimeSlots)
        val btnClose = slotDialog.findViewById<ImageView>(R.id.btnClose)
        val btnCancel = slotDialog.findViewById<Button>(R.id.btnCancel)
        val btnRefresh = slotDialog.findViewById<Button>(R.id.btnRefresh)

        // Setup RecyclerView
        val adapter = TimeSlotsAdapter { slot ->
            Toast.makeText(
                context,
                "Slot ${slot.id} - Status: ${if (slot.isActive) "Active" else "Inactive"}",
                Toast.LENGTH_SHORT
            ).show()
        }
        
        rvTimeSlots?.layoutManager = LinearLayoutManager(requireContext())
        rvTimeSlots?.adapter = adapter

        // Load slots
        viewModel.timeSlots.observe(viewLifecycleOwner) { slots ->
            adapter.submitList(slots)
        }

        btnClose?.setOnClickListener { slotDialog.dismiss() }
        btnCancel?.setOnClickListener { slotDialog.dismiss() }
        btnRefresh?.setOnClickListener {
            viewModel.refreshTimeSlots()
            Toast.makeText(context, "🔄 Slots refreshed", Toast.LENGTH_SHORT).show()
        }

        slotDialog.show()
    }

    private fun setupClickListeners() {
        // Quick Booking Feature
        binding.btnQuickBooking.setOnClickListener {
            showQuickBookingDialog()
        }
        
        // Slot Management Feature (Separate)
        binding.btnSlotManagement.setOnClickListener {
            showSlotManagementDialog()
        }
    }

    private fun observeViewModel() {
        viewModel.bookingResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                Toast.makeText(
                    context,
                    "✅ Booking created successfully! ID: ${response.bookingId}",
                    Toast.LENGTH_LONG
                ).show()
                quickBookingDialog?.dismiss()
            }.onFailure { error ->
                Toast.makeText(
                    context,
                    "❌ Booking failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showQuickBookingDialog() {
        quickBookingDialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_quick_booking)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        val rvTimeSlots = quickBookingDialog?.findViewById<RecyclerView>(R.id.rvTimeSlots)
        val btnClose = quickBookingDialog?.findViewById<ImageView>(R.id.btnClose)
        val btnCancel = quickBookingDialog?.findViewById<Button>(R.id.btnCancel)
        val btnRefresh = quickBookingDialog?.findViewById<Button>(R.id.btnRefresh)
        val progressBar = quickBookingDialog?.findViewById<ProgressBar>(R.id.progressBar)

        // Setup RecyclerView
        val adapter = TimeSlotsAdapter { slot ->
            onSlotSelected(slot)
        }
        
        rvTimeSlots?.layoutManager = LinearLayoutManager(requireContext())
        rvTimeSlots?.adapter = adapter

        // Observe time slots
        viewModel.timeSlots.observe(viewLifecycleOwner) { slots ->
            adapter.submitList(slots)
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
            rvTimeSlots?.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        btnClose?.setOnClickListener { quickBookingDialog?.dismiss() }
        btnCancel?.setOnClickListener { quickBookingDialog?.dismiss() }
        btnRefresh?.setOnClickListener { 
            viewModel.refreshTimeSlots()
            Toast.makeText(context, "🔄 Refreshing slots...", Toast.LENGTH_SHORT).show()
        }

        quickBookingDialog?.show()
    }

    private fun onSlotSelected(slot: TimeSlot) {
        if (!slot.isActive) {
            Toast.makeText(context, "⚠️ This slot is currently inactive", Toast.LENGTH_SHORT).show()
            return
        }

        if (!slot.isAvailable) {
            Toast.makeText(context, "⚠️ This slot is not available", Toast.LENGTH_SHORT).show()
            return
        }

        // Confirm booking
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirm Booking")
            .setMessage("Book ${slot.stationName} from ${slot.startTime} to ${slot.endTime}?")
            .setPositiveButton("Confirm") { _, _ ->
                viewModel.createQuickBooking(slot.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        quickBookingDialog?.dismiss()
        _binding = null
    }
}