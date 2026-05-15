package com.example.peertayo_mobile.evaluation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertayo_mobile.data.model.Criterion
import com.example.peertayo_mobile.data.model.PendingEvaluation
import com.example.peertayo_mobile.data.model.RatingEntry
import com.example.peertayo_mobile.data.model.SubmitEvaluationRequest
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import kotlinx.coroutines.launch

sealed class EvalFormState {
    object Idle : EvalFormState()
    object Submitting : EvalFormState()
    object Success : EvalFormState()
    data class Error(val message: String) : EvalFormState()
}

class EvaluateFormViewModel(private val repository: EvaluationRepository) : ViewModel() {

    private val _submitState = MutableLiveData<EvalFormState>(EvalFormState.Idle)
    val submitState: LiveData<EvalFormState> = _submitState

    // Map of criterionId -> selected score (1-5)
    private val _ratings = mutableMapOf<Long, Int>()

    private val _answeredCount = MutableLiveData(0)
    val answeredCount: LiveData<Int> = _answeredCount

    var totalCriteria: Int = 0
        private set

    fun setTotalCriteria(count: Int) {
        totalCriteria = count
    }

    fun setRating(criterionId: Long, score: Int) {
        _ratings[criterionId] = score
        _answeredCount.value = _ratings.size
    }

    fun getRating(criterionId: Long): Int? = _ratings[criterionId]

    fun isComplete(): Boolean = _ratings.size >= totalCriteria && totalCriteria > 0

    fun submit(evaluationId: Long, comment: String?) {
        if (!isComplete()) return

        viewModelScope.launch {
            _submitState.value = EvalFormState.Submitting
            val entries = _ratings.map { (criteriaId, score) ->
                RatingEntry(criteriaId = criteriaId, score = score)
            }
            val request = SubmitEvaluationRequest(
                ratings = entries,
                comment = comment?.takeIf { it.isNotBlank() }
            )
            val result = repository.submitEvaluation(evaluationId, request)
            result.onSuccess {
                _submitState.value = EvalFormState.Success
            }.onFailure { e ->
                _submitState.value = EvalFormState.Error(e.message ?: "Submission failed")
            }
        }
    }
}
