package com.example.peertayo_mobile.notification

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.data.model.NotificationItem
import com.example.peertayo_mobile.databinding.ItemNotificationBinding

class NotificationAdapter : ListAdapter<NotificationItem, NotificationAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<NotificationItem>() {
            override fun areItemsTheSame(a: NotificationItem, b: NotificationItem) = a.message == b.message && a.timeAgo == b.timeAgo
            override fun areContentsTheSame(a: NotificationItem, b: NotificationItem) = a == b
        }
    }

    inner class ViewHolder(private val binding: ItemNotificationBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: NotificationItem) {
            binding.tvMessage.text = item.message
            binding.tvTime.text = item.timeAgo
            
            // Highlight unread
            binding.root.alpha = if (item.isRead) 0.6f else 1.0f
            
            val iconRes = when (item.type) {
                "EVALUATION_ASSIGNED" -> R.drawable.bg_dot_cyan
                "DEADLINE_EXTENDED" -> R.drawable.bg_dot_success
                "ZERO_SUBMISSION" -> R.drawable.bg_dot_orange
                else -> R.drawable.bg_dot_cyan
            }
            binding.vNotifIcon.setBackgroundResource(iconRes)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
