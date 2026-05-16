package com.example.peertayo_mobile.evaluation

import com.example.peertayo_mobile.data.model.UserResponse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CreateEvaluationViewModelTest {

    private lateinit var viewModel: CreateEvaluationViewModel
    private val user1 = UserResponse(1, "Alice", "Smith", "alice@example.com", "USER", null, null)
    private val user2 = UserResponse(2, "Bob", "Jones", "bob@example.com", "USER", null, null)

    @Before
    fun setup() {
        viewModel = CreateEvaluationViewModel()
    }

    @Test
    fun `test addEvaluator automatically removes from evaluatees`() {
        // Given user1 is in evaluatees
        viewModel.addEvaluatees(listOf(user1))
        assertTrue(viewModel.evaluatees.value?.any { it.id == 1L } == true)

        // When user1 is added to evaluators
        viewModel.addEvaluators(listOf(user1))

        // Then user1 should be removed from evaluatees
        assertFalse(viewModel.evaluatees.value?.any { it.id == 1L } == true)
        assertTrue(viewModel.evaluators.value?.any { it.id == 1L } == true)
    }

    @Test
    fun `test addEvaluatee automatically removes from evaluators`() {
        // Given user1 is in evaluators
        viewModel.addEvaluators(listOf(user1))
        assertTrue(viewModel.evaluators.value?.any { it.id == 1L } == true)

        // When user1 is added to evaluatees
        viewModel.addEvaluatees(listOf(user1))

        // Then user1 should be removed from evaluators
        assertFalse(viewModel.evaluators.value?.any { it.id == 1L } == true)
        assertTrue(viewModel.evaluatees.value?.any { it.id == 1L } == true)
    }

    @Test
    fun `test getOverlapCount is always zero due to automated exclusion`() {
        viewModel.addEvaluators(listOf(user1))
        viewModel.addEvaluatees(listOf(user2))
        
        assertEquals(0, viewModel.getOverlapCount())
        
        // Try to force overlap (though UI logic prevents it)
        viewModel.addEvaluatees(listOf(user1))
        
        assertEquals(0, viewModel.getOverlapCount())
    }
}
