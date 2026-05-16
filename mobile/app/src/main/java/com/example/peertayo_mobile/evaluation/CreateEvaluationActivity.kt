package com.example.peertayo_mobile.evaluation

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.data.api.RetrofitClient
import com.example.peertayo_mobile.data.model.CreateEvaluationRequest
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import com.example.peertayo_mobile.databinding.ActivityCreateEvaluationBinding
import kotlinx.coroutines.launch

class CreateEvaluationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEvaluationBinding
    private lateinit var viewModel: CreateEvaluationViewModel
    private val repository by lazy { EvaluationRepository(RetrofitClient.evaluationApi) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEvaluationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[CreateEvaluationViewModel::class.java]

        setupViewPager()
        setupNavigation()
        observeViewModel()
    }

    private fun setupViewPager() {
        val adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount() = 4
            override fun createFragment(position: Int): Fragment {
                return when (position) {
                    0 -> EvaluationDetailsFragment()
                    1 -> EvaluationCriteriaFragment()
                    2 -> EvaluationParticipantsFragment()
                    else -> EvaluationSummaryFragment()
                }
            }
        }
        binding.viewPager.adapter = adapter
        binding.viewPager.isUserInputEnabled = false // Force button-only navigation
        
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateStepperUI(position)
                viewModel.setStep(position)
            }
        })
    }

    private fun updateStepperUI(position: Int) {
        binding.tvStepIndicator.text = "${position + 1} of 4"
        binding.stepperProgress.setProgress((position + 1) * 25, true)
        
        binding.tvStepTitle.text = when(position) {
            0 -> "Details"
            1 -> "Criteria"
            2 -> "Participants"
            else -> "Review"
        }

        binding.btnPrevious.visibility = if (position == 0) View.INVISIBLE else View.VISIBLE
        binding.btnNext.text = if (position == 3) "Publish Evaluation" else "Next Step"
    }

    private fun setupNavigation() {
        binding.btnBack.setOnClickListener { finish() }
        
        binding.btnPrevious.setOnClickListener {
            binding.viewPager.currentItem -= 1
        }

        binding.btnNext.setOnClickListener {
            val current = binding.viewPager.currentItem
            val overlap = viewModel.getOverlapCount()
            
            if (current == 2 && overlap > 0) {
                Toast.makeText(this, "❌ Error: Users cannot be in both lists (self-evaluation is not allowed). Please remove overlapping users.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (viewModel.isValidStep(current)) {
                if (current < 3) {
                    binding.viewPager.currentItem += 1
                } else {
                    publishEvaluation()
                }
            } else {
                Toast.makeText(this, "Please complete all required fields correctly", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        // Shared state handled via ViewModel
    }

    private fun publishEvaluation() {
        val title = viewModel.title.value ?: ""
        val desc = viewModel.description.value
        val deadline = viewModel.deadline.value ?: ""
        val evaluators = viewModel.evaluators.value?.map { it.id } ?: emptyList()
        val evaluatees = viewModel.evaluatees.value?.map { it.id } ?: emptyList()

        binding.btnNext.isEnabled = false
        binding.btnNext.text = "Publishing..."

        lifecycleScope.launch {
            val request = CreateEvaluationRequest(
                title = title,
                description = desc,
                deadline = deadline,
                evaluatorIds = evaluators,
                evaluateeIds = evaluatees
            )
            repository.createEvaluation(request).onSuccess { res ->
                Toast.makeText(this@CreateEvaluationActivity, "✅ Evaluation Created Successfully!", Toast.LENGTH_SHORT).show()
                if (res.evaluation?.roleUpgraded == true) {
                    Toast.makeText(this@CreateEvaluationActivity, "🎉 Account upgraded! You are now a FACILITATOR.", Toast.LENGTH_LONG).show()
                }
                finish()
            }.onFailure { e ->
                binding.btnNext.isEnabled = true
                binding.btnNext.text = "Publish Evaluation"
                Toast.makeText(this@CreateEvaluationActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
