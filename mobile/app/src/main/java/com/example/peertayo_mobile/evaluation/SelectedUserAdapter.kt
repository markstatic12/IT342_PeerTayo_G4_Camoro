package com.example.peertayo_mobile.evaluation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.data.model.UserResponse

class SelectedUserAdapter(
    private val onRemove: (Long) -> Unit
) : RecyclerView.Adapter<SelectedUserAdapter.ViewHolder>() {

    private var users: List<UserResponse> = emptyList()

    fun submitList(newList: List<UserResponse>) {
        users = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_selected_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = users[position]
        holder.bind(user)
    }

    override fun getItemCount() = users.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val name: TextView = view.findViewById(R.id.tvUserName)
        private val initial: TextView = view.findViewById(R.id.tvInitial)
        private val remove: ImageView = view.findViewById(R.id.btnRemove)

        fun bind(user: UserResponse) {
            val fullName = "${user.firstName} ${user.lastName}"
            name.text = fullName
            initial.text = user.firstName.take(1).uppercase()
            remove.setOnClickListener { onRemove(user.id) }
        }
    }
}
