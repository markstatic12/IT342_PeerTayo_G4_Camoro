package com.example.peertayo_mobile.evaluation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.databinding.FragmentCreateStepCriteriaBinding

class EvaluationCriteriaFragment : Fragment() {

    private var _binding: FragmentCreateStepCriteriaBinding? = null
    private val binding get() = _binding!!

    private val staticCriteria = listOf(
        Pair("Quality of Work", "Consistently produces accurate, thorough, and high-quality results."),
        Pair("Communication", "Clearly conveys ideas and information to team members and stakeholders."),
        Pair("Collaboration", "Works effectively with others to achieve shared goals and outcomes."),
        Pair("Reliability", "Consistently meets deadlines and fulfills commitments reliably."),
        Pair("Technical Knowledge", "Demonstrates strong understanding and application of required skills."),
        Pair("Problem Solving", "Identifies issues promptly and develops effective solutions."),
        Pair("Initiative", "Proactively seeks opportunities to contribute and improve processes."),
        Pair("Adaptability", "Adjusts effectively to changing priorities and new challenges."),
        Pair("Time Management", "Prioritizes tasks efficiently to maximize productivity."),
        Pair("Professionalism", "Maintains a positive attitude and ethical standards in all interactions.")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentCreateStepCriteriaBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.rvCriteria.layoutManager = LinearLayoutManager(requireContext())
        binding.rvCriteria.adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_criterion_overview, parent, false)
                return object : RecyclerView.ViewHolder(view) {}
            }
            override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
                val item = staticCriteria[position]
                holder.itemView.findViewById<TextView>(R.id.tvCriterionName).text = item.first
                holder.itemView.findViewById<TextView>(R.id.tvCriterionDesc).text = item.second
            }
            override fun getItemCount() = staticCriteria.size
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
