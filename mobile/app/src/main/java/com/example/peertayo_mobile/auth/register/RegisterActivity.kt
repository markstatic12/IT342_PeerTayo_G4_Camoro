package com.example.peertayo_mobile.auth.register

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.auth.login.LoginActivity
import com.example.peertayo_mobile.dashboard.DashboardActivity
import com.example.peertayo_mobile.data.api.RetrofitClient
import com.example.peertayo_mobile.data.local.SessionManager
import com.example.peertayo_mobile.data.model.RegisterRequest
import com.example.peertayo_mobile.data.repository.AuthRepository
import com.example.peertayo_mobile.databinding.ActivityRegisterBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var viewModel: RegisterViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val WEB_CLIENT_ID = "483611707223-7t1djq3kppndrfrp08krvep4uadnsbbn.apps.googleusercontent.com"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        RetrofitClient.init(this)

        setupGoogleSignIn()
        setupViewModel()
        runEntranceAnimation()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .requestProfile()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
    }

    private fun setupViewModel() {
        val repository = AuthRepository(RetrofitClient.authApi)
        val factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return RegisterViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[RegisterViewModel::class.java]
    }

    private fun runEntranceAnimation() {
        val anim = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        binding.brandRow.startAnimation(anim)

        binding.tvHeading.alpha = 0f
        binding.tvHeading.postDelayed({
            binding.tvHeading.alpha = 1f
            binding.tvHeading.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.slide_up)
            )
        }, 80)

        binding.tilFirstName.alpha = 0f
        binding.tilFirstName.postDelayed({
            binding.tilFirstName.alpha = 1f
            binding.tilFirstName.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.slide_up)
            )
            binding.tilLastName.alpha = 1f
        }, 160)
    }

    private fun setupClickListeners() {
        binding.etConfirmPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleRegister()
                true
            } else false
        }

        binding.btnRegister.setOnClickListener {
            handleRegister()
        }

        binding.btnGoogleSignUp.setOnClickListener {
            signInWithGoogle()
        }

        binding.tvLoginLink.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun observeViewModel() {
        viewModel.registerState.observe(this) { state ->
            when (state) {
                is RegisterState.Loading -> {
                    binding.btnRegister.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    hideError()
                }
                is RegisterState.Success -> {
                    binding.btnRegister.isEnabled = true
                    binding.progressBar.visibility = View.GONE

                    // Save session
                    val user = state.response.user
                    val token = state.response.token
                    if (user != null && token != null) {
                        sessionManager.saveSession(
                            token = token,
                            userId = user.id,
                            firstName = user.firstName,
                            lastName = user.lastName,
                            email = user.email,
                            role = user.primaryRole
                        )
                    }

                    Toast.makeText(this, "Account created for ${user?.firstName}!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is RegisterState.Error -> {
                    binding.btnRegister.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    showError(state.message)
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
                is RegisterState.Idle -> {
                    binding.btnRegister.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun handleRegister() {
        val firstName = binding.etFirstName.text.toString().trim()
        val lastName = binding.etLastName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        when {
            firstName.isEmpty() -> binding.tilFirstName.error = "First name is required"
            lastName.isEmpty() -> binding.tilLastName.error = "Last name is required"
            email.isEmpty() -> binding.tilEmail.error = "Email is required"
            !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> binding.tilEmail.error = "Enter a valid email"
            password.isEmpty() -> binding.tilPassword.error = "Password is required"
            password.length < 8 -> binding.tilPassword.error = "At least 8 characters"
            confirmPassword.isEmpty() -> binding.tilConfirmPassword.error = "Confirm your password"
            password != confirmPassword -> showError("Passwords do not match")
            else -> {
                viewModel.register(RegisterRequest(firstName, lastName, email, password))
            }
        }
    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        }
    }

    private fun handleGoogleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            val idToken = account.idToken
            if (idToken != null) {
                // In our backend, Google Login handles both login and account creation
                // so we can use the same ViewModel method if needed, but RegisterViewModel
                // currently only has register(). However, the backend /auth/google handles it all.
                // I'll add googleLogin to RegisterViewModel as well or just redirect to Login logic.
                // For now, I'll use a toast to guide the user or just implement it.
                viewModel.googleLogin(idToken)
            } else {
                showError("Google sign-in failed: No ID Token")
            }
        } catch (e: ApiException) {
            showError("Google sign-in failed: ${e.statusCode}")
        }
    }

    private fun showError(message: String) {
        binding.errorBanner.visibility = View.VISIBLE
        binding.tvError.text = message
    }

    private fun hideError() {
        binding.errorBanner.visibility = View.GONE
        binding.tilFirstName.error = null
        binding.tilLastName.error = null
        binding.tilEmail.error = null
        binding.tilPassword.error = null
        binding.tilConfirmPassword.error = null
    }
}
