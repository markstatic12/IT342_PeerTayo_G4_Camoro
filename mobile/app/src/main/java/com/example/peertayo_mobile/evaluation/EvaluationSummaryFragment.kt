package com.example.peertayo_mobile.evaluation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.peertayo_mobile.databinding.FragmentCreateStepSummaryBinding

class EvaluationSummaryFragment : Fragment() {

    private var _binding: FragmentCreateStepSummaryBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CreateEvaluationViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateStepSummaryBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[CreateEvaluationViewModel::class.java]
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeViewModel()
    }

    private fun observeViewModel() {
        viewModel.title.observe(viewLifecycleOwner) { 
            binding.tvSummaryTitle.text = it.ifBlank { "Untitled Evaluation" }
        }
        viewModel.deadline.observe(viewLifecycleOwner) {
            binding.tvSummaryDeadline.text = "Deadline: ${it.ifBlank { "Not set" }}"
        }
        viewModel.evaluators.observe(viewLifecycleOwner) {
            binding.tvEvaluatorCount.text = it.size.toString()
        }
        viewModel.evaluatees.observe(viewLifecycleOwner) {
            binding.tvEvaluateeCount.text = it.size.toString()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
