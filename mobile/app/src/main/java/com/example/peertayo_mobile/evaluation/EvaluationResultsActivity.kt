package com.example.peertayo_mobile.evaluation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peertayo_mobile.data.api.RetrofitClient
import com.example.peertayo_mobile.data.model.CreatedEvaluation
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import com.example.peertayo_mobile.databinding.ActivityEvaluationResultsBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class EvaluationResultsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEvaluationResultsBinding
    private val repository = EvaluationRepository(RetrofitClient.evaluationApi)
    private var evaluationId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEvaluationResultsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        evaluationId = intent.getLongExtra("EVALUATION_ID", -1)
        if (evaluationId == -1L) {
            finish()
            return
        }

        setupUI()
        loadData()
    }

    private fun setupUI() {
        binding.btnBack.setOnClickListener { finish() }
        
        binding.btnExtend.setOnClickListener { showDateTimePicker() }
        
        binding.btnClosePermanently.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Close Permanently?")
                .setMessage("This evaluation will stay closed with zero responses. This cannot be undone.")
                .setPositiveButton("Yes, Close") { _, _ -> closePermanently() }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun loadData() {
        binding.progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            repository.listCreatedEvaluations().onSuccess { list ->
                val eval = list.find { it.id == evaluationId }
                if (eval != null) {
                    updateHeader(eval)
                    checkZeroSubmissionRule(eval)
                }
            }.onFailure {
                Toast.makeText(this@EvaluationResultsActivity, "Failed to load evaluation", Toast.LENGTH_SHORT).show()
            }
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun updateHeader(eval: CreatedEvaluation) {
        binding.tvTitle.text = eval.title
        binding.tvMeta.text = "Status: ${eval.status} • ${eval.submissionCount ?: 0}/${eval.totalExpectedSubmissions ?: 0} submitted"
    }

    private fun checkZeroSubmissionRule(eval: CreatedEvaluation) {
        // BR-004 logic
        val isClosed = eval.status?.uppercase() == "CLOSED"
        val hasZeroSubmissions = (eval.submissionCount ?: 0) == 0
        val isPermanentlyClosed = eval.permanentlyClosed ?: false

        if (isClosed && hasZeroSubmissions && !isPermanentlyClosed) {
            binding.cardZeroAlert.visibility = View.VISIBLE
        } else {
            binding.cardZeroAlert.visibility = View.GONE
        }
    }

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                val selected = Calendar.getInstance().apply {
                    set(year, month, day, hour, minute)
                }
                if (selected.after(Calendar.getInstance())) {
                    extendDeadline(selected.time)
                } else {
                    Toast.makeText(this, "Deadline must be in the future", Toast.LENGTH_SHORT).show()
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun extendDeadline(date: Date) {
        val isoFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val dateStr = isoFmt.format(date)

        lifecycleScope.launch {
            repository.extendDeadline(evaluationId, dateStr).onSuccess {
                Toast.makeText(this@EvaluationResultsActivity, "Deadline extended", Toast.LENGTH_SHORT).show()
                loadData()
            }.onFailure {
                Toast.makeText(this@EvaluationResultsActivity, "Failed to extend: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun closePermanently() {
        lifecycleScope.launch {
            repository.closePermanently(evaluationId).onSuccess {
                Toast.makeText(this@EvaluationResultsActivity, "Closed permanently", Toast.LENGTH_SHORT).show()
                loadData()
            }.onFailure {
                Toast.makeText(this@EvaluationResultsActivity, "Failed to close: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
