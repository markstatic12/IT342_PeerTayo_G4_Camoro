package com.example.peertayo_mobile.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.databinding.ItemPendingEvalBinding
import java.text.SimpleDateFormat
import java.util.Locale

class PendingAdapter(
    /** Called when user taps "Evaluate →" for a specific evaluatee inside a form card. */
    private val onEvaluateClick: (form: PendingForm, evaluatee: PendingEvaluatee) -> Unit
) : ListAdapter<PendingForm, PendingAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<PendingForm>() {
            override fun areItemsTheSame(a: PendingForm, b: PendingForm) = a.id == b.id
            override fun areContentsTheSame(a: PendingForm, b: PendingForm) = a == b
        }

        private val deadlineFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault())
        private val displayFmt  = SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault())

        fun formatDeadline(iso: String?): String {
            if (iso.isNullOrBlank()) return "No deadline"
            // Remove seconds/milliseconds if present to match the pattern
            val cleanIso = if (iso.contains(".")) iso.split(".")[0] else iso
            val truncatedIso = if (cleanIso.length > 16) cleanIso.substring(0, 16) else cleanIso
            return try { displayFmt.format(deadlineFmt.parse(truncatedIso)!!) } catch (_: Exception) { iso }
        }
    }

    inner class ViewHolder(private val binding: ItemPendingEvalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(form: PendingForm) {
            binding.tvTitle.text = form.title
            
            // Show Facilitator (BR-005 parity)
            binding.tvFacilitator.text = if (!form.creatorName.isNullOrBlank()) {
                "By ${form.creatorName}"
            } else {
                "By Facilitator"
            }

            val isUrgent = form.isUrgent()
            val isMissed = form.isMissed()
            val daysLeft = form.daysLeft()

            // Status badge
            when {
                isMissed -> {
                    binding.tvUrgencyBadge.visibility = View.VISIBLE
                    binding.tvUrgencyBadge.text = "MISSED"
                    binding.tvUrgencyBadge.setBackgroundResource(R.drawable.bg_badge_red)
                }
                isUrgent -> {
                    binding.tvUrgencyBadge.visibility = View.VISIBLE
                    binding.tvUrgencyBadge.text = "URGENT"
                    binding.tvUrgencyBadge.setBackgroundResource(R.drawable.bg_badge)
                }
                else -> {
                    binding.tvUrgencyBadge.visibility = View.GONE
                }
            }

            // Deadline text
            val deadlineText = buildString {
                append("Due ")
                append(formatDeadline(form.deadline))
                if (!isMissed && isUrgent && daysLeft != null && daysLeft >= 0) {
                    append(" · ${daysLeft}d left")
                }
            }
            binding.tvDeadline.text = deadlineText

            // Evaluatee count meta
            val count = form.evaluatees.size
            binding.tvEvaluateeCount.text = "$count evaluatee${if (count != 1) "s" else ""}"

            // Build evaluatees sub-list
            binding.evaluateesContainer.removeAllViews()
            form.evaluatees.forEach { evaluatee ->
                val row = LayoutInflater.from(binding.root.context)
                    .inflate(R.layout.item_evaluatee_row, binding.evaluateesContainer, false)

                val tvName = row.findViewById<TextView>(R.id.tvEvaluateeName)
                val btnEval = row.findViewById<TextView>(R.id.btnEvaluate)
                
                tvName.text = evaluatee.name

                // Locking Logic (BR-004 parity)
                if (isMissed) {
                    btnEval.text = "LOCKED"
                    btnEval.isEnabled = false
                    btnEval.alpha = 0.5f
                    btnEval.setTextColor(ContextCompat.getColor(binding.root.context, R.color.text_muted))
                } else {
                    btnEval.text = "EVALUATE"
                    btnEval.isEnabled = true
                    btnEval.alpha = 1.0f
                    btnEval.setOnClickListener {
                        onEvaluateClick(form, evaluatee)
                    }
                }

                // Avatar initials
                val initials = evaluatee.name.trim().split(" ")
                    .filter { it.isNotEmpty() }.take(2)
                    .joinToString("") { it[0].uppercase() }
                row.findViewById<TextView>(R.id.tvInitials).text = initials

                binding.evaluateesContainer.addView(row)
            }

            // Expand/collapse logic
            binding.root.setOnClickListener {
                val isVisible = binding.detailPanel.visibility == View.VISIBLE
                binding.detailPanel.visibility = if (isVisible) View.GONE else View.VISIBLE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPendingEvalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
