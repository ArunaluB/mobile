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

    private fun loadUserData() {
        val loginResponse = userPreferences.getLoginResponse()
        
        if (loginResponse != null) {
            // Set avatar with first letter of username
            val firstLetter = loginResponse.username.firstOrNull()?.uppercase() ?: "U"
            binding.profileAvatar.text = firstLetter
            
            // Set user information
            binding.profileUsername.text = loginResponse.username
            binding.profileRole.text = loginResponse.role
            binding.profileEmail.text = loginResponse.userData.email
            binding.profilePhone.text = loginResponse.userData.phone
            binding.profileUserId.text = loginResponse.userId.take(8) + "..."
            
            // Set station information
            binding.stationName.text = loginResponse.userData.stationName
            binding.stationType.text = loginResponse.userData.station.type
            binding.stationLocation.text = String.format(
                "%.4f, %.4f",
                loginResponse.userData.station.location.lat,
                loginResponse.userData.station.location.lng
            )
            binding.totalSlots.text = loginResponse.userData.station.slots.size.toString()
        }
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
        binding.headerCard.alpha = 0f
        binding.userInfoCard.alpha = 0f
        binding.stationInfoCard.alpha = 0f
        binding.logoutButton.alpha = 0f

        binding.headerCard.animate().alpha(1f).setDuration(600).start()
        binding.userInfoCard.animate().alpha(1f).setDuration(600).setStartDelay(200).start()
        binding.stationInfoCard.animate().alpha(1f).setDuration(600).setStartDelay(400).start()
        binding.logoutButton.animate().alpha(1f).setDuration(600).setStartDelay(600).start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}