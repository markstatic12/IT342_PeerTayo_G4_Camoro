package com.example.peertayo_mobile.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.peertayo_mobile.data.local.SessionManager
import com.example.peertayo_mobile.databinding.ActivitySettingsBinding
import com.example.peertayo_mobile.landing.LandingActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)

        binding.btnBack.setOnClickListener { finish() }

        // Populate profile
        val fullName = sessionManager.getFullName()
        binding.tvFullName.text = fullName
        binding.tvEmail.text = sessionManager.getEmail()
        binding.tvRole.text = sessionManager.getRole().uppercase()
        binding.tvInitial.text = fullName.firstOrNull()?.uppercase() ?: "?"
        binding.tvFirstName.text = sessionManager.getFirstName()
        binding.tvLastName.text = sessionManager.getLastName()
        binding.tvEmailDetail.text = sessionManager.getEmail()

        // Logout
        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(this, LandingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
