package com.example.peertayo_mobile.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.peertayo_mobile.data.model.SubmittedSummary
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import kotlinx.coroutines.launch

data class DashboardData(
    val pendingCount: Int = 0,
    val averageScore: Double = 0.0,
    val submittedCount: Int = 0,
    val submittedThisMonth: Int = 0,
    val attentionCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class DashboardViewModel(private val repository: EvaluationRepository) : ViewModel() {

    private val _state = MutableLiveData<DashboardData>()
    val state: LiveData<DashboardData> = _state

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _state.value = _state.value?.copy(isLoading = true) ?: DashboardData(isLoading = true)
            
            try {
                // Fetch pending evaluations to get counts
                val pendingResult = repository.listPendingEvaluations()
                val pendingList = pendingResult.getOrNull() ?: emptyList()
                val pendingCount = pendingList.size
                
                // Deadline-based urgency: count items due within 2 days (matches web pendingDue)
                val now = System.currentTimeMillis()
                val twoDays = 2L * 24 * 60 * 60 * 1000
                val attentionCount = pendingList.count { eval ->
                    val dl = eval.deadline ?: return@count false
                    try {
                        val fmt = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.getDefault())
                        val d = fmt.parse(dl) ?: return@count false
                        val diff = d.time - now
                        diff in 0..twoDays
                    } catch (_: Exception) { false }
                }

                // Fetch submitted summary
                val summaryResult = repository.getSubmittedSummary()
                val summary = summaryResult.getOrNull()
                val submittedCount = summary?.totalSubmitted ?: 0
                val submittedThisMonth = summary?.submittedThisMonth ?: 0

                // Fetch results for average
                val resultsResult = repository.getMyResults()
                val averageScore = resultsResult.getOrNull()?.overallAverage ?: 0.0

                _state.value = DashboardData(
                    pendingCount = pendingCount,
                    averageScore = averageScore,
                    submittedCount = submittedCount,
                    submittedThisMonth = submittedThisMonth,
                    attentionCount = attentionCount,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _state.value = _state.value?.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load dashboard data"
                )
            }
        }
    }
}
