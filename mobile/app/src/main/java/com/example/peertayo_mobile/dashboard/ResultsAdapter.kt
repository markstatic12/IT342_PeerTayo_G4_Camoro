package com.example.peertayo_mobile.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.peertayo_mobile.data.model.EvaluationResultSummary
import com.example.peertayo_mobile.databinding.ItemResultCardBinding

class ResultsAdapter(
    private val onClick: (EvaluationResultSummary) -> Unit
) : ListAdapter<EvaluationResultSummary, ResultsAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<EvaluationResultSummary>() {
            override fun areItemsTheSame(a: EvaluationResultSummary, b: EvaluationResultSummary) =
                a.evaluationId == b.evaluationId
            override fun areContentsTheSame(a: EvaluationResultSummary, b: EvaluationResultSummary) =
                a == b
        }
    }

    inner class ViewHolder(private val binding: ItemResultCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: EvaluationResultSummary) {
            binding.tvTitle.text = item.title
            binding.tvScore.text = String.format("%.1f", item.overallAverage ?: 0.0)
            binding.tvResponses.text = "${item.totalResponses ?: 0} responses"
            binding.root.setOnClickListener { onClick(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemResultCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
