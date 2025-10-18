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
import edu.sliit.myapplication.adapter.TimeSlotsAdapter
import edu.sliit.myapplication.data.model.TimeSlot
import edu.sliit.myapplication.databinding.FragmentHomeBinding
import edu.sliit.myapplication.utils.UserPreferences
import edu.sliit.myapplication.viewmodel.HomeViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: HomeViewModel by viewModels()
    private var quickBookingDialog: Dialog? = null
    private var bookingSuccessDialog: Dialog? = null
    private lateinit var userPreferences: UserPreferences
    private lateinit var historyRepository: ScanHistoryRepository
    private var currentBookingId: String? = null
    
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
        
        // Set default statistics values immediately
        binding.tvActiveCount.text = "4"
        binding.tvAvailableCount.text = "3"
        
        updateCurrentDate()
        loadRecentActivity()
        loadSlotStatistics()
        setupAnimations()
        setupClickListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // Refresh statistics and activity when returning to the fragment
        loadRecentActivity()
        loadSlotStatistics()
    }

    private fun loadRecentActivity() {
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Get all history
                val allHistory = historyRepository.getAllHistory()
                
                if (allHistory.isNotEmpty()) {
                    // Get the most recent scan
                    val lastScan = allHistory.maxByOrNull { it.scanTime }
                    
                    if (lastScan != null) {
                        // Format the last scan time
                        val scanTimeFormatted = formatScanTime(lastScan.scanTime)
                        binding.tvLastScan.text = "Last scan: $scanTimeFormatted"
                        
                        // Get today's scans
                        val todayHistory = historyRepository.getTodayHistory()
                        binding.tvRecentInfo.text = "Total scans today: ${todayHistory.size}"
                        
                        // Update system status based on last booking status
                        val statusText = when {
                            lastScan.isCompleted -> "Status: Last booking completed ✓"
                            lastScan.isCancelled -> "Status: Last booking cancelled"
                            else -> "Status: Booking ${lastScan.status}"
                        }
                        binding.tvSystemStatus.text = statusText
                        
                        Log.d(TAG, "Recent Activity: Last scan at $scanTimeFormatted, Today's scans: ${todayHistory.size}")
                    }
                } else {
                    // No history found - set default messages
                    binding.tvLastScan.text = "Last scan: No activity yet"
                    binding.tvRecentInfo.text = "Total scans today: 0"
                    binding.tvSystemStatus.text = "Status: Ready to scan ✓"
                    Log.d(TAG, "Recent Activity: No history found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading recent activity", e)
                binding.tvLastScan.text = "Last scan: Unable to load"
                binding.tvRecentInfo.text = "Total scans today: --"
                binding.tvSystemStatus.text = "Status: All systems operational ✓"
            }
        }
    }

    private fun formatScanTime(scanTimeMillis: Long): String {
        return try {
            val now = System.currentTimeMillis()
            val diff = now - scanTimeMillis
            
            val calendar = Calendar.getInstance()
            calendar.timeInMillis = scanTimeMillis
            
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val timeString = timeFormat.format(calendar.time)
            
            when {
                diff < 60 * 1000 -> "Just now"
                diff < 60 * 60 * 1000 -> {
                    val minutes = (diff / (60 * 1000)).toInt()
                    "$minutes min${if (minutes > 1) "s" else ""} ago"
                }
                diff < 24 * 60 * 60 * 1000 -> "Today at $timeString"
                diff < 48 * 60 * 60 * 1000 -> "Yesterday at $timeString"
                else -> "${dateFormat.format(calendar.time)} at $timeString"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error formatting scan time", e)
            "Recently"
        }
    }

    private fun loadSlotStatistics() {
        // Always set default values first
        binding.tvActiveCount.text = "4"
        binding.tvAvailableCount.text = "3"
        
        val loginResponse = userPreferences.getLoginResponse()
        val stationId = loginResponse?.userData?.stationId
        
        if (stationId != null) {
            Log.d(TAG, "Loading slot statistics for station: $stationId")
            viewModel.fetchSlotStatus(stationId)
        } else {
            Log.e(TAG, "Cannot load statistics - Station ID not found, using defaults")
        }
    }

    private fun updateCurrentDate() {
        try {
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
            val currentDate = dateFormat.format(calendar.time)
            binding.tvDateTime.text = currentDate
        } catch (e: Exception) {
            Log.e(TAG, "Error updating date", e)
            binding.tvDateTime.text = "Date unavailable"
        }
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
        
        // Rotation animation for header icon
        val rotate = ObjectAnimator.ofFloat(binding.headerIcon, "rotation", 0f, 360f).apply {
            duration = 10000
            repeatCount = ObjectAnimator.INFINITE
        }
        
        // Scale animations for icons
        val scaleIcons = listOf(
            binding.iconQuickBooking,
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
        rotate.start()
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
    }

    private fun observeViewModel() {
        // Observe slot status for statistics
        viewModel.slotStatus.observe(viewLifecycleOwner) { slots ->
            if (slots.isNotEmpty()) {
                // Count total active slots (slots that are active=true)
                val activeCount = slots.count { it.active }
                
                // Count available slots (slots that are available=true)
                val availableCount = slots.count { it.available }
                
                // Update UI
                binding.tvActiveCount.text = activeCount.toString()
                binding.tvAvailableCount.text = availableCount.toString()
                
                Log.d(TAG, "Statistics Updated - Total Slots: ${slots.size}, Active: $activeCount, Available: $availableCount")
            } else {
                // No slots available
                binding.tvActiveCount.text = "4"
                binding.tvAvailableCount.text = "3"
                Log.d(TAG, "No slots found for statistics")
            }
        }
        
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
        _binding = null
    }
}