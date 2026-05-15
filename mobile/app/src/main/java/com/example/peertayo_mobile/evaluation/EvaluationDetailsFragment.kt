package com.example.peertayo_mobile.evaluation

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.databinding.FragmentCreateStepDetailsBinding
import java.util.*

class EvaluationDetailsFragment : Fragment() {

    private var _binding: FragmentCreateStepDetailsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: CreateEvaluationViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateStepDetailsBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(requireActivity())[CreateEvaluationViewModel::class.java]
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
            val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
                .setTitleText("Select Deadline Date")
                .setSelection(com.google.android.material.datepicker.MaterialDatePicker.todayInUtcMilliseconds())
                .setTheme(R.style.CustomDatePickerTheme)
                .build()

            datePicker.addOnPositiveButtonClickListener { selection ->
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                calendar.timeInMillis = selection
                val date = String.format("%04d-%02d-%02d", 
                    calendar.get(Calendar.YEAR), 
                    calendar.get(Calendar.MONTH) + 1, 
                    calendar.get(Calendar.DAY_OF_MONTH))
                
                // Show time picker after date selection
                showTimePicker(date)
            }
            datePicker.show(childFragmentManager, "date_picker")
        }
    }

    private fun showTimePicker(date: String) {
        val timePicker = com.google.android.material.timepicker.MaterialTimePicker.Builder()
            .setTimeFormat(com.google.android.material.timepicker.TimeFormat.CLOCK_12H)
            .setHour(23)
            .setMinute(59)
            .setTitleText("Select Deadline Time")
            .setTheme(R.style.CustomTimePickerTheme)
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val time = String.format("%02d:%02d:00", timePicker.hour, timePicker.minute)
            val fullDateTime = "${date}T${time}"
            binding.tvDeadline.text = "${date} ${String.format("%02d:%02d", timePicker.hour, timePicker.minute)}"
            viewModel.deadline.value = fullDateTime
        }
        timePicker.show(childFragmentManager, "time_picker")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
