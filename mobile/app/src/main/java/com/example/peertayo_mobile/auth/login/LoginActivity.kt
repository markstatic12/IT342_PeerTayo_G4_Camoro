package com.example.peertayo_mobile.auth.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.peertayo_mobile.auth.register.RegisterActivity
import com.example.peertayo_mobile.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        // Set up the login form
        binding.etEmail.hint = "Email address"
        binding.etPassword.hint = "Enter your password"
        binding.btnLogin.text = "Sign In"
    }

    private fun setupClickListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            
            if (email.isNotEmpty() && password.isNotEmpty()) {
                // TODO: Implement actual login logic
                Toast.makeText(this, "Login clicked for: $email", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.tvBack.setOnClickListener {
            finish()
        }
    }
}
