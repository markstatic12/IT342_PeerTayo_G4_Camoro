package com.example.peertayo_mobile.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.landing.LandingActivity
import com.example.peertayo_mobile.data.api.RetrofitClient
import com.example.peertayo_mobile.data.local.SessionManager
import com.example.peertayo_mobile.data.model.ChangePasswordRequest
import com.example.peertayo_mobile.data.model.NotificationPreferences
import com.example.peertayo_mobile.data.model.UpdateProfileRequest
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import com.example.peertayo_mobile.databinding.ActivitySettingsBinding
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var sessionManager: SessionManager
    private lateinit var repository: EvaluationRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        repository = EvaluationRepository(RetrofitClient.evaluationApi)

        setupToolbar()
        setupUI()
        setupClickListeners()
        fetchData()
    }

    private fun setupToolbar() {
        binding.btnBack.setOnClickListener { finish() }
    }

    private fun setupUI() {
        val name = sessionManager.getFullName().ifEmpty { "User" }
        binding.tvFullNameHeader.text = name
        binding.tvInitial.text = name.take(1).uppercase()
        
        val roles = sessionManager.getRole()
        binding.tvRoleHeader.text = roles.uppercase()
        binding.tvActiveRole.text = roles.uppercase()
        
        updatePermissionsList(roles)
        
        binding.etFirstName.setText(sessionManager.getFirstName())
        binding.etLastName.setText(sessionManager.getLastName())
        binding.etEmail.setText(sessionManager.getEmail())

        // Setup Toggles (Web/Mobile Consistency)
        binding.toggleNewEval.tvToggleTitle.text = "Evaluation Assignments"
        binding.toggleNewEval.tvToggleDesc.text = "Get notified when you are assigned as an evaluator"
        
        binding.toggleDeadline.tvToggleTitle.text = "Deadline Reminders"
        binding.toggleDeadline.tvToggleDesc.text = "Receive alerts for upcoming evaluation deadlines"
        
        binding.toggleResults.tvToggleTitle.text = "Results Published"
        binding.toggleResults.tvToggleDesc.text = "Notifications when evaluation results are available"
        
        binding.toggleSystem.tvToggleTitle.text = "System Alerts"
        binding.toggleSystem.tvToggleDesc.text = "Critical security and system maintenance updates"
    }

    private fun fetchData() {
        lifecycleScope.launch {
            // Profile
            repository.getProfile().onSuccess { profile ->
                profile?.let {
                    binding.etFirstName.setText(it.firstName)
                    binding.etLastName.setText(it.lastName)
                    binding.etEmail.setText(it.email)
                    binding.tvFullNameHeader.text = it.fullName
                    binding.tvInitial.text = it.firstName.take(1).uppercase()
                    
                    // Web Parity: Disable password change for Google accounts
                    if (it.provider == "GOOGLE") {
                        binding.btnChangePassword.isEnabled = false
                        binding.btnChangePassword.alpha = 0.5f
                        binding.tvSecuritySubtitle.visibility = View.VISIBLE
                        binding.tvSecuritySubtitle.text = "Your account uses Google sign-in. Password management is handled by Google."
                    }
                    
                    sessionManager.saveUser(it.firstName, it.lastName, it.email)
                }
            }.onFailure {
                Toast.makeText(this@SettingsActivity, "Failed to load profile", Toast.LENGTH_SHORT).show()
            }

            // Preferences
            repository.getPreferences().onSuccess { prefs ->
                prefs?.let {
                    binding.toggleNewEval.switchAction.isChecked = it.evaluationAssigned
                    binding.toggleDeadline.switchAction.isChecked = it.deadlineReminder
                    binding.toggleResults.switchAction.isChecked = it.resultsPublished
                    binding.toggleSystem.switchAction.isChecked = it.systemAnnouncements
                }
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnSaveChanges.setOnClickListener {
            handleUpdateProfile()
        }

        binding.btnDiscard.setOnClickListener {
            fetchData() // Reset
        }

        binding.btnSavePrefs.setOnClickListener {
            handleUpdatePreferences()
        }

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordSheet()
        }

        binding.btnLogout.setOnClickListener {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Sign Out")
                .setMessage("Are you sure you want to sign out of PeerTayo?")
                .setPositiveButton("Sign Out") { _, _ ->
                    sessionManager.clearSession()
                    Toast.makeText(this, "Signed out successfully", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, LandingActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun showChangePasswordSheet() {
        val bottomSheet = com.google.android.material.bottomsheet.BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_change_password, null)
        bottomSheet.setContentView(view)

        val btnSubmit = view.findViewById<com.google.android.material.button.MaterialButton>(R.id.btnSubmitPassword)
        val etCurrent = view.findViewById<android.widget.EditText>(R.id.etCurrentPassword)
        val etNew = view.findViewById<android.widget.EditText>(R.id.etNewPassword)
        val etConfirm = view.findViewById<android.widget.EditText>(R.id.etConfirmPassword)

        btnSubmit.setOnClickListener {
            val current = etCurrent.text.toString()
            val new = etNew.text.toString()
            val confirm = etConfirm.text.toString()

            if (current.isEmpty() || new.isEmpty() || confirm.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (new != confirm) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (new.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnSubmit.isEnabled = false
            btnSubmit.text = "Updating…"

            lifecycleScope.launch {
                val result = repository.changePassword(ChangePasswordRequest(current, new))
                result.onSuccess {
                    Toast.makeText(this@SettingsActivity, "Password updated successfully!", Toast.LENGTH_SHORT).show()
                    bottomSheet.dismiss()
                }.onFailure { e ->
                    Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    btnSubmit.isEnabled = true
                    btnSubmit.text = "Update Password"
                }
            }
        }
        bottomSheet.show()
    }

    private fun handleUpdateProfile() {
        val first = binding.etFirstName.text.toString()
        val last = binding.etLastName.text.toString()
        val email = binding.etEmail.text.toString()
        
        if (first.isEmpty() || last.isEmpty() || email.isEmpty()) return

        lifecycleScope.launch {
            val result = repository.updateProfile(UpdateProfileRequest(first, last, email))
            result.onSuccess {
                Toast.makeText(this@SettingsActivity, "Profile updated", Toast.LENGTH_SHORT).show()
                sessionManager.saveUser(first, last, email)
                binding.tvFullNameHeader.text = "$first $last"
                binding.tvInitial.text = first.take(1).uppercase()
            }.onFailure {
                Toast.makeText(this@SettingsActivity, "Update failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleUpdatePreferences() {
        val prefs = NotificationPreferences(
            evaluationAssigned = binding.toggleNewEval.switchAction.isChecked,
            deadlineReminder = binding.toggleDeadline.switchAction.isChecked,
            resultsPublished = binding.toggleResults.switchAction.isChecked,
            systemAnnouncements = binding.toggleSystem.switchAction.isChecked
        )
        lifecycleScope.launch {
            val result = repository.updatePreferences(prefs)
            result.onSuccess {
                Toast.makeText(this@SettingsActivity, "Preferences synced", Toast.LENGTH_SHORT).show()
            }.onFailure {
                Toast.makeText(this@SettingsActivity, "Failed to sync preferences", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updatePermissionsList(roleString: String) {
        binding.permissionsContainer.removeAllViews()
        val roles = roleString.split(",").map { it.trim().uppercase() }
        
        roles.forEach { role ->
            val view = layoutInflater.inflate(R.layout.item_permission_badge, binding.permissionsContainer, false)
            val title = view.findViewById<TextView>(R.id.tvPermTitle)
            val desc = view.findViewById<TextView>(R.id.tvPermDesc)
            
            when (role) {
                "RESPONDENT" -> {
                    title.text = "Respondent Access"
                    desc.text = "Full access to evaluate peers, view received feedback, and track performance results."
                }
                "FACILITATOR" -> {
                    title.text = "Facilitator Access"
                    desc.text = "Ability to create forms, manage evaluation cycles, and view team-wide analytics."
                }
                "ADMIN" -> {
                    title.text = "Administrator Access"
                    desc.text = "Global system control, user management, and advanced configuration settings."
                }
            }
            binding.permissionsContainer.addView(view)
        }
    }
}
