package com.example.peertayo_mobile.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.data.model.CreatedEvaluation
import com.example.peertayo_mobile.databinding.ItemFormCardBinding

class FormsAdapter : ListAdapter<CreatedEvaluation, FormsAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CreatedEvaluation>() {
            override fun areItemsTheSame(a: CreatedEvaluation, b: CreatedEvaluation) = a.id == b.id
            override fun areContentsTheSame(a: CreatedEvaluation, b: CreatedEvaluation) = a == b
        }
    }

    inner class ViewHolder(private val binding: ItemFormCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CreatedEvaluation) {
            binding.tvTitle.text = item.title

            // Status badge
            val status = item.status?.uppercase() ?: "ACTIVE"
            binding.tvStatus.text = status
            val statusColor = when (status) {
                "ACTIVE" -> R.color.green_success
                "CLOSED" -> R.color.text_muted
                else -> R.color.orange_accent
            }
            binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, statusColor))

            // Submissions progress
            val submitted = item.submissionCount ?: 0
            val total = item.totalExpectedSubmissions ?: 1
            binding.tvSubmissions.text = "$submitted / $total submissions"
            binding.progressBar.progress = if (total > 0) (submitted * 100 / total) else 0

            // Deadline
            binding.tvDeadline.text = item.deadline ?: ""
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemFormCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
