package edu.sliit.myapplication

import android.animation.ObjectAnimator
import android.app.Dialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.newdeveopmen.BookingHistory
import com.example.newdeveopmen.ScanHistoryRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import edu.sliit.myapplication.adapter.AvailableSlotsAdapter
import edu.sliit.myapplication.adapter.SlotManagementAdapter
import edu.sliit.myapplication.adapter.TimeSlotsAdapter
import edu.sliit.myapplication.data.model.TimeSlot
import edu.sliit.myapplication.databinding.FragmentHomeBinding
import edu.sliit.myapplication.utils.UserPreferences
import edu.sliit.myapplication.viewmodel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private var quickBookingDialog: Dialog? = null
    private var bookingSuccessDialog: Dialog? = null
    private var slotManagementDialog: Dialog? = null
    private lateinit var userPreferences: UserPreferences
    private lateinit var historyRepository: ScanHistoryRepository
    private var currentBookingId: String? = null
    private var allSlots: List<edu.sliit.myapplication.data.model.SlotDetail> = emptyList()
    
    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        userPreferences = UserPreferences(requireContext())
        historyRepository = ScanHistoryRepository(requireContext())
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
        
        // Rotation animation for header icon
        val rotate = ObjectAnimator.ofFloat(binding.headerIcon, "rotation", 0f, 360f).apply {
            duration = 10000
            repeatCount = ObjectAnimator.INFINITE
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
        rotate.start()
    }
    
    private fun showSlotManagementDialog() {
        val loginResponse = userPreferences.getLoginResponse()
        val stationId = loginResponse?.userData?.stationId
        
        Log.d(TAG, "========== Slot Management Dialog ==========")
        Log.d(TAG, "Station ID from UserPreferences: $stationId")
        Log.d(TAG, "Login Response: ${loginResponse?.userData}")
        
        if (stationId == null) {
            Log.e(TAG, "❌ Station ID is null!")
            Toast.makeText(context, "⚠️ Station ID not found. Please login again.", Toast.LENGTH_LONG).show()
            return
        }
        
        Log.d(TAG, "✓ Station ID found: $stationId")
        
        slotManagementDialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_slot_management_v2)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent)
        }

        val rvSlots = slotManagementDialog?.findViewById<RecyclerView>(R.id.rvSlots)
        val btnClose = slotManagementDialog?.findViewById<ImageView>(R.id.btnClose)
        val btnClose2 = slotManagementDialog?.findViewById<MaterialButton>(R.id.btnClose2)
        val btnRefresh = slotManagementDialog?.findViewById<MaterialButton>(R.id.btnRefresh)
        val progressBar = slotManagementDialog?.findViewById<ProgressBar>(R.id.progressBar)
        val emptyState = slotManagementDialog?.findViewById<View>(R.id.emptyState)
        val tvStationInfo = slotManagementDialog?.findViewById<TextView>(R.id.tvStationInfo)
        val tvTotalCount = slotManagementDialog?.findViewById<TextView>(R.id.tvTotalCount)
        val tvAvailableCount = slotManagementDialog?.findViewById<TextView>(R.id.tvAvailableCount)
        val tvActiveCount = slotManagementDialog?.findViewById<TextView>(R.id.tvActiveCount)
        
        // Filter chips
        val chipAll = slotManagementDialog?.findViewById<Chip>(R.id.chipAll)
        val chipAvailable = slotManagementDialog?.findViewById<Chip>(R.id.chipAvailable)
        val chipActive = slotManagementDialog?.findViewById<Chip>(R.id.chipActive)

        // Setup RecyclerView with new adapter
        val adapter = SlotManagementAdapter { slot, newActiveState ->
            // User toggled the switch
            // If slot is available (true) and user turns OFF → make unavailable (active: false)
            // If slot is unavailable (false) and user turns ON → make available (active: true)
            Log.d(TAG, "Toggle clicked for slot ${slot.slotId}")
            Log.d(TAG, "Current available: ${slot.available}, New switch state: $newActiveState")
            viewModel.toggleSlotStatus(stationId, slot.slotId, slot.available)
        }
        
        rvSlots?.layoutManager = LinearLayoutManager(requireContext())
        rvSlots?.adapter = adapter

        // Create observers
        val slotStatusObserver = androidx.lifecycle.Observer<List<edu.sliit.myapplication.data.model.SlotDetail>> { slots ->
            allSlots = slots
            
            if (slots.isEmpty()) {
                rvSlots?.visibility = View.GONE
                emptyState?.visibility = View.VISIBLE
                Log.d(TAG, "Slot Management: No slots available")
            } else {
                rvSlots?.visibility = View.VISIBLE
                emptyState?.visibility = View.GONE
                adapter.submitList(slots)
                
                // Update stats
                val availableCount = slots.count { it.available }
                val unavailableCount = slots.count { !it.available }
                
                tvTotalCount?.text = slots.size.toString()
                tvAvailableCount?.text = availableCount.toString()
                tvActiveCount?.text = unavailableCount.toString()  // Show unavailable count
                
                Log.d(TAG, "Slot Management: Displaying ${slots.size} slots (Available: $availableCount, Unavailable: $unavailableCount)")
            }
        }
        
        val stationNameObserver = androidx.lifecycle.Observer<String> { stationName ->
            tvStationInfo?.text = stationName
            Log.d(TAG, "Slot Management: Station name - $stationName")
        }

        val loadingObserver = androidx.lifecycle.Observer<Boolean> { isLoading ->
            progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
            Log.d(TAG, "Slot Management: Loading - $isLoading")
        }
        
        val slotUpdateObserver = androidx.lifecycle.Observer<Result<edu.sliit.myapplication.data.model.SlotDetail>> { result ->
            result.onSuccess { updatedSlot ->
                val status = if (updatedSlot.active) "activated" else "deactivated"
                Toast.makeText(
                    context,
                    "✅ Slot ${updatedSlot.slotId} $status successfully!",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d(TAG, "Slot Management: Slot ${updatedSlot.slotId} $status")
            }.onFailure { error ->
                Toast.makeText(
                    context,
                    "❌ Failed to update slot: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
                Log.e(TAG, "Slot Management: Update failed - ${error.message}")
            }
        }
        
        // Attach observers
        viewModel.slotStatus.observe(viewLifecycleOwner, slotStatusObserver)
        viewModel.slotStationName.observe(viewLifecycleOwner, stationNameObserver)
        viewModel.isLoading.observe(viewLifecycleOwner, loadingObserver)
        viewModel.slotUpdateResult.observe(viewLifecycleOwner, slotUpdateObserver)
        
        // Filter functionality
        chipAll?.setOnClickListener {
            adapter.submitList(allSlots)
            Log.d(TAG, "Slot Management: Filter - All (${allSlots.size} slots)")
        }
        
        chipAvailable?.setOnClickListener {
            val filtered = allSlots.filter { it.available }
            adapter.submitList(filtered)
            Log.d(TAG, "Slot Management: Filter - Available (${filtered.size} slots)")
        }
        
        chipActive?.setOnClickListener {
            val filtered = allSlots.filter { it.active }
            adapter.submitList(filtered)
            Log.d(TAG, "Slot Management: Filter - Active (${filtered.size} slots)")
        }

        // Clean up observers on dismiss
        val dismissListener = {
            viewModel.slotStatus.removeObserver(slotStatusObserver)
            viewModel.slotStationName.removeObserver(stationNameObserver)
            viewModel.isLoading.removeObserver(loadingObserver)
            viewModel.slotUpdateResult.removeObserver(slotUpdateObserver)
            Log.d(TAG, "Slot Management: Dialog dismissed, observers removed")
        }

        btnClose?.setOnClickListener { 
            slotManagementDialog?.dismiss()
            dismissListener()
        }
        btnClose2?.setOnClickListener { 
            slotManagementDialog?.dismiss()
            dismissListener()
        }
        btnRefresh?.setOnClickListener { 
            viewModel.fetchSlotStatus(stationId)
            Toast.makeText(context, "🔄 Refreshing slots...", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "Slot Management: Refresh requested")
        }
        
        slotManagementDialog?.setOnDismissListener {
            dismissListener()
        }

        // Initial load
        Log.d(TAG, "Slot Management: Fetching slot status for station $stationId")
        viewModel.fetchSlotStatus(stationId)
        slotManagementDialog?.show()
    }

    private fun setupClickListeners() {
        // Quick Booking Feature - Updated Flow
        binding.btnQuickBooking.setOnClickListener {
            // Get station ID from local storage
            val loginResponse = userPreferences.getLoginResponse()
            val stationId = loginResponse?.userData?.stationId
            
            if (stationId != null) {
                // Fetch available slots from API
                viewModel.fetchAvailableSlots(stationId)
                showQuickBookingDialog()
            } else {
                Toast.makeText(context, "⚠️ Station ID not found. Please login again.", Toast.LENGTH_LONG).show()
            }
        }
        
        // Slot Management Feature (Separate)
        binding.btnSlotManagement.setOnClickListener {
            showSlotManagementDialog()
        }
    }

    private fun observeViewModel() {
        // Observe old booking result (keeping for backwards compatibility)
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
        
        // Observe new booking result
        viewModel.bookingResult2.observe(viewLifecycleOwner) { result ->
            result.onSuccess { response ->
                currentBookingId = response.id
                
                // Save booking to history
                saveBookingToHistory(response)
                
                quickBookingDialog?.dismiss()
                showBookingSuccessDialog(response.id, response.status, response.slotId)
            }.onFailure { error ->
                Toast.makeText(
                    context,
                    "❌ Booking failed: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
        
        // Observe start booking result
        viewModel.startBookingResult.observe(viewLifecycleOwner) { result ->
            result.onSuccess {
                // Update history status to "Started" or "In-Progress"
                currentBookingId?.let { bookingId ->
                    updateHistoryStatus(bookingId, status = "Started")
                }
                
                Toast.makeText(
                    context,
                    "✅ Booking started successfully!",
                    Toast.LENGTH_LONG
                ).show()
                bookingSuccessDialog?.dismiss()
            }.onFailure { error ->
                Toast.makeText(
                    context,
                    "❌ Failed to start booking: ${error.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    
    private fun saveBookingToHistory(response: edu.sliit.myapplication.data.model.QuickBookingResponse2) {
        try {
            // Get station name from login response
            val loginResponse = userPreferences.getLoginResponse()
            val stationName = loginResponse?.userData?.stationName ?: "Unknown Station"
            
            val bookingHistory = BookingHistory(
                reservationId = response.id,
                stationId = response.stationId,
                stationName = stationName,
                status = response.status,
                ownerNic = response.ownerNic,
                startTime = formatTime(response.startUtc),
                endTime = formatTime(response.endUtc),
                scanTime = System.currentTimeMillis(),
                isCompleted = false,
                isCancelled = false
            )
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    historyRepository.insertHistory(bookingHistory)
                    Log.d(TAG, "✓ Quick booking saved to history: ${response.id}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving quick booking to history", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing booking history", e)
        }
    }
    
    private fun updateHistoryStatus(bookingId: String, status: String) {
        try {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    // Get existing history
                    val allHistory = historyRepository.getAllHistory()
                    val existingBooking = allHistory.find { it.reservationId == bookingId }
                    
                    if (existingBooking != null) {
                        // Update with new status
                        val updatedBooking = existingBooking.copy(
                            status = status,
                            isCompleted = status.equals("Completed", ignoreCase = true),
                            isCancelled = status.equals("Cancelled", ignoreCase = true)
                        )
                        historyRepository.insertHistory(updatedBooking)
                        Log.d(TAG, "✓ History updated: $bookingId -> $status")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating history status", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateHistoryStatus", e)
        }
    }
    
    private fun formatTime(isoDateTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = inputFormat.parse(isoDateTime)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            // Fallback formatting
            try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                val outputFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
                val date = inputFormat.parse(isoDateTime)
                outputFormat.format(date!!)
            } catch (e2: Exception) {
                isoDateTime
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

        // Setup RecyclerView with new adapter
        val adapter = AvailableSlotsAdapter { slot ->
            onAvailableSlotSelected(slot.slotId)
        }
        
        rvTimeSlots?.layoutManager = LinearLayoutManager(requireContext())
        rvTimeSlots?.adapter = adapter

        // Update adapter with current available slots
        viewModel.availableSlots.value?.let { slots ->
            adapter.submitList(slots)
            Log.d(TAG, "Quick Booking: Displaying ${slots.size} available slots")
        }
        
        // Observe available slots for updates
        val slotsObserver = androidx.lifecycle.Observer<List<edu.sliit.myapplication.data.model.AvailableSlot>> { slots ->
            adapter.submitList(slots)
            Log.d(TAG, "Quick Booking: Updated with ${slots.size} available slots")
        }
        viewModel.availableSlots.observe(viewLifecycleOwner, slotsObserver)

        // Observe loading state
        val loadingObserver = androidx.lifecycle.Observer<Boolean> { isLoading ->
            progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
            rvTimeSlots?.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
        viewModel.isLoading.observe(viewLifecycleOwner, loadingObserver)

        btnClose?.setOnClickListener { 
            quickBookingDialog?.dismiss()
            viewModel.availableSlots.removeObserver(slotsObserver)
            viewModel.isLoading.removeObserver(loadingObserver)
        }
        btnCancel?.setOnClickListener { 
            quickBookingDialog?.dismiss()
            viewModel.availableSlots.removeObserver(slotsObserver)
            viewModel.isLoading.removeObserver(loadingObserver)
        }
        btnRefresh?.setOnClickListener { 
            val loginResponse = userPreferences.getLoginResponse()
            val stationId = loginResponse?.userData?.stationId
            if (stationId != null) {
                viewModel.fetchAvailableSlots(stationId)
                Toast.makeText(context, "🔄 Refreshing slots...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "⚠️ Station ID not found", Toast.LENGTH_SHORT).show()
            }
        }
        
        quickBookingDialog?.setOnDismissListener {
            viewModel.availableSlots.removeObserver(slotsObserver)
            viewModel.isLoading.removeObserver(loadingObserver)
        }

        quickBookingDialog?.show()
    }

    private fun onAvailableSlotSelected(slotId: String) {
        // Confirm booking
        android.app.AlertDialog.Builder(requireContext())
            .setTitle("Confirm Booking")
            .setMessage("Do you want to book this slot?")
            .setPositiveButton("Confirm") { _, _ ->
                val loginResponse = userPreferences.getLoginResponse()
                val stationId = loginResponse?.userData?.stationId
                
                if (stationId != null) {
                    viewModel.createQuickBooking2(stationId, slotId)
                } else {
                    Toast.makeText(context, "⚠️ Station ID not found", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showBookingSuccessDialog(bookingId: String, status: String, slotId: String) {
        bookingSuccessDialog = Dialog(requireContext()).apply {
            setContentView(R.layout.dialog_booking_success)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            window?.setBackgroundDrawableResource(android.R.color.transparent)
            setCancelable(false)
        }

        val tvBookingId = bookingSuccessDialog?.findViewById<TextView>(R.id.tvBookingId)
        val tvBookingStatus = bookingSuccessDialog?.findViewById<TextView>(R.id.tvBookingStatus)
        val tvSlotId = bookingSuccessDialog?.findViewById<TextView>(R.id.tvSlotId)
        val btnStartBooking = bookingSuccessDialog?.findViewById<MaterialButton>(R.id.btnStartBooking)
        val btnClose = bookingSuccessDialog?.findViewById<MaterialButton>(R.id.btnClose)

        tvBookingId?.text = bookingId
        tvBookingStatus?.text = status
        tvSlotId?.text = slotId

        btnStartBooking?.setOnClickListener {
            viewModel.startBooking(bookingId)
        }

        btnClose?.setOnClickListener {
            bookingSuccessDialog?.dismiss()
        }

        bookingSuccessDialog?.show()
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
        bookingSuccessDialog?.dismiss()
        slotManagementDialog?.dismiss()
        _binding = null
    }
}