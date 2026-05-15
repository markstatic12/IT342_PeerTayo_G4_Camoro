package com.example.peertayo_mobile.evaluation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.data.model.UserResponse

class SearchAdapter(
    private val onSelectionChanged: (UserResponse, Boolean) -> Unit
) : RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    private var users: List<UserResponse> = emptyList()

    fun submitList(newList: List<UserResponse>) {
        users = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_search_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(users[position])
    }

    override fun getItemCount() = users.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.tvUserName)
        private val email: TextView = view.findViewById(R.id.tvEmail)
        private val checkbox: CheckBox = view.findViewById(R.id.checkbox)

        fun bind(user: UserResponse) {
            name.text = "${user.firstName} ${user.lastName}"
            email.text = user.email
            checkbox.setOnCheckedChangeListener(null)
            checkbox.isChecked = false // Reset state for reuse
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                onSelectionChanged(user, isChecked)
            }
            itemView.setOnClickListener { checkbox.toggle() }
        }
    }
}
