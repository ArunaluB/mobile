package edu.sliit.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import edu.sliit.myapplication.data.network.RetrofitClient
import edu.sliit.myapplication.databinding.ActivityLoginBinding
import edu.sliit.myapplication.models.LoginRequest
import edu.sliit.myapplication.utils.UserPreferences
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userPreferences = UserPreferences(this)

        // Check if user is already logged in
        if (userPreferences.isLoggedIn()) {
            navigateToMain()
            return
        }

        setupClickListeners()
        animateViews()
        
        // Request focus and show keyboard
        binding.usernameInput.postDelayed({
            binding.usernameInput.requestFocus()
            showKeyboard()
        }, 800)
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
                val loginRequest = LoginRequest(username, password)
                android.util.Log.d("LoginActivity", "Attempting login for user: $username")
                
                val response = RetrofitClient.apiService.login(loginRequest)
                
                android.util.Log.d("LoginActivity", "Response code: ${response.code()}")
                android.util.Log.d("LoginActivity", "Response message: ${response.message()}")

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    android.util.Log.d("LoginActivity", "Login successful for: ${loginResponse.username}")
                    
                    // Save login response to SharedPreferences
                    userPreferences.saveLoginResponse(loginResponse)

                    // Success message
                    Snackbar.make(
                        binding.root,
                        "✓ Login Successful! Welcome ${loginResponse.username}",
                        Snackbar.LENGTH_SHORT
                    ).setBackgroundTint(getColor(R.color.success)).show()

                    // Navigate to MainActivity with a slight delay
                    binding.root.postDelayed({
                        navigateToMain()
                    }, 500)
                } else {
                    // Error message with status code
                    val errorMessage = when (response.code()) {
                        401 -> "Invalid username or password"
                        404 -> "API endpoint not found"
                        500 -> "Server error. Please try again"
                        else -> response.message() ?: "Login failed"
                    }
                    android.util.Log.e("LoginActivity", "Login failed: $errorMessage (Code: ${response.code()})")
                    showError("✗ $errorMessage")
                }
            } catch (e: java.net.UnknownHostException) {
                android.util.Log.e("LoginActivity", "Network error: ${e.message}", e)
                showError("✗ Cannot connect to server. Check your network connection")
            } catch (e: java.net.ConnectException) {
                android.util.Log.e("LoginActivity", "Connection error: ${e.message}", e)
                showError("✗ Connection refused. Make sure the server is running")
            } catch (e: javax.net.ssl.SSLException) {
                android.util.Log.e("LoginActivity", "SSL error: ${e.message}", e)
                showError("✗ SSL connection error. Check server certificate")
            } catch (e: java.net.SocketTimeoutException) {
                android.util.Log.e("LoginActivity", "Timeout error: ${e.message}", e)
                showError("✗ Connection timeout. Server took too long to respond")
            } catch (e: Exception) {
                android.util.Log.e("LoginActivity", "Unexpected error: ${e.message}", e)
                showError("✗ Error: ${e.message ?: "Unknown error occurred"}")
                e.printStackTrace()
            } finally {
                binding.loginButton.isEnabled = true
                binding.progressBar.visibility = View.GONE
            }
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
        // Updated to new layout IDs (logo and title elements)
        binding.logoCircle.alpha = 0f
        binding.logoText.alpha = 0f
        binding.appTitle.alpha = 0f
        binding.appSubtitle.alpha = 0f
        binding.loginCard.alpha = 0f

        binding.logoCircle.animate().alpha(1f).setDuration(800).start()
        binding.logoText.animate().alpha(1f).setDuration(800).setStartDelay(200).start()
        binding.appTitle.animate().alpha(1f).setDuration(800).setStartDelay(400).start()
        binding.appSubtitle.animate().alpha(1f).setDuration(800).setStartDelay(600).start()
        binding.loginCard.animate().alpha(1f).setDuration(800).setStartDelay(800).start()
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