package com.example.peertayo_mobile.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertayo_mobile.data.model.CreatedEvaluation
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * FormsViewModel — GAP-04 fix.
 *
 * Added:
 * - 4-stat strip: Total / Active / Needs Attention / Closed  (matches web FormsCreatedPage)
 * - filter(type): "all" | "active" | "attention" | "closed"
 * - search(query): live title-based search
 * - "Needs Attention" = overdue (deadline passed, status still ACTIVE)
 * - submissionProgress formatted as "N/M" string in the adapter (GAP-11)
 */

data class FormsStats(
    val total: Int,
    val active: Int,
    val needsAttention: Int,
    val closed: Int
)

sealed class FormsState {
    object Loading : FormsState()
    data class Success(
        val forms: List<CreatedEvaluation>,
        val stats: FormsStats
    ) : FormsState()
    data class Error(val message: String) : FormsState()
}

class FormsViewModel(private val repository: EvaluationRepository) : ViewModel() {

    private val _state = MutableLiveData<FormsState>()
    val state: LiveData<FormsState> = _state

    private var allForms: List<CreatedEvaluation> = emptyList()
    private var cachedStats = FormsStats(0, 0, 0, 0)
    private var currentQuery: String = ""
    private var currentFilter: String = "all"

    init {
        loadForms()
    }

    fun loadForms() {
        viewModelScope.launch {
            _state.value = FormsState.Loading
            val result = repository.listCreatedEvaluations()
            result.onSuccess { list ->
                allForms = list
                cachedStats = computeStats(list)
                emitFiltered()
            }.onFailure { e ->
                _state.value = FormsState.Error(e.message ?: "Failed to load forms")
            }
        }
    }

    fun filter(type: String) {
        currentFilter = type
        emitFiltered()
    }

    fun search(query: String) {
        currentQuery = query.trim()
        emitFiltered()
    }

    private fun emitFiltered() {
        var result = allForms

        // Search by title
        if (currentQuery.isNotEmpty()) {
            result = result.filter { it.title.contains(currentQuery, ignoreCase = true) }
        }

        // Tab filter
        result = when (currentFilter) {
            "active"    -> result.filter { it.status?.uppercase() == "ACTIVE" && !isOverdue(it) }
            "attention" -> result.filter { isNeedsAttention(it) }
            "closed"    -> result.filter { it.status?.uppercase() == "CLOSED" }
            else        -> result
        }

        _state.value = FormsState.Success(forms = result, stats = cachedStats)
    }

    /** Needs Attention = ACTIVE status but deadline has passed (overdue). */
    private fun isNeedsAttention(form: CreatedEvaluation): Boolean {
        val status = form.status?.uppercase() ?: return false
        if (status != "ACTIVE") return false
        return isOverdue(form)
    }

    private fun isOverdue(form: CreatedEvaluation): Boolean {
        val dl = form.deadline ?: return false
        return try {
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val d = fmt.parse(dl) ?: return false
            d.before(Date())
        } catch (_: Exception) { false }
    }

    private fun computeStats(forms: List<CreatedEvaluation>): FormsStats {
        val active = forms.count { it.status?.uppercase() == "ACTIVE" && !isOverdue(it) }
        val attention = forms.count { isNeedsAttention(it) }
        val closed = forms.count { it.status?.uppercase() == "CLOSED" }
        return FormsStats(
            total = forms.size,
            active = active,
            needsAttention = attention,
            closed = closed
        )
    }
    fun deleteEvaluation(id: Long) {
        viewModelScope.launch {
            repository.deleteEvaluation(id).onSuccess {
                loadForms()
            }
        }
    }
}
