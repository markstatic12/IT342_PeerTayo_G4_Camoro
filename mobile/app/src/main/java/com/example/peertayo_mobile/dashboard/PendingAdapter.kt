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

        private val deadlineFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        private val displayFmt  = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        fun formatDeadline(iso: String?): String {
            if (iso.isNullOrBlank()) return "No deadline"
            return try { displayFmt.format(deadlineFmt.parse(iso)!!) } catch (_: Exception) { iso }
        }
    }

    inner class ViewHolder(private val binding: ItemPendingEvalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(form: PendingForm) {
            binding.tvTitle.text = form.title

            val isUrgent = form.isUrgent()
            val isMissed = form.isMissed()
            val daysLeft = form.daysLeft()

            // Status badge — mirrors web: "Urgent" in orange, "Pending" in blue
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
                if (isUrgent && daysLeft != null && daysLeft >= 0) {
                    append(" · ${daysLeft}d left")
                }
            }
            binding.tvDeadline.text = deadlineText

            // Evaluatee count meta
            val count = form.evaluatees.size
            binding.tvEvaluateeCount.text = "$count evaluatee${if (count != 1) "s" else ""}"

            // Progress bar (no evaluatees done in pending list — always 0/N)
            binding.progressBar.max = 100
            binding.progressBar.progress = 0
            binding.tvProgress.text = "0/$count"

            // Build evaluatees sub-list (matches web's detail panel evaluatee rows)
            binding.evaluateesContainer.removeAllViews()
            form.evaluatees.forEach { evaluatee ->
                val row = LayoutInflater.from(binding.root.context)
                    .inflate(R.layout.item_evaluatee_row, binding.evaluateesContainer, false)

                row.findViewById<TextView>(R.id.tvEvaluateeName).text = evaluatee.name

                // Avatar initials
                val initials = evaluatee.name.trim().split(" ")
                    .filter { it.isNotEmpty() }.take(2)
                    .joinToString("") { it[0].uppercase() }
                row.findViewById<TextView>(R.id.tvInitials).text = initials

                row.findViewById<View>(R.id.btnEvaluate).setOnClickListener {
                    onEvaluateClick(form, evaluatee)
                }

                binding.evaluateesContainer.addView(row)
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
