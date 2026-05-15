package com.example.peertayo_mobile.evaluation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peertayo_mobile.databinding.FragmentCreateStepParticipantsBinding

class EvaluationParticipantsFragment : Fragment() {

    private var _binding: FragmentCreateStepParticipantsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CreateEvaluationViewModel

    private lateinit var evaluatorAdapter: SelectedUserAdapter
    private lateinit var evaluateeAdapter: SelectedUserAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateStepParticipantsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[CreateEvaluationViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapters()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupAdapters() {
        evaluatorAdapter = SelectedUserAdapter { id -> viewModel.removeEvaluator(id) }
        binding.rvEvaluators.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEvaluators.adapter = evaluatorAdapter

        evaluateeAdapter = SelectedUserAdapter { id -> viewModel.removeEvaluatee(id) }
        binding.rvEvaluatees.layoutManager = LinearLayoutManager(requireContext())
        binding.rvEvaluatees.adapter = evaluateeAdapter
    }

    private fun setupClickListeners() {
        binding.btnAddEvaluators.setOnClickListener {
            UserSearchBottomSheet { users ->
                viewModel.addEvaluators(users)
            }.show(childFragmentManager, "search_evaluators")
        }

        binding.btnAddEvaluatees.setOnClickListener {
            UserSearchBottomSheet { users ->
                viewModel.addEvaluatees(users)
            }.show(childFragmentManager, "search_evaluatees")
        }
    }

    private fun observeViewModel() {
        viewModel.evaluators.observe(viewLifecycleOwner) { users ->
            evaluatorAdapter.submitList(users)
        }
        viewModel.evaluatees.observe(viewLifecycleOwner) { users ->
            evaluateeAdapter.submitList(users)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
