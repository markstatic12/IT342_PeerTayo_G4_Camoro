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
import com.example.peertayo_mobile.databinding.FragmentFormsBinding
import com.example.peertayo_mobile.evaluation.CreateEvaluationActivity
import android.content.Intent

/**
 * FormsFragment — GAP-04 fix.
 *
 * Added:
 * - 4-stat strip observer (Total / Active / Needs Attention / Closed)
 * - Search bar wired to FormsViewModel.search()
 * - Filter tabs: All / Active / Needs Attention / Closed
 */
class FormsFragment : Fragment() {

    private var _binding: FragmentFormsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: FormsViewModel
    private lateinit var adapter: FormsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFormsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecyclerView()
        setupSearch()
        setupFilterTabs()
        observeViewModel()

        binding.btnCreate.setOnClickListener {
            startActivity(Intent(requireContext(), CreateEvaluationActivity::class.java))
        }
    }

    private fun setupViewModel() {
        val repository = EvaluationRepository(RetrofitClient.evaluationApi)
        val factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return FormsViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[FormsViewModel::class.java]
    }

    private fun setupRecyclerView() {
        adapter = FormsAdapter()
        binding.rvForms.layoutManager = LinearLayoutManager(requireContext())
        binding.rvForms.adapter = adapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.search(s?.toString() ?: "")
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })
    }

    /** Filter tabs: All / Active / Needs Attention / Closed */
    private fun setupFilterTabs() {
        val pills = listOf(
            binding.filterAll       to "all",
            binding.filterActive    to "active",
            binding.filterAttention to "attention",
            binding.filterClosed    to "closed"
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
                is FormsState.Loading -> {
                    binding.rvForms.visibility = View.GONE
                    binding.emptyState.visibility = View.GONE
                }
                is FormsState.Success -> {
                    // Update 4-stat strip
                    binding.tvStatTotal.text     = state.stats.total.toString()
                    binding.tvStatActive.text    = state.stats.active.toString()
                    binding.tvStatAttention.text = state.stats.needsAttention.toString()
                    binding.tvStatClosed.text    = state.stats.closed.toString()

                    if (state.forms.isEmpty()) {
                        binding.rvForms.visibility = View.GONE
                        binding.emptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvForms.visibility = View.VISIBLE
                        binding.emptyState.visibility = View.GONE
                        adapter.submitList(state.forms)
                        binding.rvForms.scrollToPosition(0)
                    }
                }
                is FormsState.Error -> {
                    binding.rvForms.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.loadForms()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
