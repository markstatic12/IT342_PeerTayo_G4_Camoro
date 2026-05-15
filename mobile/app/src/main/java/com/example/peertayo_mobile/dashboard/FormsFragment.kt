package com.example.peertayo_mobile.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peertayo_mobile.data.api.RetrofitClient
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import com.example.peertayo_mobile.databinding.FragmentFormsBinding
import com.example.peertayo_mobile.evaluation.CreateEvaluationActivity

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

    private fun observeViewModel() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is FormsState.Loading -> {
                    binding.rvForms.visibility = View.GONE
                    binding.emptyState.visibility = View.GONE
                }
                is FormsState.Success -> {
                    if (state.forms.isEmpty()) {
                        binding.rvForms.visibility = View.GONE
                        binding.emptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvForms.visibility = View.VISIBLE
                        binding.emptyState.visibility = View.GONE
                        adapter.submitList(state.forms)
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
