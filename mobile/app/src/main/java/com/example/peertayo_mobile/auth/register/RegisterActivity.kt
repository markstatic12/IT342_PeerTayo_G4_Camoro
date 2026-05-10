package com.example.peertayo_mobile.auth.register

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.peertayo_mobile.MainActivity
import com.example.peertayo_mobile.auth.login.LoginActivity
import com.example.peertayo_mobile.databinding.ActivityRegisterBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: RegisterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeRegisterState()
    }

    private fun setupClickListeners() {
        binding.btnRegister.setOnClickListener {
            clearErrors()
            validateAndRegister()
        }

        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
        
        binding.btnGoogleSignUp.setOnClickListener {
            // TODO: Implement Google Sign-Up
            Toast.makeText(this, "Google Sign-Up coming soon", Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateAndRegister() {
        val firstName = binding.etFirstName.text.toString()
        val lastName = binding.etLastName.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etConfirmPassword.text.toString()

        // Validation
        when {
            firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                showError("Please fill in all fields")
            }
            password != confirmPassword -> {
                showError("Passwords do not match")
            }
            password.length < 6 -> {
                showError("Password must be at least 6 characters")
            }
            else -> {
                viewModel.register(firstName, lastName, email, password)
            }
        }
    }

    private fun observeRegisterState() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                is RegisterState.Loading -> {
                    binding.btnRegister.isEnabled = false
                    binding.btnRegister.text = "Creating account..."
                    binding.progressBar.visibility = View.VISIBLE
                }
                is RegisterState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "Welcome ${state.user.fullName}!", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
                is RegisterState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnRegister.isEnabled = true
                    binding.btnRegister.text = "Create Account"
                    showError(state.message)
                }
            }
        }
    }
    
    private fun showError(message: String) {
        binding.tvError.text = message
        binding.tvError.visibility = View.VISIBLE
    }
    
    private fun clearErrors() {
        binding.tvError.visibility = View.GONE
    }
}
