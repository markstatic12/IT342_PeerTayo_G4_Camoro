package com.example.peertayo_mobile.dashboard

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.peertayo_mobile.data.model.CompletedForm
import com.example.peertayo_mobile.databinding.ItemCompletedCardBinding

class CompletedAdapter : ListAdapter<CompletedForm, CompletedAdapter.ViewHolder>(DIFF) {

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CompletedForm>() {
            override fun areItemsTheSame(a: CompletedForm, b: CompletedForm) = a.id == b.id
            override fun areContentsTheSame(a: CompletedForm, b: CompletedForm) = a == b
        }
    }

    inner class ViewHolder(private val binding: ItemCompletedCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CompletedForm) {
            binding.tvTitle.text = item.title
            binding.tvEvaluatee.text = item.evaluateeName
            binding.tvDate.text = item.submittedAt ?: ""
            binding.tvInitial.text = item.evaluateeName.firstOrNull()?.uppercase() ?: "?"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCompletedCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
