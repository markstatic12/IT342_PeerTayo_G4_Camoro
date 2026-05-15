package com.example.peertayo_mobile.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertayo_mobile.data.model.CreatedEvaluation
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import kotlinx.coroutines.launch

sealed class FormsState {
    object Loading : FormsState()
    data class Success(val forms: List<CreatedEvaluation>) : FormsState()
    data class Error(val message: String) : FormsState()
}

class FormsViewModel(private val repository: EvaluationRepository) : ViewModel() {

    private val _state = MutableLiveData<FormsState>()
    val state: LiveData<FormsState> = _state

    init {
        loadForms()
    }

    fun loadForms() {
        viewModelScope.launch {
            _state.value = FormsState.Loading
            val result = repository.listCreatedEvaluations()
            result.onSuccess { list ->
                _state.value = FormsState.Success(list)
            }.onFailure { e ->
                _state.value = FormsState.Error(e.message ?: "Failed to load forms")
            }
        }
    }
}
