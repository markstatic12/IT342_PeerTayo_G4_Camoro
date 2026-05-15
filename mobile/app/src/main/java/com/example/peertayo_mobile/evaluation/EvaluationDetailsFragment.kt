package com.example.peertayo_mobile.evaluation

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.peertayo_mobile.databinding.FragmentCreateStepDetailsBinding
import java.util.*

class EvaluationDetailsFragment : Fragment() {

    private var _binding: FragmentCreateStepDetailsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: CreateEvaluationViewModel by activityViewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateStepDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupInputSync()
        setupDatePicker()
    }

    private fun setupInputSync() {
        binding.etTitle.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.title.value = s.toString() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etDescription.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { viewModel.description.value = s.toString() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun setupDatePicker() {
        binding.btnDeadline.setOnClickListener {
            val calendar = Calendar.getInstance()
            val datePicker = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    val date = String.format("%04d-%02d-%02d", year, month + 1, day)
                    binding.tvDeadline.text = date
                    viewModel.deadline.value = date
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
