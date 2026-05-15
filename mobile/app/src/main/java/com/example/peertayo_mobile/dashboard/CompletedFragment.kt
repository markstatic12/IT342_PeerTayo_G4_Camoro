package com.example.peertayo_mobile.dashboard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.data.api.RetrofitClient
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import com.example.peertayo_mobile.databinding.FragmentCompletedBinding

/**
 * CompletedFragment — GAP-03 fix.
 *
 * Migrated from a raw coroutine call to CompletedViewModel (proper MVVM).
 * Added:
 * - Search bar wired to CompletedViewModel.search()
 * - Filter tabs (All / This Week) wired to CompletedViewModel.filter()
 * - Correct stat labels: Total Submitted / This Month / Avg Given
 */
class CompletedFragment : Fragment() {

    private var _binding: FragmentCompletedBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CompletedViewModel
    private lateinit var adapter: CompletedAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecyclerView()
        setupSearch()
        setupFilterTabs()
        observeViewModel()
    }

    private fun setupViewModel() {
        val repository = EvaluationRepository(RetrofitClient.evaluationApi)
        val factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return CompletedViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[CompletedViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = CompletedAdapter()
        binding.rvCompleted.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCompleted.adapter = adapter
    }

    /** Live search against evaluatee name + form title */
    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    /** Filter tabs: All / This Week — matches web MyCompletedFormsPage tabs */
    private fun setupFilterTabs() {
        val pills = listOf(
            binding.filterAll      to "all",
            binding.filterThisWeek to "week"
        )
        pills.forEach { (pill, type) ->
            pill.setOnClickListener {
                pills.forEach { (v, _) ->
                    v.setTextColor(resources.getColor(R.color.text_secondary, null))
                    v.setBackgroundResource(R.drawable.bg_filter_pill)
                }
                pill.setTextColor(resources.getColor(R.color.text_on_cyan, null))
                pill.setBackgroundResource(R.drawable.bg_filter_pill_active)
                viewModel.filter(type)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is CompletedState.Loading -> {
                    binding.rvCompleted.visibility = View.GONE
                    binding.emptyState.visibility = View.GONE
                }
                is CompletedState.Success -> {
                    if (state.forms.isEmpty()) {
                        binding.rvCompleted.visibility = View.GONE
                        binding.emptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvCompleted.visibility = View.VISIBLE
                        binding.emptyState.visibility = View.GONE
                        adapter.submitList(state.forms)
                        binding.rvCompleted.scrollToPosition(0)
                    }
                    // Stats aligned with web MyCompletedFormsPage:
                    // tvSubmittedCount  = Total Submitted
                    // tvEvaluateesCount = Submitted This Month
                    // tvLastDate        = Avg Score Given
                    binding.tvSubmittedCount.text  = state.totalSubmitted.toString()
                    binding.tvEvaluateesCount.text = state.submittedThisMonth.toString()
                    binding.tvLastDate.text        = state.avgScoreGiven
                }
                is CompletedState.Error -> {
                    binding.rvCompleted.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadCompleted()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
