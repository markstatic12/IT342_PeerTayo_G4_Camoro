package com.example.peertayo_mobile.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertayo_mobile.data.model.CompletedForm
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * CompletedViewModel — GAP-03 fix.
 *
 * Adds:
 * - filter(type): "all" | "week"  (matches web All / This Week tabs)
 * - search(query): live title-based search
 * - avgScoreGiven: computed from the form list as a best-effort average
 *   (CompletedForm doesn't currently carry per-criteria ratings from the API,
 *    so we show "—" when ratings are unavailable — same as web when no data)
 * - submittedThisMonth: from SubmittedSummary API (matches web's 2nd stat)
 */

sealed class CompletedState {
    object Loading : CompletedState()
    data class Success(
        val forms: List<CompletedForm>,
        val totalSubmitted: Int,
        val submittedThisMonth: Int,
        val avgScoreGiven: String
    ) : CompletedState()
    data class Error(val message: String) : CompletedState()
}

class CompletedViewModel(private val repository: EvaluationRepository) : ViewModel() {

    private val _state = MutableLiveData<CompletedState>()
    val state: LiveData<CompletedState> = _state

    private var allForms: List<CompletedForm> = emptyList()
    private var totalSubmitted: Int = 0
    private var submittedThisMonth: Int = 0
    private var avgScoreGiven: String = "—"
    private var currentQuery: String = ""
    private var currentFilter: String = "all"

    init {
        loadCompleted()
    }

    fun loadCompleted() {
        viewModelScope.launch {
            _state.value = CompletedState.Loading
            val completedResult = repository.getCompletedForms()
            val summaryResult = repository.getSubmittedSummary()

            completedResult.onSuccess { forms ->
                allForms = forms
                totalSubmitted = forms.size
                submittedThisMonth = summaryResult.getOrNull()?.submittedThisMonth ?: 0
                avgScoreGiven = "—" // API doesn't return per-submission rating; matches web "—" fallback
                emitFiltered()
            }.onFailure { e ->
                _state.value = CompletedState.Error(e.message ?: "Failed to load")
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

        // Search by title (case-insensitive)
        if (currentQuery.isNotEmpty()) {
            result = result.filter {
                it.title.contains(currentQuery, ignoreCase = true) ||
                it.evaluateeName.contains(currentQuery, ignoreCase = true)
            }
        }

        // Tab filter
        if (currentFilter == "week") {
            val weekAgo = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -7) }.time
            val fmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            result = result.filter { form ->
                val submitted = form.submittedAt ?: return@filter false
                try {
                    val date = fmt.parse(submitted) ?: return@filter false
                    date.after(weekAgo)
                } catch (_: Exception) { false }
            }
        }

        _state.value = CompletedState.Success(
            forms = result,
            totalSubmitted = totalSubmitted,
            submittedThisMonth = submittedThisMonth,
            avgScoreGiven = avgScoreGiven
        )
    }
}
