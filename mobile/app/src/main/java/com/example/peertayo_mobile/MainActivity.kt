package com.example.peertayo_mobile

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.peertayo_mobile.auth.login.LoginActivity
import com.example.peertayo_mobile.core.utils.TokenManager
import com.example.peertayo_mobile.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    @Inject
    lateinit var tokenManager: TokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (!tokenManager.isLoggedIn()) {
            navigateToLogin()
            return
        }

        binding.tvWelcome.text = "Welcome to PeerTayo!"

        binding.btnLogout.setOnClickListener {
            tokenManager.clearToken()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
