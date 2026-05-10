package com.example.peertayo_mobile.landing

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peertayo_mobile.auth.login.LoginActivity
import com.example.peertayo_mobile.auth.register.RegisterActivity
import com.example.peertayo_mobile.databinding.ActivityLandingBinding

class LandingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLandingBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
    }

    private fun setupUI() {
        // Setup RecyclerView for features
        binding.rvFeatures.apply {
            layoutManager = LinearLayoutManager(this@LandingActivity)
            adapter = FeaturesAdapter(getFeaturesList())
        }

        // Setup RecyclerView for how it works steps
        binding.rvHowItWorks.apply {
            layoutManager = LinearLayoutManager(this@LandingActivity)
            adapter = HowItWorksAdapter(getHowItWorksList())
        }

        // Setup stats
        setupStats()
    }

    private fun setupClickListeners() {
        binding.btnGetStarted.setOnClickListener {
            navigateToRegister()
        }

        binding.btnLogin.setOnClickListener {
            navigateToLogin()
        }

        binding.btnLearnMore.setOnClickListener {
            // Scroll to features section
            binding.scrollView.smoothScrollTo(0, binding.featuresSection.top)
        }
    }
    
    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
    }
    
    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
    }

    private fun setupStats() {
        // Animate stats counting (simplified version)
        binding.statEvaluations.text = "500+"
        binding.statUsers.text = "1,000+"
        binding.statTeams.text = "50+"
    }

    private fun getFeaturesList(): List<FeatureItem> {
        return listOf(
            FeatureItem(
                title = "Private Evaluations",
                description = "Submit feedback privately with confidence that your responses are confidential.",
                iconType = "lock"
            ),
            FeatureItem(
                title = "Structured Forms",
                description = "Use standardized 10-question rating scales for consistent, comparable results.",
                iconType = "form"
            ),
            FeatureItem(
                title = "Real-time Analytics",
                description = "View aggregated results and performance insights instantly.",
                iconType = "chart"
            ),
            FeatureItem(
                title = "Smart Notifications",
                description = "Get timely reminders for pending evaluations and deadline alerts.",
                iconType = "bell"
            ),
            FeatureItem(
                title = "Role-based Access",
                description = "Different experiences for respondents and facilitators.",
                iconType = "role"
            ),
            FeatureItem(
                title = "Mobile Ready",
                description = "Complete evaluations on-the-go with our mobile app.",
                iconType = "mobile"
            )
        )
    }

    private fun getHowItWorksList(): List<HowItWorksItem> {
        return listOf(
            HowItWorksItem(
                step = "01",
                title = "Create Form",
                description = "Facilitators create evaluation forms with standardized questions.",
                iconType = "create"
            ),
            HowItWorksItem(
                step = "02",
                title = "Assign Evaluators",
                description = "Assign team members to evaluate their peers with clear deadlines.",
                iconType = "assign"
            ),
            HowItWorksItem(
                step = "03",
                title = "Submit Feedback",
                description = "Evaluators complete private rating-based assessments.",
                iconType = "submit"
            ),
            HowItWorksItem(
                step = "04",
                title = "View Results",
                description = "Access summarized analytics and performance insights.",
                iconType = "results"
            )
        )
    }
}
