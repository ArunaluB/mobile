package edu.sliit.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import edu.sliit.myapplication.databinding.ActivitySplashBinding
import edu.sliit.myapplication.data.network.RetrofitClient
import edu.sliit.myapplication.utils.UserPreferences

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RetrofitClient with context for authentication
        RetrofitClient.initialize(this)
        
        userPreferences = UserPreferences(this)

        startAnimations()

        Handler(Looper.getMainLooper()).postDelayed({
            // Check if user is already logged in
            if (userPreferences.isLoggedIn()) {
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 3000) // 3 seconds delay
    }

    private fun startAnimations() {
        // Fade in animation
        binding.logoCircle.animate()
            .alpha(1f)
            .setDuration(900)
            .start()

        binding.logoText.animate()
            .alpha(1f)
            .setDuration(900)
            .setStartDelay(200)
            .start()

        binding.appNameText.animate()
            .alpha(1f)
            .setDuration(900)
            .setStartDelay(400)
            .start()

        binding.taglineText.animate()
            .alpha(1f)
            .setDuration(900)
            .setStartDelay(600)
            .start()

        binding.loadingProgress.animate()
            .alpha(1f)
            .setDuration(900)
            .setStartDelay(800)
            .start()

        binding.designerText.animate()
            .alpha(1f)
            .setDuration(900)
            .setStartDelay(1000)
            .start()
    }
}