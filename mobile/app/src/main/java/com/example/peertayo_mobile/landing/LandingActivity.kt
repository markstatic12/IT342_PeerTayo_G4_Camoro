package com.example.peertayo_mobile.landing

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peertayo_mobile.R
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
        runEntranceAnimations()
    }

    private fun setupUI() {
        // Features RecyclerView
        binding.rvFeatures.apply {
            layoutManager = LinearLayoutManager(this@LandingActivity)
            adapter = FeaturesAdapter(getFeaturesList())
            isNestedScrollingEnabled = false
        }

        // How It Works RecyclerView
        binding.rvHowItWorks.apply {
            layoutManager = LinearLayoutManager(this@LandingActivity)
            adapter = HowItWorksAdapter(getHowItWorksList())
            isNestedScrollingEnabled = false
        }

        // Stats (can animate counting in a real implementation)
        binding.statEvaluations.text = "500+"
        binding.statUsers.text = "1,000+"
        binding.statTeams.text = "50+"
    }

    private fun setupClickListeners() {
        binding.btnGetStarted.setOnClickListener {
            navigateToRegister()
        }

        binding.btnLogin.setOnClickListener {
            navigateToLogin()
        }

        binding.btnLearnMore.setOnClickListener {
            // Smooth scroll to features section
            binding.scrollView.post {
                binding.scrollView.smoothScrollTo(0, binding.featuresSection.top)
            }
        }

        // CTA section secondary link
        binding.tvSignInLink.setOnClickListener {
            navigateToLogin()
        }
    }

    private fun runEntranceAnimations() {
        val fadeUp = AnimationUtils.loadAnimation(this, R.anim.fade_up)

        // Stagger the hero elements
        binding.heroBadge.alpha = 0f
        binding.heroBadge.postDelayed({
            binding.heroBadge.alpha = 1f
            binding.heroBadge.startAnimation(fadeUp)
        }, 80)

        binding.statsStrip.alpha = 0f
        binding.statsStrip.postDelayed({
            binding.statsStrip.alpha = 1f
            binding.statsStrip.startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.fade_up)
            )
        }, 220)
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun navigateToRegister() {
        startActivity(Intent(this, RegisterActivity::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
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
