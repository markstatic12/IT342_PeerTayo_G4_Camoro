package com.example.peertayo_mobile.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertayo_mobile.data.model.PendingEvaluation
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import kotlinx.coroutines.launch
import java.util.Date
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Grouped pending form — mirrors web's client-side groupByForm() logic.
 * Each PendingForm represents one evaluation form with multiple evaluatees.
 */
data class PendingEvaluatee(
    val assignmentId: Long,
    val name: String,
    val evaluateeId: Long
)

data class PendingForm(
    val id: Long,            // evaluationId (the form)
    val title: String,
    val deadline: String?,
    val evaluatees: List<PendingEvaluatee>
) {
    /** Days left until deadline. Negative means overdue/missed. */
    fun daysLeft(): Long? {
        val dl = deadline ?: return null
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val d = fmt.parse(dl) ?: return null
            val diff = d.time - Date().time
            TimeUnit.MILLISECONDS.toDays(diff)
        } catch (_: Exception) { null }
    }

    fun isUrgent(): Boolean {
        val d = daysLeft() ?: return false
        return d in 0..3
    }

    fun isMissed(): Boolean {
        val d = daysLeft() ?: return false
        return d < 0
    }
}

sealed class PendingState {
    object Loading : PendingState()
    data class Success(
        val forms: List<PendingForm>,
        val totalPending: Int,
        val urgentCount: Int,
        val submittedThisMonth: Int
    ) : PendingState()
    data class Error(val message: String) : PendingState()
}

class PendingViewModel(private val repository: EvaluationRepository) : ViewModel() {

    private val _state = MutableLiveData<PendingState>()
    val state: LiveData<PendingState> = _state

    private var allForms: List<PendingForm> = emptyList()
    private var submittedThisMonth: Int = 0

    init {
        loadPending()
    }

    fun loadPending() {
        viewModelScope.launch {
            _state.value = PendingState.Loading
            try {
                val pendingResult = repository.listPendingEvaluations()
                val summaryResult = repository.getSubmittedSummary()

                val rawList = pendingResult.getOrNull() ?: emptyList()
                submittedThisMonth = summaryResult.getOrNull()?.submittedThisMonth ?: 0

                allForms = groupByForm(rawList)
                emitFiltered("all")
            } catch (e: Exception) {
                _state.value = PendingState.Error(e.message ?: "Failed to load")
            }
        }
    }

    fun filter(type: String) {
        emitFiltered(type)
    }

    private fun emitFiltered(type: String) {
        val filtered = when (type) {
            "urgent" -> allForms.filter { it.isUrgent() }
            "missed" -> allForms.filter { it.isMissed() }
            else     -> allForms
        }
        _state.value = PendingState.Success(
            forms = filtered,
            totalPending = allForms.sumOf { it.evaluatees.size },
            urgentCount = allForms.count { it.isUrgent() },
            submittedThisMonth = submittedThisMonth
        )
    }

    /** Mirrors web's groupByForm() — groups flat API list by evaluation form id. */
    private fun groupByForm(flat: List<PendingEvaluation>): List<PendingForm> {
        val map = LinkedHashMap<Long, PendingForm>()
        for (item in flat) {
            val formId = item.evaluationId
            val evaluatee = PendingEvaluatee(
                assignmentId = item.id,
                name = item.evaluateeName,
                evaluateeId = item.evaluateeId
            )
            val existing = map[formId]
            if (existing == null) {
                map[formId] = PendingForm(
                    id = formId,
                    title = item.title,
                    deadline = item.deadline,
                    evaluatees = listOf(evaluatee)
                )
            } else {
                map[formId] = existing.copy(evaluatees = existing.evaluatees + evaluatee)
            }
        }
        return map.values.toList()
    }
}
