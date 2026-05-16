package com.example.peertayo_mobile.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.data.api.RetrofitClient
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import com.example.peertayo_mobile.databinding.FragmentPendingBinding
import com.example.peertayo_mobile.evaluation.EvaluateFormActivity

class PendingFragment : Fragment() {

    private var _binding: FragmentPendingBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: PendingViewModel
    private lateinit var adapter: PendingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPendingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewModel()
        setupRecyclerView()
        setupFilters()
        observeViewModel()
    }

    private fun setupViewModel() {
        val repository = EvaluationRepository(RetrofitClient.evaluationApi)
        val sessionManager = com.example.peertayo_mobile.data.local.SessionManager(requireContext())
        val factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return PendingViewModel(repository, sessionManager) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[PendingViewModel::class.java]
    }

    private fun setupRecyclerView() {
        // PendingAdapter now receives PendingForm and launches evaluate for a specific evaluatee
        adapter = PendingAdapter { form, evaluatee ->
            val intent = Intent(requireContext(), EvaluateFormActivity::class.java).apply {
                putExtra("EVAL_ID", evaluatee.assignmentId)
                putExtra("EVAL_TITLE", form.title)
                putExtra("EVALUATEE_NAME", evaluatee.name)
                putExtra("DEADLINE", form.deadline ?: "")
            }
            startActivity(intent)
        }
        binding.rvPending.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPending.adapter = adapter
    }

    private fun setupFilters() {
        // Matches web: All / Urgent / Missed
        val pills = listOf(
            binding.filterAll    to "all",
            binding.filterUrgent to "urgent",
            binding.filterWeek   to "missed"  // repurposed to "Missed" to match web
        )
        pills.forEach { (pillView, type) ->
            pillView.setOnClickListener {
                // Update visual selection
                pills.forEach { (v, _) ->
                    v.setTextColor(resources.getColor(R.color.text_secondary, null))
                    v.setBackgroundResource(R.drawable.bg_filter_pill)
                }
                pillView.setTextColor(resources.getColor(R.color.text_on_cyan, null))
                pillView.setBackgroundResource(R.drawable.bg_filter_pill_active)
                viewModel.filter(type)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PendingState.Loading -> {
                    binding.rvPending.visibility = View.GONE
                    binding.emptyState.visibility = View.GONE
                }
                is PendingState.Success -> {
                    if (state.forms.isEmpty()) {
                        binding.rvPending.visibility = View.GONE
                        binding.emptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvPending.visibility = View.VISIBLE
                        binding.emptyState.visibility = View.GONE
                        adapter.submitList(state.forms)
                        binding.rvPending.scrollToPosition(0)
                    }
                    // Update stats — now matches web: Pending Count / Urgent / Submitted This Month
                    binding.tvPendingCount.text = state.totalPending.toString()
                    binding.tvUrgentCount.text  = state.urgentCount.toString()
                    binding.tvDoneCount.text    = state.submittedThisMonth.toString()
                }
                is PendingState.Error -> {
                    binding.rvPending.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadPending()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
