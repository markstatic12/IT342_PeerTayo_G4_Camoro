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
    data class Success(val results: MyResults?, val evaluations: List<EvaluationResultSummary>) : ResultsState()
    data class Error(val message: String) : ResultsState()
}

class ResultsViewModel(private val repository: EvaluationRepository) : ViewModel() {

    private val _state = MutableLiveData<ResultsState>()
    val state: LiveData<ResultsState> = _state

    init {
        loadResults()
    }

    fun loadResults() {
        viewModelScope.launch {
            _state.value = ResultsState.Loading
            val result = repository.getMyResults()
            result.onSuccess { myResults ->
                _state.value = ResultsState.Success(
                    results = myResults,
                    evaluations = myResults?.evaluations ?: emptyList()
                )
            }.onFailure { e ->
                _state.value = ResultsState.Error(e.message ?: "Failed to load results")
            }
        }
    }
}
