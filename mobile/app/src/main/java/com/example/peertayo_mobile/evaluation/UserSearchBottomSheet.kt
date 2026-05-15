package com.example.peertayo_mobile.evaluation

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peertayo_mobile.data.api.RetrofitClient
import com.example.peertayo_mobile.data.model.UserResponse
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import com.example.peertayo_mobile.databinding.BottomSheetUserSearchBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class UserSearchBottomSheet(
    private val onUsersSelected: (List<UserResponse>) -> Unit
) : BottomSheetDialogFragment() {

    private var _binding: BottomSheetUserSearchBinding? = null
    private val binding get() = _binding!!
    private val repository by lazy { EvaluationRepository(RetrofitClient.evaluationApi) }
    private lateinit var searchAdapter: SearchAdapter
    private var searchJob: Job? = null
    private val selectedUsers = mutableListOf<UserResponse>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetUserSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupList()
        setupSearch()
        binding.btnConfirm.setOnClickListener {
            onUsersSelected(selectedUsers)
            dismiss()
        }
    }

    private fun setupList() {
        searchAdapter = SearchAdapter { user, isSelected ->
            if (isSelected) selectedUsers.add(user) else selectedUsers.removeIf { it.id == user.id }
            binding.btnConfirm.isEnabled = selectedUsers.isNotEmpty()
            binding.btnConfirm.text = if (selectedUsers.isEmpty()) "Select Users" else "Add ${selectedUsers.size} Users"
        }
        binding.rvSearchResults.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSearchResults.adapter = searchAdapter
    }

    private fun setupSearch() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val query = s.toString().trim()
                if (query.isNotEmpty()) {
                    performSearch(query)
                } else {
                    searchJob?.cancel()
                    searchAdapter.submitList(emptyList())
                    binding.tvEmpty.visibility = View.GONE
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun performSearch(query: String) {
        searchJob?.cancel()
        searchJob = lifecycleScope.launch {
            delay(300) // Debounce
            binding.progressBar.visibility = View.VISIBLE
            repository.searchUsers(query).onSuccess { users ->
                binding.progressBar.visibility = View.GONE
                searchAdapter.submitList(users)
                binding.tvEmpty.visibility = if (users.isEmpty()) View.VISIBLE else View.GONE
            }.onFailure {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(requireContext(), "Search failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
