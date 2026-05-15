package com.example.peertayo_mobile.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.databinding.FragmentResultDetailBinding

class ResultDetailFragment : Fragment() {

    private var _binding: FragmentResultDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(
            title: String,
            average: Double,
            responses: Int,
            comments: ArrayList<String>,
            criteriaNames: ArrayList<String>,
            criteriaScores: ArrayList<Float>
        ): ResultDetailFragment {
            return ResultDetailFragment().apply {
                arguments = Bundle().apply {
                    putString("title", title)
                    putDouble("average", average)
                    putInt("responses", responses)
                    putStringArrayList("comments", comments)
                    putStringArrayList("criteriaNames", criteriaNames)
                    putFloatArray("criteriaScores", criteriaScores.toFloatArray())
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResultDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val args = arguments ?: return
        val title = args.getString("title", "")
        val average = args.getDouble("average", 0.0)
        val responses = args.getInt("responses", 0)
        val comments = args.getStringArrayList("comments") ?: emptyList<String>()
        val criteriaNames = args.getStringArrayList("criteriaNames") ?: emptyList<String>()
        val criteriaScores = args.getFloatArray("criteriaScores") ?: floatArrayOf()

        // Header
        binding.tvTitle.text = title
        binding.tvOverallScore.text = String.format("%.1f", average)
        binding.tvResponseCount.text = "$responses responses"

        // Back
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Criteria breakdown bars
        val inflater = LayoutInflater.from(requireContext())
        criteriaNames.forEachIndexed { index, name ->
            val score = criteriaScores.getOrElse(index) { 0f }
            val row = inflater.inflate(R.layout.item_criteria_bar, binding.criteriaContainer, false)
            row.findViewById<TextView>(R.id.tvCriterionName).text = name
            row.findViewById<TextView>(R.id.tvCriterionScore).text = String.format("%.1f", score)
            row.findViewById<ProgressBar>(R.id.progressBar).progress = (score / 5f * 100).toInt()
            binding.criteriaContainer.addView(row)
        }

        // Comments
        if (comments.isEmpty()) {
            binding.tvNoComments.visibility = View.VISIBLE
        } else {
            binding.tvNoComments.visibility = View.GONE
            comments.forEach { comment ->
                val tv = TextView(requireContext()).apply {
                    text = "\"$comment\""
                    setTextColor(resources.getColor(R.color.text_secondary, null))
                    textSize = 13f
                    setBackgroundResource(R.drawable.bg_glass_card)
                    setPadding(32, 20, 32, 20)
                    val params = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    params.bottomMargin = 8
                    layoutParams = params
                }
                binding.commentsContainer.addView(tv)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
