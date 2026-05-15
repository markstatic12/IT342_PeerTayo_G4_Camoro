package com.example.peertayo_mobile.notification

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.peertayo_mobile.data.api.RetrofitClient
import com.example.peertayo_mobile.data.repository.EvaluationRepository
import com.example.peertayo_mobile.databinding.ActivityNotificationsBinding
import kotlinx.coroutines.launch

/**
 * NotificationActivity — GAP-10 fix.
 *
 * Replaced static placeholder with actual API-fetched notifications.
 * Wires to NotificationAdapter and EvaluationRepository.listNotifications().
 */
class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding
    private lateinit var adapter: NotificationAdapter
    private val repository = EvaluationRepository(RetrofitClient.evaluationApi)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        loadNotifications()

        binding.btnBack.setOnClickListener { finish() }

        binding.btnMarkAll.setOnClickListener {
            markAllAsRead()
        }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter()
        binding.rvNotifications.layoutManager = LinearLayoutManager(this)
        binding.rvNotifications.adapter = adapter
    }

    private fun loadNotifications() {
        lifecycleScope.launch {
            val result = repository.listNotifications()
            result.onSuccess { notifications ->
                if (notifications.isNullOrEmpty()) {
                    binding.rvNotifications.visibility = View.GONE
                    binding.emptyState.visibility = View.VISIBLE
                } else {
                    binding.rvNotifications.visibility = View.VISIBLE
                    binding.emptyState.visibility = View.GONE
                    adapter.submitList(notifications)
                }
            }.onFailure {
                binding.rvNotifications.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            }
        }
    }

    private fun markAllAsRead() {
        lifecycleScope.launch {
            repository.markNotificationsRead().onSuccess {
                loadNotifications() // Refresh list
            }
        }
    }
}
