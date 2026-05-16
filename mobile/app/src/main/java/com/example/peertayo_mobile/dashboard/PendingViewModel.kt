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
import com.example.peertayo_mobile.data.local.SessionManager


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
    val creatorName: String?,
    val evaluatees: List<PendingEvaluatee>
) {
    /** Precise check if the deadline has passed. */
    fun isMissed(): Boolean {
        val dl = deadline ?: return false
        return try {
            // Support full ISO formats (2026-05-16T23:59:00)
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val d = fmt.parse(dl) ?: return false
            Date().after(d) // True if current time is past the deadline
        } catch (_: Exception) { false }
    }

    /** Days left until deadline. Used for the "Urgent" pill. */
    fun daysLeft(): Long? {
        val dl = deadline ?: return null
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
            val d = fmt.parse(dl) ?: return null
            val diff = d.time - Date().time
            // Use precise division to ensure even partial days show as 0+
            TimeUnit.MILLISECONDS.toDays(diff)
        } catch (_: Exception) { null }
    }

    fun isUrgent(): Boolean {
        if (isMissed()) return false
        val d = daysLeft() ?: return false
        return d in 0..1 // Match web's urgency feel (2 days or less)
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


class PendingViewModel(
    private val repository: EvaluationRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

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
                val currentUserId = sessionManager.getUserId()
                
                // Defensive: Filter out self-evaluations even if returned by backend
                val filteredList = rawList.filter { it.evaluateeId != currentUserId }
                
                submittedThisMonth = summaryResult.getOrNull()?.submittedThisMonth ?: 0

                allForms = groupByForm(filteredList)
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
                    creatorName = item.creatorName,
                    evaluatees = listOf(evaluatee)
                )
            } else {
                map[formId] = existing.copy(evaluatees = existing.evaluatees + evaluatee)
            }
        }
        return map.values.toList()
    }
}
