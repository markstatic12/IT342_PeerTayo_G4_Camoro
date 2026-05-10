package com.example.peertayo_mobile.landing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.peertayo_mobile.R

class HowItWorksAdapter(private val steps: List<HowItWorksItem>) : 
    RecyclerView.Adapter<HowItWorksAdapter.HowItWorksViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HowItWorksViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_how_it_works, parent, false)
        return HowItWorksViewHolder(view)
    }

    override fun onBindViewHolder(holder: HowItWorksViewHolder, position: Int) {
        holder.bind(steps[position])
    }

    override fun getItemCount(): Int = steps.size

    class HowItWorksViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stepView: TextView = itemView.findViewById(R.id.tvStep)
        private val iconView: ImageView = itemView.findViewById(R.id.ivStepIcon)
        private val titleView: TextView = itemView.findViewById(R.id.tvStepTitle)
        private val descriptionView: TextView = itemView.findViewById(R.id.tvStepDescription)

        fun bind(step: HowItWorksItem) {
            stepView.text = step.step
            titleView.text = step.title
            descriptionView.text = step.description
            
            // Set appropriate icon based on iconType
            val iconRes = when (step.iconType) {
                "create" -> R.drawable.ic_create
                "assign" -> R.drawable.ic_assign
                "submit" -> R.drawable.ic_submit
                "results" -> R.drawable.ic_results
                else -> R.drawable.ic_default
            }
            iconView.setImageResource(iconRes)
        }
    }
}
