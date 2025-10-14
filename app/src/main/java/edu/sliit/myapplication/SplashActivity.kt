package edu.sliit.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import edu.sliit.myapplication.databinding.ActivitySplashBinding
import edu.sliit.myapplication.utils.UserPreferences

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

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
        binding.appNameText.animate()
            .alpha(1f)
            .setDuration(1000)
            .start()

        binding.lottieAnimation.animate()
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(300)
            .start()

        binding.designerText.animate()
            .alpha(1f)
            .setDuration(1000)
            .setStartDelay(600)
            .start()
    }
}