package edu.sliit.myapplication

import android.Manifest
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
            viewModel.confirmAction("confirm-start")
        }
        
        binding.btnConfirmEnd.setOnClickListener {
            viewModel.confirmAction("confirm-end")
        }
        
        binding.btnComplete.setOnClickListener {
            completeBooking()
        }
        
        binding.btnCancel.setOnClickListener {
            cancelBooking()
        }
        
        binding.btnScanAnother.setOnClickListener {
            resetUI()
        }
    }
    
    private fun observeViewModel() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            showLoading(isLoading)
        }
        
        viewModel.scanResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    displayBookingDetails(it.getOrNull()!!)
                } else {
                    showError(it.exceptionOrNull()?.message ?: "Scan failed")
                }
            }
        }
        
        viewModel.confirmResult.observe(viewLifecycleOwner) { result ->
            result?.let {
                if (it.isSuccess) {
                    val response = it.getOrNull()!!
                    updateStatusUI(response.status, response.nextActions)
                    Toast.makeText(context, "Status updated successfully!", Toast.LENGTH_SHORT).show()
                } else {
                    showError(it.exceptionOrNull()?.message ?: "Confirmation failed")
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
        if (!isAdded || _binding == null) return
        
        binding.emptyState.visibility = View.GONE
        binding.bookingDetailsCard.visibility = View.VISIBLE
        
        // Display booking information
        binding.tvReservationId.text = response.reservationId
        binding.tvStationId.text = response.stationId
        binding.tvStatus.text = response.status
        
        // Format and display times
        val startTime = formatDateTime(response.startTimeUtc)
        val endTime = formatDateTime(response.endTimeUtc)
        binding.tvStartTime.text = startTime
        binding.tvEndTime.text = endTime
        
        // Display time slot highlight
        binding.tvTimeSlot.text = "${formatTime(response.startTimeUtc)} - ${formatTime(response.endTimeUtc)}"
        binding.tvDate.text = formatDate(response.startTimeUtc)
        
        // Display owner NIC if available
        if (response.ownerNic != null) {
            binding.tvOwnerNic.text = response.ownerNic
            binding.ownerNicLayout.visibility = View.VISIBLE
        } else {
            binding.ownerNicLayout.visibility = View.GONE
        }
        
        // Update status UI
        updateStatusUI(response.status, response.nextActions)
        
        // Save to local database
        saveScanToDatabase(response)
    }
    
    private fun saveScanToDatabase(response: edu.sliit.myapplication.data.model.ScanResponse) {
        val bookingHistory = BookingHistory(
            reservationId = response.reservationId,
            stationId = response.stationId,
            status = response.status,
            ownerNic = response.ownerNic ?: "",
            startTime = formatTime(response.startTimeUtc),
            endTime = formatTime(response.endTimeUtc),
            scanTime = System.currentTimeMillis(),
            isCompleted = response.status.equals("completed", ignoreCase = true),
            isCancelled = response.status.equals("cancelled", ignoreCase = true)
        )
        
        currentReservationId = response.reservationId
        
        CoroutineScope(Dispatchers.IO).launch {
            historyRepository.insertHistory(bookingHistory)
            Log.d(TAG, "Scan saved to SharedPreferences: ${response.reservationId}")
        }
    }
    
    private fun updateStatusUI(status: String, nextActions: List<String>) {
        binding.statusBadge.text = status
        
        // Set status color
        val statusColor = when (status.lowercase()) {
            "approved" -> R.color.status_approved
            "in-progress" -> R.color.status_in_progress
            "completed" -> R.color.status_completed
            "cancelled" -> R.color.status_cancelled
            else -> R.color.status_pending
        }
        binding.statusBadge.setBackgroundResource(statusColor)
        
        // Show/hide action buttons based on next actions
        binding.actionButtonsLayout.visibility = if (nextActions.isNotEmpty()) View.VISIBLE else View.GONE
        
        binding.btnConfirmStart.visibility = if (nextActions.contains("confirm-start")) View.VISIBLE else View.GONE
        binding.btnConfirmEnd.visibility = if (nextActions.contains("confirm-end")) View.VISIBLE else View.GONE
        
        // Show complete/cancel buttons for in-progress bookings
        val isInProgress = status.equals("in-progress", ignoreCase = true)
        binding.completeCancelLayout.visibility = if (isInProgress) View.VISIBLE else View.GONE
    }
    
    private fun completeBooking() {
        // Mark booking as completed
        binding.tvStatus.text = "Completed"
        binding.statusBadge.text = "Completed"
        binding.statusBadge.setBackgroundResource(R.color.status_completed)
        binding.actionButtonsLayout.visibility = View.GONE
        binding.completeCancelLayout.visibility = View.GONE
        
        // Update in SharedPreferences
        if (currentReservationId.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                historyRepository.updateHistoryStatus(currentReservationId, isCompleted = true, isCancelled = false)
                Log.d(TAG, "Marked scan $currentReservationId as completed")
            }
        }
        
        Toast.makeText(context, "Booking completed successfully!", Toast.LENGTH_SHORT).show()
        
        // Show success animation
        binding.successAnimation.visibility = View.VISIBLE
    }
    
    private fun cancelBooking() {
        // Mark booking as cancelled
        binding.tvStatus.text = "Cancelled"
        binding.statusBadge.text = "Cancelled"
        binding.statusBadge.setBackgroundResource(R.color.status_cancelled)
        binding.actionButtonsLayout.visibility = View.GONE
        binding.completeCancelLayout.visibility = View.GONE
        
        // Update in SharedPreferences
        if (currentReservationId.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                historyRepository.updateHistoryStatus(currentReservationId, isCompleted = false, isCancelled = true)
                Log.d(TAG, "Marked scan $currentReservationId as cancelled")
            }
        }
        
        Toast.makeText(context, "Booking cancelled", Toast.LENGTH_SHORT).show()
    }
    
    private fun resetUI() {
        viewModel.clearResults()
        binding.emptyState.visibility = View.VISIBLE
        binding.bookingDetailsCard.visibility = View.GONE
        binding.successAnimation.visibility = View.GONE
        currentReservationId = ""
    }
    
    private fun showDemoData() {
        // Demo data for testing without API
        val demoResponse = edu.sliit.myapplication.data.model.ScanResponse(
            reservationId = "RES-001",
            stationId = "STN-045",
            status = "Approved",
            startTimeUtc = "2025-10-12T12:30:00Z",
            endTimeUtc = "2025-10-12T13:30:00Z",
            ownerNic = "1999XXXXXXXX",
            nextActions = listOf("confirm-start")
        )
        displayBookingDetails(demoResponse)
        Toast.makeText(context, "Demo data loaded (Long press to test)", Toast.LENGTH_SHORT).show()
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
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
