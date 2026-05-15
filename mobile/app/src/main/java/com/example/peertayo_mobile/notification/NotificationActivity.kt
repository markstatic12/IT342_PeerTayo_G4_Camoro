package com.example.peertayo_mobile.notification

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.peertayo_mobile.databinding.ActivityNotificationsBinding

class NotificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNotificationsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNotificationsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }

        binding.btnMarkAll.setOnClickListener {
            // TODO: Mark all notifications as read via API
        }

        // Show empty state for now — no notification API endpoint wired yet
        binding.rvNotifications.visibility = View.GONE
        binding.emptyState.visibility = View.VISIBLE
    }
}
