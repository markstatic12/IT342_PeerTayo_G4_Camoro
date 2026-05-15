package com.example.peertayo_mobile.dashboard

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
import com.example.peertayo_mobile.databinding.FragmentResultsBinding

/**
 * ResultsFragment — GAP-01 fix.
 *
 * Added:
 * - 4th stat card: "Highest Criterion" (tvHighestCriterion)
 * - Filter tabs: All / Highest / Most Recent  (delegates to ResultsViewModel.filter())
 * - Correct terminology: "Evaluations Received", "Overall Avg Score", "Total Responses"
 */
class ResultsFragment : Fragment() {

    private var _binding: FragmentResultsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: ResultsViewModel
    private lateinit var adapter: ResultsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecyclerView()
        setupFilterTabs()
        observeViewModel()
    }

    private fun setupViewModel() {
        val repository = EvaluationRepository(RetrofitClient.evaluationApi)
        val factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return ResultsViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[ResultsViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = ResultsAdapter { resultSummary ->
            val fragment = ResultDetailFragment.newInstance(
                title = resultSummary.title,
                average = resultSummary.overallAverage ?: 0.0,
                responses = resultSummary.totalResponses ?: 0,
                comments = ArrayList(resultSummary.comments ?: emptyList()),
                criteriaNames = ArrayList(resultSummary.questionAverages?.map { it.criteriaName ?: "Criterion" } ?: emptyList()),
                criteriaScores = ArrayList(resultSummary.questionAverages?.map { it.average.toFloat() } ?: emptyList())
            )
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    android.R.anim.fade_in, android.R.anim.fade_out,
                    android.R.anim.fade_in, android.R.anim.fade_out
                )
                .replace(R.id.fragmentContainer, fragment)
                .addToBackStack(null)
                .commit()
        }
        binding.rvResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvResults.adapter = adapter
    }

    /** Wires filter tabs to ResultsViewModel.filter() — matches web All/Highest/Most Recent */
    private fun setupFilterTabs() {
        val pills = listOf(
            binding.filterAll     to "all",
            binding.filterHighest to "highest",
            binding.filterRecent  to "recent"
        )
        pills.forEach { (pill, type) ->
            pill.setOnClickListener {
                // Update visual selection
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
                is ResultsState.Loading -> {
                    binding.rvResults.visibility = View.GONE
                    binding.emptyState.visibility = View.GONE
                }
                is ResultsState.Success -> {
                    val evals = state.evaluations
                    if (evals.isEmpty()) {
                        binding.rvResults.visibility = View.GONE
                        binding.emptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvResults.visibility = View.VISIBLE
                        binding.emptyState.visibility = View.GONE
                        adapter.submitList(evals)
                        binding.rvResults.scrollToPosition(0)
                    }

                    // Stats — aligned with web MyResultsPage 4-stat summary strip
                    val r = state.results
                    binding.tvReceivedCount.text = evals.size.toString()
                    binding.tvAverageScore.text =
                        if ((r?.overallAverage ?: 0.0) > 0)
                            String.format("%.1f", r?.overallAverage ?: 0.0)
                        else "—"
                    binding.tvImprovedScore.text = (r?.totalResponses ?: 0).toString()
                    // 4th stat: Highest Criterion (GAP-01)
                    binding.tvHighestCriterion.text = state.highestCriterion
                }
                is ResultsState.Error -> {
                    binding.rvResults.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
