package com.example.peertayo_mobile.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertayo_mobile.data.model.EvaluationResultSummary
import com.example.peertayo_mobile.data.model.MyResults
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import kotlinx.coroutines.launch

sealed class ResultsState {
    object Loading : ResultsState()
    data class Success(
        val results: MyResults?,
        val evaluations: List<EvaluationResultSummary>,
        /** Highest-scoring criterion name — matches web's "Highest Criterion" stat card. */
        val highestCriterion: String
    ) : ResultsState()
    data class Error(val message: String) : ResultsState()
}

/**
 * ResultsViewModel — GAP-01 fix.
 *
 * Added:
 * - [filter] method supporting "all" / "highest" / "recent" tabs (matches web filter tabs)
 * - [highestCriterion] computed from questionAverages (4th stat card on web)
 * - Full master list is kept so filtering doesn't require a network call
 */
class ResultsViewModel(private val repository: EvaluationRepository) : ViewModel() {

    private val _state = MutableLiveData<ResultsState>()
    val state: LiveData<ResultsState> = _state

    private var allEvaluations: List<EvaluationResultSummary> = emptyList()
    private var cachedResults: MyResults? = null
    private var cachedHighestCriterion: String = "—"

    init {
        loadResults()
    }

    fun loadResults() {
        viewModelScope.launch {
            _state.value = ResultsState.Loading
            val result = repository.getMyResults()
            result.onSuccess { myResults ->
                allEvaluations = myResults?.evaluations ?: emptyList()
                cachedResults = myResults

                // Determine highest-scoring criterion from overall questionAverages
                cachedHighestCriterion = myResults?.questionAverages
                    ?.maxByOrNull { it.average }
                    ?.criteriaName ?: "—"

                emitFiltered("all")
            }.onFailure { e ->
                _state.value = ResultsState.Error(e.message ?: "Failed to load results")
            }
        }
    }

    /**
     * Filter the displayed list.
     * - "all"     → original order
     * - "highest" → sorted by overallAverage descending
     * - "recent"  → sorted by submittedAt descending (most recent first)
     */
    fun filter(type: String) {
        emitFiltered(type)
    }

    private fun emitFiltered(type: String) {
        val filtered = when (type) {
            "highest" -> allEvaluations.sortedByDescending { it.overallAverage ?: 0.0 }
            "recent"  -> allEvaluations.sortedByDescending { it.submittedAt ?: "" }
            else      -> allEvaluations
        }
        _state.value = ResultsState.Success(
            results = cachedResults,
            evaluations = filtered,
            highestCriterion = cachedHighestCriterion
        )
    }
}
