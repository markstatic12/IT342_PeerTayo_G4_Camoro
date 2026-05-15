package com.example.peertayo_mobile.auth.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.auth.register.RegisterActivity
import com.example.peertayo_mobile.dashboard.DashboardActivity
import com.example.peertayo_mobile.data.api.RetrofitClient
import com.example.peertayo_mobile.data.local.SessionManager
import com.example.peertayo_mobile.data.model.LoginRequest
import com.example.peertayo_mobile.data.repository.AuthRepository
import com.example.peertayo_mobile.databinding.ActivityLoginBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var sessionManager: SessionManager
    private lateinit var googleSignInClient: GoogleSignInClient

    companion object {
        private const val RC_SIGN_IN = 9001
        private const val WEB_CLIENT_ID = "483611707223-7t1djq3kppndrfrp08krvep4uadnsbbn.apps.googleusercontent.com"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
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
                return LoginViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[LoginViewModel::class.java]
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

        binding.tilEmail.alpha = 0f
        binding.tilEmail.postDelayed({
            binding.tilEmail.alpha = 1f
            binding.tilEmail.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.slide_up)
            )
        }, 160)
    }

    private fun setupClickListeners() {
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleLogin()
                true
            } else false
        }

        binding.btnLogin.setOnClickListener {
            handleLogin()
        }

        binding.btnGoogleSignIn.setOnClickListener {
            signInWithGoogle()
        }

        binding.tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    private fun observeViewModel() {
        viewModel.loginState.observe(this) { state ->
            when (state) {
                is LoginState.Loading -> {
                    binding.btnLogin.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                    hideError()
                }
                is LoginState.Success -> {
                    binding.btnLogin.isEnabled = true
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
                            role = user.roles?.joinToString(", ") ?: "RESPONDENT"
                        )
                    }

                    Toast.makeText(this, "Welcome ${user?.fullName}!", Toast.LENGTH_SHORT).show()

                    // Navigate to Dashboard
                    val intent = Intent(this, DashboardActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is LoginState.Error -> {
                    binding.btnLogin.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                    showError(state.message)
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
                is LoginState.Idle -> {
                    binding.btnLogin.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }
            }
        }
    }

    private fun handleLogin() {
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return
        }

        viewModel.login(LoginRequest(email, password))
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
        binding.tilEmail.error = null
        binding.tilPassword.error = null
    }
}
