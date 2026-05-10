package com.example.peertayo_mobile.auth.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.peertayo_mobile.auth.login.LoginActivity
import com.example.peertayo_mobile.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        // Set up the registration form
        binding.etFirstName.hint = "First name"
        binding.etLastName.hint = "Last name"
        binding.etEmail.hint = "Email address"
        binding.etPassword.hint = "Create a password"
        binding.etConfirmPassword.hint = "Confirm password"
        binding.btnRegister.text = "Create Account"
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()
            
            if (firstName.isNotEmpty() && lastName.isNotEmpty() && email.isNotEmpty() && 
                password.isNotEmpty() && confirmPassword.isNotEmpty()) {
                
                if (password == confirmPassword) {
                    // TODO: Implement actual registration logic
                    Toast.makeText(this, "Account created for: $email", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        binding.tvBack.setOnClickListener {
            finish()
        }
    }
}
