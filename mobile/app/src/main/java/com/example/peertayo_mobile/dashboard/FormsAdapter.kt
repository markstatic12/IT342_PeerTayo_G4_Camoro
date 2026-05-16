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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * FormsAdapter — GAP-04 / GAP-11 fix.
 *
 * - Status badge now shows "NEEDS ATTENTION" (orange) for overdue ACTIVE forms
 * - submissionProgress formatted as "N/M" from submissionCount + totalExpectedSubmissions (GAP-11)
 * - Deadline formatted from ISO string to human-readable "MMM d, yyyy"
 */
class FormsAdapter(
    private val onEdit: (CreatedEvaluation) -> Unit,
    private val onArchive: (CreatedEvaluation) -> Unit,
    private val onDelete: (CreatedEvaluation) -> Unit,
    private val onViewResults: (CreatedEvaluation) -> Unit
) : ListAdapter<CreatedEvaluation, FormsAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CreatedEvaluation>() {
            override fun areItemsTheSame(a: CreatedEvaluation, b: CreatedEvaluation) = a.id == b.id
            override fun areContentsTheSame(a: CreatedEvaluation, b: CreatedEvaluation) = a == b
        }

        private val isoFmt     = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        private val displayFmt = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())

        fun formatDeadline(iso: String?): String {
            if (iso.isNullOrBlank()) return "No deadline"
            return try { displayFmt.format(isoFmt.parse(iso)!!) } catch (_: Exception) { iso }
        }

        fun isOverdue(iso: String?): Boolean {
            if (iso.isNullOrBlank()) return false
            return try {
                val d = isoFmt.parse(iso) ?: return false
                d.before(Date())
            } catch (_: Exception) { false }
        }
    }

    inner class ViewHolder(private val binding: ItemFormCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CreatedEvaluation) {
            binding.tvTitle.text = item.title

            val statusRaw = item.status?.uppercase() ?: "ACTIVE"
            val overdue = isOverdue(item.deadline) && statusRaw == "ACTIVE"

            // Status badge — web: Active / Needs Attention / Closed
            val (badgeLabel, badgeColor) = when {
                overdue             -> "NEEDS ATTENTION" to R.color.orange_accent
                statusRaw == "ACTIVE" -> "ACTIVE"       to R.color.green_success
                statusRaw == "CLOSED" -> "CLOSED"       to R.color.text_muted
                else                -> statusRaw         to R.color.cyan_primary
            }
            binding.tvStatus.text = badgeLabel
            binding.tvStatus.setTextColor(ContextCompat.getColor(binding.root.context, badgeColor))

            // Submission progress — GAP-11: formatted as "N/M submissions"
            val submitted = item.submissionCount ?: 0
            val total     = item.totalExpectedSubmissions ?: 0
            binding.tvSubmissions.text = if (total > 0) "$submitted/$total submissions" else "No submissions yet"
            binding.progressBar.max      = if (total > 0) total else 1
            binding.progressBar.progress = submitted

            // Deadline — formatted ISO → "May 20, 2026"
            binding.tvDeadline.text = formatDeadline(item.deadline)

            // Context Menu (GAP-04)
            binding.root.setOnClickListener { onViewResults(item) }
            binding.btnMenu.setOnClickListener { view ->
                val popup = android.widget.PopupMenu(view.context, view)
                popup.menu.add("View Results")
                popup.menu.add("Edit").apply { isEnabled = (statusRaw != "CLOSED") }
                popup.menu.add("Archive")
                popup.menu.add("Delete")
                
                popup.setOnMenuItemClickListener { menuItem ->
                    when (menuItem.title) {
                        "View Results" -> { onViewResults(item); true }
                        "Edit" -> { onEdit(item); true }
                        "Archive" -> { onArchive(item); true }
                        "Delete" -> { onDelete(item); true }
                        else -> false
                    }
                }
                popup.show()
            }
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
