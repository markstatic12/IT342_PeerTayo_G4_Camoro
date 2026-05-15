package com.example.peertayo_mobile.evaluation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.data.api.RetrofitClient
import com.example.peertayo_mobile.data.model.Criterion
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import com.example.peertayo_mobile.databinding.ActivityEvaluateFormBinding

class EvaluateFormActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEvaluateFormBinding
    private lateinit var viewModel: EvaluateFormViewModel

    private var evalId: Long = 0L
    private var evalTitle: String = ""
    private var evaluateeName: String = ""
    private var deadline: String = ""

    // Default criteria when none are provided by the API
    private val defaultCriteria = listOf(
        Criterion(1, "Communication", "Ability to communicate ideas clearly"),
        Criterion(2, "Teamwork", "Contributes effectively to team goals"),
        Criterion(3, "Leadership", "Takes initiative and guides others"),
        Criterion(4, "Problem Solving", "Identifies and resolves issues"),
        Criterion(5, "Creativity", "Brings innovative ideas and approaches"),
        Criterion(6, "Time Management", "Meets deadlines and manages time well"),
        Criterion(7, "Adaptability", "Adjusts to changing situations"),
        Criterion(8, "Work Ethic", "Shows dedication and commitment"),
        Criterion(9, "Technical Skills", "Demonstrates relevant technical ability"),
        Criterion(10, "Professionalism", "Maintains professional conduct")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEvaluateFormBinding.inflate(layoutInflater)
        setContentView(binding.root)

        RetrofitClient.init(this)

        // Extract intent data
        evalId = intent.getLongExtra("EVAL_ID", 0L)
        evalTitle = intent.getStringExtra("EVAL_TITLE") ?: "Evaluation"
        evaluateeName = intent.getStringExtra("EVALUATEE_NAME") ?: ""
        deadline = intent.getStringExtra("DEADLINE") ?: ""

        setupViewModel()
        setupHeader()
        setupCriteria()
        setupActions()
        observeViewModel()
    }

    private fun setupViewModel() {
        val repository = EvaluationRepository(RetrofitClient.evaluationApi)
        val factory = object : ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return EvaluateFormViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[EvaluateFormViewModel::class.java]
    }

    private fun setupHeader() {
        binding.tvEvalTitle.text = evalTitle
        binding.tvEvaluatee.text = "Evaluating: $evaluateeName"
        binding.tvDeadline.text = if (deadline.isNotEmpty()) "Due: $deadline" else ""
    }

    private fun setupCriteria() {
        // TODO: In a full implementation, criteria would come from the API via the pending evaluation
        // For now, use default criteria
        val criteria = defaultCriteria
        viewModel.setTotalCriteria(criteria.size)

        val inflater = LayoutInflater.from(this)
        criteria.forEach { criterion ->
            val row = inflater.inflate(R.layout.item_rating_row, binding.criteriaContainer, false)

            row.findViewById<TextView>(R.id.tvCriterionName).text = criterion.name

            val descView = row.findViewById<TextView>(R.id.tvCriterionDesc)
            if (!criterion.description.isNullOrBlank()) {
                descView.text = criterion.description
                descView.visibility = View.VISIBLE
            }

            // Setup rating buttons
            val buttons = listOf(
                row.findViewById<TextView>(R.id.btnRate1),
                row.findViewById<TextView>(R.id.btnRate2),
                row.findViewById<TextView>(R.id.btnRate3),
                row.findViewById<TextView>(R.id.btnRate4),
                row.findViewById<TextView>(R.id.btnRate5)
            )

            buttons.forEachIndexed { index, btn ->
                val score = index + 1
                btn.setOnClickListener {
                    viewModel.setRating(criterion.id, score)
                    // Update visuals
                    buttons.forEach { b ->
                        b.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
                        b.setBackgroundResource(R.drawable.bg_rating_btn)
                    }
                    btn.setTextColor(ContextCompat.getColor(this, R.color.text_on_cyan))
                    btn.setBackgroundColor(ContextCompat.getColor(this, R.color.cyan_primary))
                }

                // Restore selection if already rated
                val existing = viewModel.getRating(criterion.id)
                if (existing == score) {
                    btn.setTextColor(ContextCompat.getColor(this, R.color.text_on_cyan))
                    btn.setBackgroundColor(ContextCompat.getColor(this, R.color.cyan_primary))
                }
            }

            binding.criteriaContainer.addView(row)
        }
    }

    private fun setupActions() {
        binding.btnBack.setOnClickListener { finish() }

        binding.btnSubmit.setOnClickListener {
            val comment = binding.etComment.text?.toString()
            viewModel.submit(evalId, comment)
        }
    }

    private fun observeViewModel() {
        viewModel.answeredCount.observe(this) { count ->
            val total = viewModel.totalCriteria
            binding.tvProgress.text = "$count / $total"
            binding.btnSubmit.isEnabled = viewModel.isComplete()
        }

        viewModel.submitState.observe(this) { state ->
            when (state) {
                is EvalFormState.Idle -> { /* nothing */ }
                is EvalFormState.Submitting -> {
                    binding.btnSubmit.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                }
                is EvalFormState.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, "✅ Evaluation submitted!", Toast.LENGTH_LONG).show()
                    finish()
                }
                is EvalFormState.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSubmit.isEnabled = true
                    Toast.makeText(this, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}
