package com.example.peertayo_mobile.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.data.api.RetrofitClient
import com.example.peertayo_mobile.data.local.SessionManager
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import com.example.peertayo_mobile.databinding.FragmentHomeBinding
import com.example.peertayo_mobile.evaluation.CreateEvaluationActivity
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import android.os.Handler
import android.os.Looper

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sessionManager: SessionManager
    private lateinit var viewModel: DashboardViewModel

    // Carousel state (GAP-07)
    private val carouselHandler = Handler(Looper.getMainLooper())
    private var currentSlideIndex = 0
    private val slides = listOf(
        PromoSlide(R.string.promo_title, R.string.promo_desc, R.string.promo_button),
        PromoSlide(R.string.promo_title_productivity, R.string.promo_desc_productivity, R.string.promo_button),
        PromoSlide(R.string.promo_title_insights, R.string.promo_desc_insights, R.string.promo_button)
    )

    private val carouselRunnable = object : Runnable {
        override fun run() {
            currentSlideIndex = (currentSlideIndex + 1) % slides.size
            updatePromoSlide()
            carouselHandler.postDelayed(this, 5000) // 5 seconds auto-advance
        }
    }

    data class PromoSlide(val titleRes: Int, val descRes: Int, val buttonRes: Int)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sessionManager = SessionManager(requireContext())

        setupViewModel()
        setupGreeting()
        setupPromoBanner()
        setupShortcutClicks()
        observeViewModel()
    }

    private fun setupViewModel() {
        val repository = EvaluationRepository(RetrofitClient.evaluationApi)
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[DashboardViewModel::class.java]
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { data ->
            // Stat shortcut cards
            binding.tvPendingCount.text   = if (data.isLoading) "…" else data.pendingCount.toString()
            binding.tvAverageScore.text   = if (data.isLoading) "…" else
                if (data.averageScore > 0) String.format("%.1f", data.averageScore) else "—"
            binding.tvSubmittedCount.text = if (data.isLoading) "…" else data.submittedCount.toString()
            // 4th card: "Needs Attention" — urgent pending (overdue deadlines)
            binding.tvAttentionCount.text = if (data.isLoading) "…" else data.attentionCount.toString()

            // ── Dynamic Recent Activity Feed (matches web's activityItems list) ──
            if (!data.isLoading) {
                buildRecentActivity(data)
            }
        }
    }

    /**
     * Mirrors web's activityItems useMemo:
     * Submitted evaluations → pending evaluations → performance results
     */
    private fun buildRecentActivity(data: DashboardData) {
        binding.recentActivityContainer.removeAllViews()

        val items = mutableListOf<Triple<String, String, String>>() // (dot color hex, title, message)

        if (data.submittedCount > 0) {
            items.add(Triple(
                "#22C55E",
                "Submitted ${data.submittedCount} evaluation${if (data.submittedCount != 1) "s" else ""}",
                "${data.submittedThisMonth} this month"
            ))
        }

        if (data.pendingCount > 0) {
            val urgentMsg = if (data.attentionCount > 0) "${data.attentionCount} due within 2 days" else "No urgent deadlines"
            items.add(Triple(
                "#F97316",
                "${data.pendingCount} evaluation${if (data.pendingCount != 1) "s" else ""} pending",
                urgentMsg
            ))
        }

        if (data.averageScore > 0) {
            items.add(Triple(
                "#A78BFA",
                "Performance results available",
                "avg ${String.format("%.1f", data.averageScore)} across your criteria"
            ))
        }

        if (items.isEmpty()) {
            binding.tvRecentEmpty.visibility = View.VISIBLE
            return
        }

        binding.tvRecentEmpty.visibility = View.GONE
        val inflater = LayoutInflater.from(requireContext())
        items.forEach { (colorHex, title, message) ->
            val row = inflater.inflate(R.layout.item_activity_row, binding.recentActivityContainer, false)
            
            // Apply dynamic color to the timeline dot
            val dot = row.findViewById<View>(R.id.activityDot)
            dot.backgroundTintList = android.content.res.ColorStateList.valueOf(
                android.graphics.Color.parseColor(colorHex)
            )
            
            row.findViewById<TextView>(R.id.tvActivityTitle).text = title
            row.findViewById<TextView>(R.id.tvActivityMessage).text = message
            binding.recentActivityContainer.addView(row)
        }
    }

    private fun setupGreeting() {
        // Web: "Hello, {firstName} 👋" — mobile now matches exactly
        val firstName = sessionManager.getFirstName().ifEmpty {
            sessionManager.getFullName().split(" ").firstOrNull() ?: "there"
        }
        binding.tvGreeting.text = "Hello, $firstName"

        // Today's full date label — matches web's formatDateLabel
        val dateFormat = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault())
        binding.tvDate.text = dateFormat.format(Date())

        // Deadline alert (updated by ViewModel once loaded)
        binding.tvRolePill.text = sessionManager.getRole().uppercase()
    }

    private fun setupPromoBanner() {
        if (!sessionManager.isFacilitator()) {
            binding.promoBanner.visibility = View.VISIBLE
            updatePromoSlide()
            startCarousel()
            binding.btnPromote.setOnClickListener {
                handlePromoteToFacilitator()
            }
        } else {
            binding.promoBanner.visibility = View.GONE
            stopCarousel()
        }
    }

    private fun startCarousel() {
        stopCarousel()
        carouselHandler.postDelayed(carouselRunnable, 5000)
    }

    private fun stopCarousel() {
        carouselHandler.removeCallbacks(carouselRunnable)
    }

    private fun updatePromoSlide() {
        if (_binding == null) return
        val slide = slides[currentSlideIndex]
        binding.promoTitle.text = getString(slide.titleRes)
        binding.promoDesc.text = getString(slide.descRes)
        binding.btnPromote.text = getString(slide.buttonRes)
    }

    /**
     * Implements the promote-to-facilitator flow (was previously a TODO stub).
     * Matches web's handlePromote() in DashboardPage.jsx.
     */
    private fun handlePromoteToFacilitator() {
        binding.btnPromote.isEnabled = false
        binding.btnPromote.text = "Upgrading…"

        val repository = EvaluationRepository(RetrofitClient.evaluationApi)
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = repository.promoteToFacilitator()
                result.onSuccess { user ->
                    // Save updated role list in session (GAP-06 / Multiple Roles)
                    val roles = user?.roles?.joinToString(", ") ?: "RESPONDENT, FACILITATOR"
                    sessionManager.saveRole(roles)
                    
                    // Immediately show the 'Forms' tab in DashboardActivity
                    (activity as? DashboardActivity)?.refreshNavMenu()
                    
                    // Navigate to CreateEvaluation (matches web: navigate('/forms-created/new'))
                    startActivity(Intent(requireContext(), CreateEvaluationActivity::class.java))
                    binding.promoBanner.visibility = View.GONE
                    Toast.makeText(requireContext(), "You are now a Facilitator!", Toast.LENGTH_SHORT).show()
                }.onFailure { e ->
                    Toast.makeText(requireContext(), e.message ?: "Failed to upgrade. Try again.", Toast.LENGTH_LONG).show()
                    binding.btnPromote.isEnabled = true
                    binding.btnPromote.text = getString(R.string.promo_button)
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Network error. Please try again.", Toast.LENGTH_LONG).show()
                binding.btnPromote.isEnabled = true
                binding.btnPromote.text = getString(R.string.promo_button)
            }
        }
    }

    private fun setupShortcutClicks() {
        binding.cardPending.setOnClickListener {
            (activity as? DashboardActivity)?.navigateToTab(R.id.nav_pending)
        }
        binding.cardAverage.setOnClickListener {
            (activity as? DashboardActivity)?.navigateToTab(R.id.nav_results)
        }
        binding.cardSubmitted.setOnClickListener {
            (activity as? DashboardActivity)?.navigateToTab(R.id.nav_completed)
        }
        // Attention card → pending (urgent filter) — same as web's "View Pending" action
        binding.cardAttention.setOnClickListener {
            (activity as? DashboardActivity)?.navigateToTab(R.id.nav_pending)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopCarousel()
        _binding = null
    }
}
