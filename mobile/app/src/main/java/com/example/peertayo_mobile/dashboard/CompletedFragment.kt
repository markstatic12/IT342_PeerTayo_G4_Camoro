package com.example.peertayo_mobile.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peertayo_mobile.data.api.RetrofitClient
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import com.example.peertayo_mobile.databinding.FragmentCompletedBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class CompletedFragment : Fragment() {

    private var _binding: FragmentCompletedBinding? = null
    private val binding get() = _binding!!
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
        adapter = CompletedAdapter()
        binding.rvCompleted.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCompleted.adapter = adapter
        loadData()
    }

    private fun loadData() {
        val repository = EvaluationRepository(RetrofitClient.evaluationApi)
        viewLifecycleOwner.lifecycleScope.launch {
            // Load completed forms and submitted summary in parallel
            val completedResult = repository.getCompletedForms()
            val summaryResult  = repository.getSubmittedSummary()

            completedResult.onSuccess { forms ->
                if (forms.isEmpty()) {
                    binding.rvCompleted.visibility = View.GONE
                    binding.emptyState.visibility  = View.VISIBLE
                } else {
                    binding.rvCompleted.visibility = View.VISIBLE
                    binding.emptyState.visibility  = View.GONE
                    adapter.submitList(forms)
                }

                // ── Stats aligned with web MyCompletedFormsPage summary strip ──
                // tvSubmittedCount  = Total Submitted (all time)
                // tvEvaluateesCount = Submitted This Month
                // tvLastDate        = Avg Score Given (if calculable)
                binding.tvSubmittedCount.text  = forms.size.toString()

                val summary = summaryResult.getOrNull()
                binding.tvEvaluateesCount.text = (summary?.submittedThisMonth ?: 0).toString()

                // Avg score given: average of all submitted ratings — web shows avgScoreGiven
                // Since CompletedForm model doesn't carry per-criteria ratings, show "—"
                // The web pulls this from getCompletedForms() which includes criteria breakdowns
                binding.tvLastDate.text = "—"

            }.onFailure {
                binding.rvCompleted.visibility = View.GONE
                binding.emptyState.visibility  = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
