package com.example.peertayo_mobile.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.data.api.RetrofitClient
import com.example.peertayo_mobile.data.local.SessionManager
import com.example.peertayo_mobile.databinding.ActivityDashboardBinding
import com.example.peertayo_mobile.landing.LandingActivity

class DashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var sessionManager: SessionManager

    // Cache fragments to avoid re-creation on tab switch
    private val homeFragment by lazy { HomeFragment() }
    private val pendingFragment by lazy { PendingFragment() }
    private val resultsFragment by lazy { ResultsFragment() }
    private val completedFragment by lazy { CompletedFragment() }
    private val formsFragment by lazy { FormsFragment() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sessionManager = SessionManager(this)
        RetrofitClient.init(this)

        // Guard: if not logged in, redirect to landing
        if (!sessionManager.isLoggedIn()) {
            navigateToLanding()
            return
        }

        setupBottomNav()
        setupTopBar()

        // Load Home tab by default
        if (savedInstanceState == null) {
            loadFragment(homeFragment)
        }
    }

    private fun setupBottomNav() {
        refreshNavMenu()

        binding.bottomNav.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.nav_home -> homeFragment
                R.id.nav_pending -> pendingFragment
                R.id.nav_results -> resultsFragment
                R.id.nav_completed -> completedFragment
                R.id.nav_forms -> formsFragment
                else -> homeFragment
            }
            loadFragment(fragment)
            true
        }
    }

    /** Dynamically show/hide the Forms tab based on the latest session role (GAP-06) */
    fun refreshNavMenu() {
        val menu = binding.bottomNav.menu
        menu.findItem(R.id.nav_forms)?.isVisible = sessionManager.isFacilitator()
    }

    private fun setupTopBar() {
        binding.btnNotifications.setOnClickListener {
            startActivity(Intent(this, com.example.peertayo_mobile.notification.NotificationActivity::class.java))
        }
        binding.btnSettings.setOnClickListener {
            startActivity(Intent(this, com.example.peertayo_mobile.settings.SettingsActivity::class.java))
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                android.R.anim.fade_in,
                android.R.anim.fade_out
            )
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    /** Called by HomeFragment shortcut cards to switch tabs programmatically */
    fun navigateToTab(itemId: Int) {
        binding.bottomNav.selectedItemId = itemId
    }

    private fun navigateToLanding() {
        val intent = Intent(this, LandingActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
