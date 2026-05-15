package com.example.peertayo_mobile.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.data.api.RetrofitClient
import com.example.peertayo_mobile.data.local.SessionManager
import com.example.peertayo_mobile.data.model.*
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import com.example.peertayo_mobile.databinding.ActivitySettingsBinding
import com.example.peertayo_mobile.databinding.ItemSettingsToggleBinding
import com.example.peertayo_mobile.landing.LandingActivity
import kotlinx.coroutines.launch

/**
 * SettingsActivity — Replicated from Web Settings (GAP-Parity)
 * Includes: Profile, Password & Security, Notifications, Roles & Access
 */
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

        setupUI()
        setupListeners()
        fetchData()
    }

    private fun fetchData() {
        lifecycleScope.launch {
            // Fetch Profile
            repository.getProfile().onSuccess { profile ->
                profile?.let {
                    sessionManager.saveUser(it.firstName, it.lastName, it.email)
                    setupUI()
                }
            }
            // Fetch Preferences
            repository.getPreferences().onSuccess { prefs ->
                prefs?.let {
                    binding.toggleNewEval.switchAction.isChecked = it.newEvaluation
                    binding.toggleDeadline.switchAction.isChecked = it.deadlineReminders
                    binding.toggleResults.switchAction.isChecked = it.resultsPublished
                    binding.toggleSystem.switchAction.isChecked = it.systemAnnouncements
                }
            }
        }
    }

    private fun setupUI() {
        // Header
        val fullName = sessionManager.getFullName()
        binding.tvFullNameHeader.text = fullName
        binding.tvRoleHeader.text = sessionManager.getRole().uppercase()
        binding.tvInitial.text = fullName.firstOrNull()?.uppercase() ?: "?"

        // Profile Form
        binding.etFirstName.setText(sessionManager.getFirstName())
        binding.etLastName.setText(sessionManager.getLastName())
        binding.etEmail.setText(sessionManager.getEmail())

        // Notification Toggles (Sync with Web structure)
        setupToggle(binding.toggleNewEval, "New Evaluation Assigned", "When a facilitator assigns you to a form")
        setupToggle(binding.toggleDeadline, "Deadline Reminders", "48-hour and 24-hour reminders")
        setupToggle(binding.toggleResults, "Results Published", "When your evaluation results are ready")
        setupToggle(binding.toggleSystem, "System Announcements", "Important updates about PeerTayo")

        // Roles & Permissions
        binding.tvActiveRole.text = sessionManager.getRole()
        populatePermissions()
    }

    private fun setupToggle(toggleBinding: ItemSettingsToggleBinding, title: String, desc: String) {
        toggleBinding.tvToggleTitle.text = title
        toggleBinding.tvToggleDesc.text = desc
    }

    private fun populatePermissions() {
        val isFacilitator = sessionManager.getRole() == "FACILITATOR"
        val permissions = mutableListOf(
            PermissionItem("Submit peer evaluations", true),
            PermissionItem("View personal performance results", true),
            PermissionItem("Receive evaluation notifications", true),
            PermissionItem("Create evaluation forms", isFacilitator),
            PermissionItem("Assign evaluators and evaluatees", isFacilitator),
            PermissionItem("View aggregated team results", isFacilitator)
        )

        binding.permissionsContainer.removeAllViews()
        val inflater = LayoutInflater.from(this)
        permissions.forEach { perm ->
            val view = inflater.inflate(R.layout.item_permission_row, binding.permissionsContainer, false)
            view.findViewById<TextView>(R.id.tvPermissionAction).text = perm.action
            val statusTv = view.findViewById<TextView>(R.id.tvPermissionStatus)
            
            if (perm.isAllowed) {
                statusTv.text = "✓ Allowed"
                statusTv.setTextColor(getColor(R.color.green_success))
            } else {
                statusTv.text = "— Not available"
                statusTv.setTextColor(getColor(R.color.text_muted))
            }
            binding.permissionsContainer.addView(view)
        }
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener { finish() }

        // Save Profile
        binding.btnSaveChanges.setOnClickListener { handleUpdateProfile() }
        binding.btnDiscard.setOnClickListener { setupUI() } // Reset fields

        // Save Preferences
        binding.btnSavePrefs.setOnClickListener {
            val prefs = NotificationPreferences(
                newEvaluation = binding.toggleNewEval.switchAction.isChecked,
                deadlineReminders = binding.toggleDeadline.switchAction.isChecked,
                resultsPublished = binding.toggleResults.switchAction.isChecked,
                systemAnnouncements = binding.toggleSystem.switchAction.isChecked
            )
            handleUpdatePreferences(prefs)
        }

        binding.btnChangePassword.setOnClickListener {
            showChangePasswordSheet()
        }

        // Logout
        binding.btnLogout.setOnClickListener {
            sessionManager.clearSession()
            val intent = Intent(this, LandingActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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

        binding.btnSaveChanges.isEnabled = false
        binding.btnSaveChanges.text = "Saving…"

        lifecycleScope.launch {
            val result = repository.updateProfile(UpdateProfileRequest(first, last, email))
            result.onSuccess { auth ->
                auth?.user?.let { user ->
                    // Update session with fresh token and user data
                    sessionManager.saveSession(
                        auth.token ?: "", user.id, user.firstName, 
                        user.lastName, user.email, user.primaryRole
                    )
                    setupUI() // Refresh header
                    Toast.makeText(this@SettingsActivity, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                }
            }.onFailure { e ->
                Toast.makeText(this@SettingsActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
            binding.btnSaveChanges.isEnabled = true
            binding.btnSaveChanges.text = "Update Profile"
        }
    }

    private fun handleUpdatePreferences(prefs: NotificationPreferences) {
        binding.btnSavePrefs.isEnabled = false
        lifecycleScope.launch {
            val result = repository.updatePreferences(prefs)
            result.onSuccess {
                Toast.makeText(this@SettingsActivity, "Preferences saved!", Toast.LENGTH_SHORT).show()
            }.onFailure { e ->
                Toast.makeText(this@SettingsActivity, "Sync failed: ${e.message}", Toast.LENGTH_LONG).show()
            }
            binding.btnSavePrefs.isEnabled = true
        }
    }
}
