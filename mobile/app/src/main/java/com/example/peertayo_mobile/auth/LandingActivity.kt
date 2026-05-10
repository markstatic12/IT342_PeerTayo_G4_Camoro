package com.example.peertayo_mobile.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.peertayo_mobile.databinding.ActivityLandingBinding
import com.example.peertayo_mobile.auth.login.LoginActivity
import com.example.peertayo_mobile.auth.register.RegisterActivity

class LandingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Navigation bar buttons
        binding.btnNavLogin.setOnClickListener {
            navigateToLogin()
        }
        binding.btnNavSignUp.setOnClickListener {
            navigateToRegister()
        }

        // Hero section button
        binding.btnGetStarted.setOnClickListener {
            navigateToRegister()
        }

        // CTA buttons
        binding.btnFinalCTA.setOnClickListener {
            navigateToRegister()
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }

    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }
}
