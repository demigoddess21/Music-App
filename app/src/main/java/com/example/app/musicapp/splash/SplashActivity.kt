package com.example.app.musicapp.splash

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import com.example.app.musicapp.BuildConfig
import com.example.app.musicapp.MainActivity
import com.example.app.musicapp.R
import com.example.app.musicapp.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {
    private lateinit var runnable: Runnable
    private val binding: ActivitySplashBinding by lazy {
        ActivitySplashBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        if (Build.VERSION.SDK_INT >= 21) {
            val window = this.window
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            window.statusBarColor = this.resources.getColor(R.color.black)
        }
        binding.tvVersion.text = "Version: ${BuildConfig.VERSION_NAME}"
        init()
    }
    private fun init() {
        runnable = Runnable {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
    override fun onStart() {
        super.onStart()
        Handler(Looper.getMainLooper()).postDelayed(runnable, 1500)
    }


}