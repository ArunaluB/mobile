package edu.sliit.myapplication

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import edu.sliit.myapplication.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        startAnimations()

        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
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