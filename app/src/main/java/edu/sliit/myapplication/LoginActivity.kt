package edu.sliit.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import edu.sliit.myapplication.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var databaseHelper: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        databaseHelper = DatabaseHelper(this)

        setupClickListeners()
        animateViews()
    }

    private fun setupClickListeners() {
        binding.loginButton.setOnClickListener {
            val username = binding.usernameInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()

            if (validateInputs(username, password)) {
                performLogin(username, password)
            }
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

        if (databaseHelper.checkUser(username, password)) {
            // Success message
            Snackbar.make(
                binding.root,
                "✓ Login Successful!",
                Snackbar.LENGTH_SHORT
            ).setBackgroundTint(getColor(R.color.success)).show()

            // Navigate to MainActivity
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        } else {
            // Error message
            Snackbar.make(
                binding.root,
                "✗ Invalid username or password",
                Snackbar.LENGTH_LONG
            ).setBackgroundTint(getColor(R.color.error)).show()

            binding.loginButton.isEnabled = true
        }
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
}