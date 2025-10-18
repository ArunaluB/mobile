package edu.sliit.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import edu.sliit.myapplication.databinding.FragmentProfileBinding
import edu.sliit.myapplication.utils.UserPreferences

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var userPreferences: UserPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        userPreferences = UserPreferences(requireContext())
        
        loadUserData()
        setupLogoutButton()
        animateViews()
    }
    
    override fun onResume() {
        super.onResume()
        // Reload user data when fragment becomes visible
        loadUserData()
    }

    private fun loadUserData() {
        android.util.Log.d("ProfileFragment", "========== LOADING USER DATA ==========")
        val loginResponse = userPreferences.getLoginResponse()
        
        if (loginResponse != null) {
            try {
                android.util.Log.d("ProfileFragment", "Login response found!")
                android.util.Log.d("ProfileFragment", "Username: ${loginResponse.username}")
                android.util.Log.d("ProfileFragment", "Role: ${loginResponse.role}")
                android.util.Log.d("ProfileFragment", "UserId: ${loginResponse.userId}")
                android.util.Log.d("ProfileFragment", "UserData is null: ${loginResponse.userData == null}")
                
                // Set avatar with first letter of username
                val firstLetter = loginResponse.username.firstOrNull()?.uppercase() ?: "U"
                binding.profileAvatar.text = firstLetter
                
                // Set user information
                binding.profileUsername.text = loginResponse.username ?: "Unknown"
                binding.profileRole.text = loginResponse.role ?: "User"
                
                // Enhanced logging for email and phone
                val email = loginResponse.userData?.email
                val phone = loginResponse.userData?.phone
                android.util.Log.d("ProfileFragment", "Email from userData: $email")
                android.util.Log.d("ProfileFragment", "Phone from userData: $phone")
                
                binding.profileEmail.text = email ?: "N/A"
                binding.profilePhone.text = phone ?: "N/A"
                binding.profileUserId.text = (loginResponse.userId?.take(8) ?: "Unknown") + "..."
                
                // Set station information with null safety
                loginResponse.userData?.let { userData ->
                    android.util.Log.d("ProfileFragment", "UserData exists")
                    android.util.Log.d("ProfileFragment", "Station Name: ${userData.stationName}")
                    android.util.Log.d("ProfileFragment", "Station object is null: ${userData.station == null}")
                    
                    binding.stationName.text = userData.stationName ?: "N/A"
                    
                    userData.station?.let { station ->
                        android.util.Log.d("ProfileFragment", "Station data exists")
                        android.util.Log.d("ProfileFragment", "Station Type: ${station.type}")
                        android.util.Log.d("ProfileFragment", "Station Location: ${station.location}")
                        android.util.Log.d("ProfileFragment", "Station Slots count: ${station.slots?.size}")
                        
                        binding.stationType.text = station.type ?: "N/A"
                        
                        station.location?.let { location ->
                            binding.stationLocation.text = String.format(
                                "%.4f, %.4f",
                                location.lat,
                                location.lng
                            )
                        } ?: run {
                            binding.stationLocation.text = "N/A"
                        }
                        
                        binding.totalSlots.text = station.slots?.size?.toString() ?: "0"
                    } ?: run {
                        android.util.Log.w("ProfileFragment", "Station object is null, setting N/A")
                        binding.stationType.text = "N/A"
                        binding.stationLocation.text = "N/A"
                        binding.totalSlots.text = "0"
                    }
                } ?: run {
                    android.util.Log.w("ProfileFragment", "UserData is null, setting all to N/A")
                    binding.stationName.text = "N/A"
                    binding.stationType.text = "N/A"
                    binding.stationLocation.text = "N/A"
                    binding.totalSlots.text = "0"
                }
                
                android.util.Log.d("ProfileFragment", "User data loaded successfully")
            } catch (e: Exception) {
                // Log the error and set default values
                android.util.Log.e("ProfileFragment", "Error loading user data: ${e.message}", e)
                e.printStackTrace()
                setDefaultValues()
            }
        } else {
            android.util.Log.e("ProfileFragment", "LoginResponse is NULL - no data in SharedPreferences!")
            setDefaultValues()
        }
        android.util.Log.d("ProfileFragment", "=======================================")
    }
    
    private fun setDefaultValues() {
        binding.profileAvatar.text = "U"
        binding.profileUsername.text = "Unknown User"
        binding.profileRole.text = "User"
        binding.profileEmail.text = "N/A"
        binding.profilePhone.text = "N/A"
        binding.profileUserId.text = "N/A"
        binding.stationName.text = "N/A"
        binding.stationType.text = "N/A"
        binding.stationLocation.text = "N/A"
        binding.totalSlots.text = "0"
    }

    private fun setupLogoutButton() {
        binding.logoutButton.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Clear all saved user data
        userPreferences.clearUserData()
        
        // Navigate to LoginActivity
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    private fun animateViews() {
        try {
            binding.headerCard.alpha = 0f
            binding.userInfoCard.alpha = 0f
            binding.stationInfoCard.alpha = 0f
            binding.logoutButton.alpha = 0f

            binding.headerCard.animate().alpha(1f).setDuration(600).start()
            binding.userInfoCard.animate().alpha(1f).setDuration(600).setStartDelay(200).start()
            binding.stationInfoCard.animate().alpha(1f).setDuration(600).setStartDelay(400).start()
            binding.logoutButton.animate().alpha(1f).setDuration(600).setStartDelay(600).start()
        } catch (e: Exception) {
            android.util.Log.e("ProfileFragment", "Error animating views", e)
            // Set views to fully visible if animation fails
            binding.headerCard.alpha = 1f
            binding.userInfoCard.alpha = 1f
            binding.stationInfoCard.alpha = 1f
            binding.logoutButton.alpha = 1f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}