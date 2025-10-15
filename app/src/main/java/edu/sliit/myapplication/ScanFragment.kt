package edu.sliit.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.integration.android.IntentIntegrator
import com.example.newdeveopmen.BookingHistory
import com.example.newdeveopmen.ScanHistoryRepository
import edu.sliit.myapplication.databinding.FragmentScanBinding
import edu.sliit.myapplication.utils.UserPreferences
import edu.sliit.myapplication.viewmodel.ScanViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ScanFragment : Fragment() {

    private var _binding: FragmentScanBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ScanViewModel by viewModels()
    private lateinit var historyRepository: ScanHistoryRepository
    private lateinit var userPreferences: UserPreferences
    private var currentReservationId: String = ""
    
    companion object {
        private const val TAG = "ScanFragment"
    }
    
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startQRScanner()
        } else {
            Toast.makeText(context, "Camera permission is required to scan QR codes", Toast.LENGTH_SHORT).show()
        }
    }
    
    private val qrScannerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val scanResult = IntentIntegrator.parseActivityResult(result.resultCode, result.data)
        if (scanResult != null && scanResult.contents != null) {
            viewModel.scanQRCode(scanResult.contents)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView called")
        _binding = FragmentScanBinding.inflate(inflater, container, false)
        Log.d(TAG, "Binding inflated successfully")
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated called")
        try {
            // Initialize Repository with SharedPreferences
            historyRepository = ScanHistoryRepository(requireContext())
            userPreferences = UserPreferences(requireContext())
            
            setupUI()
            Log.d(TAG, "UI setup completed")
            observeViewModel()
            Log.d(TAG, "ViewModel observers set up")
        } catch (e: Exception) {
            Log.e(TAG, "Error in onViewCreated", e)
            e.printStackTrace()
            Toast.makeText(context, "Error initializing scanner: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun setupUI() {
        binding.btnScanQr.setOnClickListener {
            checkCameraPermissionAndScan()
        }
        
        // Long press to show demo data
        binding.btnScanQr.setOnLongClickListener {
            showDemoData()
            true
        }
        
        binding.btnConfirmStart.setOnClickListener {
            // Send "Start" action to match API response
            Log.d(TAG, "Start button clicked")
            viewModel.confirmAction("Start")
        }
        
        binding.btnConfirmEnd.setOnClickListener {
            // Send "End" or "confirm-end" based on API
            Log.d(TAG, "End button clicked")
            viewModel.confirmAction("End")
        }
        
        binding.btnComplete.setOnClickListener {
            // Call complete API through ViewModel
            Log.d(TAG, "Complete button clicked")
            viewModel.confirmAction("Complete")
        }
        
        binding.btnCancel.setOnClickListener {
            // Call cancel API through ViewModel
            Log.d(TAG, "Cancel button clicked")
            viewModel.confirmAction("Cancel")
        }
        
        binding.btnScanAnother.setOnClickListener {
            resetUI()
        }
    }
    
    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isAdded && _binding != null) {
                showLoading(isLoading)
            }
        }
        
        viewModel.scanResult.observe(viewLifecycleOwner) { result ->
            if (!isAdded || _binding == null) return@observe
            
            result?.let {
                if (it.isSuccess) {
                    val response = it.getOrNull()
                    if (response != null) {
                        displayBookingDetails(response)
                    } else {
                        showError("No data received from server")
                    }
                } else {
                    val errorMessage = it.exceptionOrNull()?.message ?: "Scan failed"
                    Log.e(TAG, "Scan error: $errorMessage")
                    
                    // Handle 401 Unauthorized - Session Expired
                    if (errorMessage.contains("Unauthorized", ignoreCase = true) || 
                        errorMessage.contains("401", ignoreCase = true)) {
                        handleSessionExpired()
                        return@observe
                    }
                    
                    // Handle specific error types
                    val userMessage = when {
                        errorMessage.contains("already started", ignoreCase = true) -> 
                            "⚠️ This booking has already been started"
                        errorMessage.contains("already completed", ignoreCase = true) -> 
                            "✅ This booking is already completed"
                        errorMessage.contains("already cancelled", ignoreCase = true) -> 
                            "🚫 This booking has been cancelled"
                        errorMessage.contains("Invalid state", ignoreCase = true) -> 
                            "⚠️ Cannot perform this action - Invalid booking state"
                        errorMessage.contains("Cannot connect", ignoreCase = true) -> 
                            "📡 Cannot connect to server - Check your internet connection"
                        else -> errorMessage
                    }
                    showError(userMessage)
                }
            }
        }
        
        viewModel.confirmResult.observe(viewLifecycleOwner) { result ->
            if (!isAdded || _binding == null) return@observe
            
            result?.let {
                if (it.isSuccess) {
                    val response = it.getOrNull()
                    if (response != null) {
                        Log.d(TAG, "Confirm action successful - Status: ${response.status}, NextActions: ${response.nextActions}")
                        
                        // Update UI with new status
                        updateStatusUI(response.status, response.nextActions)
                        
                        // Update displayed status
                        binding.tvStatus.text = response.status
                        
                        // Update history in database
                        updateHistoryStatus(response)
                        
                        // Show success message based on status
                        val message = when (response.status.lowercase()) {
                            "started" -> "✅ Session started successfully!"
                            "completed" -> "✅ Booking completed successfully!"
                            "cancelled" -> "🚫 Booking cancelled"
                            else -> "Status updated successfully!"
                        }
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        
                        // Show success animation for completed status
                        if (response.status.equals("completed", ignoreCase = true)) {
                            binding.successAnimation.visibility = View.VISIBLE
                        }
                    }
                } else {
                    val errorMessage = it.exceptionOrNull()?.message ?: "Confirmation failed"
                    Log.e(TAG, "Confirm action error: $errorMessage")
                    
                    // Handle 401 Unauthorized - Session Expired
                    if (errorMessage.contains("Unauthorized", ignoreCase = true) || 
                        errorMessage.contains("401", ignoreCase = true)) {
                        handleSessionExpired()
                        return@observe
                    }
                    
                    // Handle specific conflict errors
                    val userMessage = when {
                        errorMessage.contains("already started", ignoreCase = true) -> {
                            "⚠️ Booking Already Started\nThis booking is already in progress. No need to start again."
                        }
                        errorMessage.contains("already completed", ignoreCase = true) -> {
                            "✅ Booking Already Completed\nThis booking was already completed earlier."
                        }
                        errorMessage.contains("already cancelled", ignoreCase = true) -> {
                            "🚫 Booking Already Cancelled\nThis booking has been cancelled and cannot be modified."
                        }
                        errorMessage.contains("Invalid state", ignoreCase = true) -> {
                            "⚠️ Invalid Action\nCannot perform this action in the current booking state."
                        }
                        errorMessage.contains("not found", ignoreCase = true) -> {
                            "❌ Booking Not Found\nThis booking ID does not exist."
                        }
                        errorMessage.contains("Cannot connect", ignoreCase = true) -> {
                            "📡 Connection Error\nCannot reach the server. Please check your internet connection."
                        }
                        else -> "❌ Error: $errorMessage"
                    }
                    showError(userMessage)
                }
            }
        }
    }
    
    private fun checkCameraPermissionAndScan() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                startQRScanner()
            }
            else -> {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }
    
    private fun startQRScanner() {
        try {
            val integrator = IntentIntegrator.forSupportFragment(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            integrator.setPrompt("Scan QR Code for Booking")
            integrator.setCameraId(0)
            integrator.setBeepEnabled(true)
            integrator.setBarcodeImageEnabled(false)
            integrator.setOrientationLocked(true)
            qrScannerLauncher.launch(integrator.createScanIntent())
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error starting camera: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun displayBookingDetails(response: edu.sliit.myapplication.data.model.ScanResponse) {
        try {
            Log.d(TAG, "displayBookingDetails called")
            Log.d(TAG, "Response - ID: ${response.id}, Status: ${response.status}, NextActions: ${response.nextActions}")
            
            if (!isAdded || _binding == null) {
                Log.w(TAG, "Fragment not attached or binding is null")
                return
            }
            
            binding.emptyState.visibility = View.GONE
            binding.bookingDetailsCard.visibility = View.VISIBLE
            
            // Display booking information with null safety
            binding.tvReservationId.text = response.id
            binding.tvStationId.text = response.stationId ?: "N/A"
            binding.tvStatus.text = response.status
            
            // Format and display times with error handling
            val startTime = formatDateTime(response.startUtc)
            val endTime = formatDateTime(response.endUtc)
            binding.tvStartTime.text = startTime
            binding.tvEndTime.text = endTime
            
            // Display time slot highlight
            binding.tvTimeSlot.text = "${formatTime(response.startUtc)} - ${formatTime(response.endUtc)}"
            binding.tvDate.text = formatDate(response.startUtc)
            
            // Display owner NIC if available
            if (!response.ownerNic.isNullOrEmpty()) {
                binding.tvOwnerNic.text = response.ownerNic
                binding.ownerNicLayout.visibility = View.VISIBLE
            } else {
                binding.ownerNicLayout.visibility = View.GONE
            }
            
            Log.d(TAG, "About to call updateStatusUI with status='${response.status}' and actions=${response.nextActions}")
            
            // Update status UI
            updateStatusUI(response.status, response.nextActions)
            
            // Save to local database
            saveScanToDatabase(response)
            
            Log.d(TAG, "Booking details displayed successfully: ${response.id}")
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying booking details", e)
            showError("Error displaying booking: ${e.message}")
        }
    }
    
    private fun saveScanToDatabase(response: edu.sliit.myapplication.data.model.ScanResponse) {
        try {
            val bookingHistory = BookingHistory(
                reservationId = response.id,
                stationId = response.stationId ?: "Unknown",
                status = response.status,
                ownerNic = response.ownerNic ?: "",
                startTime = formatTime(response.startUtc),
                endTime = formatTime(response.endUtc),
                scanTime = System.currentTimeMillis(),
                isCompleted = response.status.equals("completed", ignoreCase = true),
                isCancelled = response.status.equals("cancelled", ignoreCase = true)
            )
            
            currentReservationId = response.id
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    historyRepository.insertHistory(bookingHistory)
                    Log.d(TAG, "Scan saved to database: ${response.id}")
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving to database", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error preparing booking history", e)
        }
    }
    
    private fun updateStatusUI(status: String, nextActions: List<String>) {
        try {
            Log.d(TAG, "updateStatusUI called - Status: $status, NextActions: $nextActions")
            
            binding.statusBadge.text = status
            
            // Set status color with Material Design colors
            val statusColor = when (status.lowercase()) {
                "approved" -> R.color.status_approved
                "in-progress", "started" -> R.color.status_in_progress
                "completed" -> R.color.status_completed
                "cancelled" -> R.color.status_cancelled
                else -> R.color.status_pending
            }
            binding.statusBadge.setBackgroundResource(statusColor)
            
            // Show/hide action buttons based on next actions
            val hasActions = nextActions.isNotEmpty()
            binding.actionButtonsLayout.visibility = if (hasActions) View.VISIBLE else View.GONE
            
            Log.d(TAG, "Action buttons layout visibility: ${if (hasActions) "VISIBLE" else "GONE"}")
            
            // Handle different action types - Reset first
            binding.btnConfirmStart.visibility = View.GONE
            binding.btnConfirmEnd.visibility = View.GONE
            
            // Check each action and show appropriate button
            nextActions.forEach { action ->
                Log.d(TAG, "Processing action: '$action' (lowercase: '${action.lowercase()}')")
                
                when (action.lowercase()) {
                    "start", "confirm-start" -> {
                        binding.btnConfirmStart.visibility = View.VISIBLE
                        binding.btnConfirmStart.text = "▶ Start Session"
                        Log.d(TAG, "✓ Start button set to VISIBLE")
                    }
                    "end", "confirm-end", "complete" -> {
                        binding.btnConfirmEnd.visibility = View.VISIBLE
                        binding.btnConfirmEnd.text = "⏹ End Session"
                        Log.d(TAG, "✓ End button set to VISIBLE")
                    }
                    else -> {
                        Log.w(TAG, "⚠ Unknown action: '$action'")
                    }
                }
            }
            
            // Show complete/cancel buttons for in-progress bookings
            val isInProgress = status.equals("in-progress", ignoreCase = true) || 
                              status.equals("started", ignoreCase = true)
            binding.completeCancelLayout.visibility = if (isInProgress) View.VISIBLE else View.GONE
            
            Log.d(TAG, "Status UI updated successfully - Start visible: ${binding.btnConfirmStart.visibility == View.VISIBLE}, End visible: ${binding.btnConfirmEnd.visibility == View.VISIBLE}")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating status UI", e)
            e.printStackTrace()
        }
    }
    
    private fun updateHistoryStatus(response: edu.sliit.myapplication.data.model.ScanResponse) {
        try {
            if (currentReservationId.isEmpty()) {
                currentReservationId = response.id
            }
            
            val isCompleted = response.status.equals("completed", ignoreCase = true)
            val isCancelled = response.status.equals("cancelled", ignoreCase = true)
            
            Log.d(TAG, "Updating history - ID: $currentReservationId, Completed: $isCompleted, Cancelled: $isCancelled")
            
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    historyRepository.updateHistoryStatus(currentReservationId, isCompleted, isCancelled)
                    Log.d(TAG, "✓ History updated successfully")
                } catch (e: Exception) {
                    Log.e(TAG, "Error updating history", e)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in updateHistoryStatus", e)
        }
    }
    
    private fun resetUI() {
        viewModel.clearResults()
        binding.emptyState.visibility = View.VISIBLE
        binding.bookingDetailsCard.visibility = View.GONE
        binding.successAnimation.visibility = View.GONE
        currentReservationId = ""
    }
    
    private fun showDemoData() {
        // Demo data for testing without API - matches new response format
        val demoResponse = edu.sliit.myapplication.data.model.ScanResponse(
            id = "91f70ffe-f2a6-4327-993b-7b6b43c5a40e",
            ownerNic = "200300500600",
            stationId = "95d60eae-082e-4eab-bd6d-10218cb9ca8a",
            startUtc = "2025-10-15T17:43:43.412Z",
            endUtc = "2025-10-15T19:43:43.412Z",
            status = "Approved",
            startedUtc = null,
            endedUtc = null,
            nextActions = listOf("Start")
        )
        displayBookingDetails(demoResponse)
        Toast.makeText(context, "✅ Demo data loaded (Long press to test)", Toast.LENGTH_SHORT).show()
    }
    
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnScanQr.isEnabled = !isLoading
    }
    
    private fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
    
    private fun formatDateTime(isoDateTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(isoDateTime)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            isoDateTime
        }
    }
    
    private fun formatTime(isoDateTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(isoDateTime)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            isoDateTime
        }
    }
    
    private fun formatDate(isoDateTime: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val outputFormat = SimpleDateFormat("EEEE, MMM dd, yyyy", Locale.getDefault())
            val date = inputFormat.parse(isoDateTime)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            isoDateTime
        }
    }
    
    private fun handleSessionExpired() {
        Log.e(TAG, "Session expired - redirecting to login")
        
        // Clear user session
        userPreferences.clearUserData()
        
        // Show error message
        Toast.makeText(
            context,
            "🔒 Session Expired\nPlease login again to continue.",
            Toast.LENGTH_LONG
        ).show()
        
        // Redirect to LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        
        // Close current activity
        requireActivity().finish()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
