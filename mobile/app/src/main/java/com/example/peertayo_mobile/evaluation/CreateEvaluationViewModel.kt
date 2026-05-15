package com.example.peertayo_mobile.evaluation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.peertayo_mobile.data.model.UserResponse

class CreateEvaluationViewModel : ViewModel() {

    // Step 1: Details
    val title = MutableLiveData<String>("")
    val description = MutableLiveData<String>("")
    val deadline = MutableLiveData<String>("")

    // Step 2: Participants
    private val _evaluators = MutableLiveData<List<UserResponse>>(emptyList())
    val evaluators: LiveData<List<UserResponse>> = _evaluators

    private val _evaluatees = MutableLiveData<List<UserResponse>>(emptyList())
    val evaluatees: LiveData<List<UserResponse>> = _evaluatees

    // Navigation
    private val _currentStep = MutableLiveData<Int>(0)
    val currentStep: LiveData<Int> = _currentStep

    fun setStep(step: Int) {
        _currentStep.value = step
    }

    fun addEvaluators(users: List<UserResponse>) {
        val current = _evaluators.value ?: emptyList()
        val newOnes = users.filter { newUser -> current.none { it.id == newUser.id } }
        _evaluators.value = current + newOnes
    }

    fun removeEvaluator(userId: Long) {
        _evaluators.value = _evaluators.value?.filter { it.id != userId }
    }

    fun addEvaluatees(users: List<UserResponse>) {
        val current = _evaluatees.value ?: emptyList()
        val newOnes = users.filter { newUser -> current.none { it.id == newUser.id } }
        _evaluatees.value = current + newOnes
    }

    fun removeEvaluatee(userId: Long) {
        _evaluatees.value = _evaluatees.value?.filter { it.id != userId }
    }

    fun isValidStep(step: Int): Boolean {
        return when (step) {
            0 -> !title.value.isNullOrBlank() && !deadline.value.isNullOrBlank()
            1 -> true // Criteria Overview is read-only
            2 -> !evaluators.value.isNullOrEmpty() && !evaluatees.value.isNullOrEmpty()
            else -> true
        }
    }
}
