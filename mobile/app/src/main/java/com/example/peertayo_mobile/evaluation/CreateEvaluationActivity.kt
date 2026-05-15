package com.example.peertayo_mobile.evaluation

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.peertayo_mobile.data.api.RetrofitClient
import com.example.peertayo_mobile.data.model.CreateEvaluationRequest
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import com.example.peertayo_mobile.databinding.ActivityCreateEvaluationBinding
import kotlinx.coroutines.launch

class CreateEvaluationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCreateEvaluationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateEvaluationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        RetrofitClient.init(this)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnPublish.setOnClickListener {
            publish()
        }
    }

    private fun publish() {
        val title = binding.etTitle.text?.toString()?.trim() ?: ""
        val description = binding.etDescription.text?.toString()?.trim()
        val deadline = binding.etDeadline.text?.toString()?.trim() ?: ""
        val evaluatorStr = binding.etEvaluators.text?.toString()?.trim() ?: ""
        val evaluateeStr = binding.etEvaluatees.text?.toString()?.trim() ?: ""

        if (title.isEmpty()) {
            Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (deadline.isEmpty()) {
            Toast.makeText(this, "Deadline is required", Toast.LENGTH_SHORT).show()
            return
        }

        val evaluatorIds = evaluatorStr.split(",")
            .mapNotNull { it.trim().toLongOrNull() }
        val evaluateeIds = evaluateeStr.split(",")
            .mapNotNull { it.trim().toLongOrNull() }

        if (evaluatorIds.isEmpty()) {
            Toast.makeText(this, "At least one evaluator ID is required", Toast.LENGTH_SHORT).show()
            return
        }
        if (evaluateeIds.isEmpty()) {
            Toast.makeText(this, "At least one evaluatee ID is required", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnPublish.isEnabled = false

        val repository = EvaluationRepository(RetrofitClient.evaluationApi)
        lifecycleScope.launch {
            val request = CreateEvaluationRequest(
                title = title,
                description = description?.takeIf { it.isNotBlank() },
                deadline = deadline,
                evaluatorIds = evaluatorIds,
                evaluateeIds = evaluateeIds
            )
            val result = repository.createEvaluation(request)
            result.onSuccess {
                Toast.makeText(this@CreateEvaluationActivity, "✅ Evaluation published!", Toast.LENGTH_LONG).show()
                finish()
            }.onFailure { e ->
                binding.btnPublish.isEnabled = true
                Toast.makeText(this@CreateEvaluationActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
