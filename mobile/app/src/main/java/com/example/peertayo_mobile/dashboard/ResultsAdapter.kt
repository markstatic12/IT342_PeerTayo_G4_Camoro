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
            binding.tvFacilitator.text = "By ${item.createdByName ?: "—"}"
            binding.tvScore.text = String.format("%.1f", item.overallAverage ?: 0.0)
            val responses = item.totalResponses ?: 0
            binding.tvResponses.text = "Based on $responses response${if (responses != 1) "s" else ""}"
            binding.root.setOnClickListener { onClick(item) }

            // Context Menu (GAP-01)
            binding.btnMenu.setOnClickListener { view ->
                val popup = android.widget.PopupMenu(view.context, view)
                popup.menu.add("Archive")
                popup.menu.add("Delete")
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.title) {
                        "Archive" -> { /* TODO: API Archive */ true }
                        "Delete" -> { /* TODO: API Delete */ true }
                        else -> false
                    }
                }
                popup.show()
            }
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
