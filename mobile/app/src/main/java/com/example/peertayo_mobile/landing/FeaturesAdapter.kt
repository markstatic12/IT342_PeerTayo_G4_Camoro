package com.example.peertayo_mobile.landing

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.peertayo_mobile.R

class FeaturesAdapter(private val features: List<FeatureItem>) :
    RecyclerView.Adapter<FeaturesAdapter.FeatureViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_feature, parent, false)
        return FeatureViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeatureViewHolder, position: Int) {
        holder.bind(features[position])
        // Stagger entrance animation
        val anim = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.fade_up)
        anim.startOffset = (position * 60L)
        holder.itemView.startAnimation(anim)
    }

    override fun getItemCount(): Int = features.size

    class FeatureViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val iconView: ImageView = itemView.findViewById(R.id.ivFeatureIcon)
        private val titleView: TextView = itemView.findViewById(R.id.tvFeatureTitle)
        private val descriptionView: TextView = itemView.findViewById(R.id.tvFeatureDescription)

        fun bind(feature: FeatureItem) {
            titleView.text = feature.title
            descriptionView.text = feature.description

            val iconRes = when (feature.iconType) {
                "lock"   -> R.drawable.ic_lock
                "form"   -> R.drawable.ic_form
                "chart"  -> R.drawable.ic_chart
                "bell"   -> R.drawable.ic_bell
                "role"   -> R.drawable.ic_role
                "mobile" -> R.drawable.ic_mobile
                else     -> R.drawable.ic_default
            }
            iconView.setImageResource(iconRes)
            // Tint icon to web blue-light color (#60a5fa)
            iconView.imageTintList = ColorStateList.valueOf(
                ContextCompat.getColor(itemView.context, R.color.blue_light)
            )
        }
    }
}
