package edu.sliit.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import edu.sliit.myapplication.data.local.OfflineDatabase
import edu.sliit.myapplication.data.network.RetrofitClient
import edu.sliit.myapplication.databinding.ActivityLoginBinding
import edu.sliit.myapplication.models.LoginRequest
import edu.sliit.myapplication.models.LoginResponse
import edu.sliit.myapplication.models.UserData
import edu.sliit.myapplication.utils.NetworkMonitor
import edu.sliit.myapplication.utils.UserPreferences
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPreferences: UserPreferences
    private lateinit var offlineDb: OfflineDatabase
    private lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)
        offlineDb = OfflineDatabase(this)
        networkMonitor = NetworkMonitor(this)

        // Check if user is already logged in
        if (userPreferences.isLoggedIn()) {
            navigateToMain()
            return
        }

        setupClickListeners()
        animateViews()
        
        // Show offline indicator if no internet
        if (!networkMonitor.isOnline()) {
            showOfflineMode()
        }
        
        // Request focus and show keyboard
        binding.usernameInput.postDelayed({
            binding.usernameInput.requestFocus()
            showKeyboard()
        }, 800)
    }

    private fun showOfflineMode() {
        Snackbar.make(
            binding.root,
            "📡 Offline Mode: Use saved credentials to login",
            Snackbar.LENGTH_LONG
        ).setBackgroundTint(getColor(android.R.color.holo_orange_light)).show()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val username = binding.usernameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (validateInputs(username, password)) {
                performLogin(username, password)
            }
        }
        
        // Handle password field "Done" action
        binding.passwordInput.setOnEditorActionListener { _, _, _ ->
            val username = binding.usernameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            
            if (validateInputs(username, password)) {
                performLogin(username, password)
            }
            true
        }
    }

    private fun validateInputs(username: String, password: String): Boolean {
        if (username.isEmpty()) {
            binding.usernameLayout.error = "Username required"
            return false
        }
        binding.usernameLayout.error = null

        if (password.isEmpty()) {
            binding.passwordLayout.error = "Password required"
            return false
        }
        binding.passwordLayout.error = null

        return true
    }

    private fun performLogin(username: String, password: String) {
        binding.loginButton.isEnabled = false
        binding.progressBar.visibility = View.VISIBLE
        hideKeyboard()

        lifecycleScope.launch {
            try {
                // Check if online
                if (networkMonitor.isOnline()) {
                    // Online mode: Try API login
                    performOnlineLogin(username, password)
                } else {
                    // Offline mode: Check local credentials
                    performOfflineLogin(username, password)
                }
            } catch (e: Exception) {
                android.util.Log.e("LoginActivity", "Login error: ${e.message}", e)
                // Fallback to offline login if online login fails
                performOfflineLogin(username, password)
            } finally {
                binding.loginButton.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
        }
    }

    private suspend fun performOnlineLogin(username: String, password: String) {
        try {
            val loginRequest = LoginRequest(username, password)
            android.util.Log.d("LoginActivity", "Attempting online login for user: $username")
            
            val response = RetrofitClient.apiService.login(loginRequest)
            
            android.util.Log.d("LoginActivity", "Response code: ${response.code()}")

            if (response.isSuccessful && response.body() != null) {
                val loginResponse = response.body()!!
                android.util.Log.d("LoginActivity", "========== ONLINE LOGIN SUCCESS ==========")
                android.util.Log.d("LoginActivity", "Login successful for: ${loginResponse.username}")
                android.util.Log.d("LoginActivity", "Role: ${loginResponse.role}")
                android.util.Log.d("LoginActivity", "StationName: ${loginResponse.userData?.stationName}")
                android.util.Log.d("LoginActivity", "==========================================")
                
                // Save to SharedPreferences
                userPreferences.saveLoginResponse(loginResponse)
                
                // Save to local database for offline access
                offlineDb.saveUserCredentials(
                    username = username,
                    password = password, // Save encrypted in production!
                    fullName = loginResponse.userData?.fullName,
                    email = loginResponse.userData?.email,
                    phone = loginResponse.userData?.phone,
                    role = loginResponse.role,
                    stationId = loginResponse.userData?.stationId,
                    stationName = loginResponse.userData?.stationName,
                    token = loginResponse.token
                )
                
                android.util.Log.d("LoginActivity", "✅ Credentials saved for offline access")

                // Success message
                Snackbar.make(
                    binding.root,
                    "✓ Login Successful! Welcome ${loginResponse.username}",
                    Snackbar.LENGTH_SHORT
                ).setBackgroundTint(getColor(R.color.success)).show()

                // Navigate to MainActivity
                binding.root.postDelayed({
                    navigateToMain()
                }, 500)
            } else {
                // API login failed, try offline
                android.util.Log.w("LoginActivity", "Online login failed, trying offline...")
                performOfflineLogin(username, password)
            }
        } catch (e: java.net.UnknownHostException) {
            android.util.Log.e("LoginActivity", "Network error, trying offline login", e)
            performOfflineLogin(username, password)
        } catch (e: java.net.ConnectException) {
            android.util.Log.e("LoginActivity", "Connection error, trying offline login", e)
            performOfflineLogin(username, password)
        } catch (e: Exception) {
            android.util.Log.e("LoginActivity", "Unexpected error, trying offline login", e)
            performOfflineLogin(username, password)
        }
    }

    private fun performOfflineLogin(username: String, password: String) {
        android.util.Log.d("LoginActivity", "Attempting offline login for user: $username")
        
        // Check if credentials exist in local database
        val isValid = offlineDb.validateUserCredentials(username, password)
        
        if (isValid) {
            android.util.Log.d("LoginActivity", "========== OFFLINE LOGIN SUCCESS ==========")
            
            // Get user data from database
            val userData = offlineDb.getUserData(username)
            
            if (userData != null) {
                // Create LoginResponse from local data
                val loginResponse = LoginResponse(
                    success = true,
                    message = "Logged in offline",
                    token = userData["token"] ?: "",
                    username = userData["username"] ?: username,
                    role = userData["role"] ?: "Operator",
                    userId = "",
                    userData = UserData(
                        id = "",
                        fullName = userData["fullName"],
                        email = userData["email"],
                        phone = userData["phone"],
                        role = userData["role"],
                        stationId = userData["stationId"],
                        stationName = userData["stationName"],
                        nic = null,
                        isActive = true,
                        createdAt = null
                    )
                )
                
                // Save to SharedPreferences
                userPreferences.saveLoginResponse(loginResponse)
                
                android.util.Log.d("LoginActivity", "✅ Offline login successful")
                android.util.Log.d("LoginActivity", "User: ${userData["username"]}")
                android.util.Log.d("LoginActivity", "Station: ${userData["stationName"]}")
                android.util.Log.d("LoginActivity", "==========================================")

                // Success message with offline indicator
                Snackbar.make(
                    binding.root,
                    "✓ Offline Login Successful! Welcome ${userData["username"]}",
                    Snackbar.LENGTH_SHORT
                ).setBackgroundTint(getColor(android.R.color.holo_orange_light)).show()

                // Navigate to MainActivity
                binding.root.postDelayed({
                    navigateToMain()
                }, 500)
            } else {
                showError("✗ Error loading user data")
            }
        } else {
            android.util.Log.e("LoginActivity", "❌ Offline login failed: Invalid credentials")
            showError("✗ Invalid credentials. Login online first to save credentials.")
        }
    }

    private fun showError(message: String) {
        Snackbar.make(
            binding.root,
            message,
            Snackbar.LENGTH_LONG
        ).setBackgroundTint(getColor(R.color.error)).show()
    }

    private fun navigateToMain() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun animateViews() {
        binding.loginAnimation.alpha = 0f
        binding.welcomeText.alpha = 0f
        binding.subtitleText.alpha = 0f
        binding.loginCard.alpha = 0f

        binding.loginAnimation.animate().alpha(1f).setDuration(800).start()
        binding.welcomeText.animate().alpha(1f).setDuration(800).setStartDelay(200).start()
        binding.subtitleText.animate().alpha(1f).setDuration(800).setStartDelay(400).start()
        binding.loginCard.animate().alpha(1f).setDuration(800).setStartDelay(600).start()
    }
    
    private fun showKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.usernameInput, InputMethodManager.SHOW_IMPLICIT)
    }
    
    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }
}