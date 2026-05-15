package com.example.peertayo_mobile.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.peertayo_mobile.R
import com.example.peertayo_mobile.databinding.FragmentResultDetailBinding

/**
 * ResultDetailFragment — Full detail view for a single evaluation result.
 *
 * GAP-08 FIX: Replaced ProgressBar with 5 filled/empty dot indicators per criterion,
 * matching the web MyResultsPage right-panel visualization exactly.
 * Dots are drawn programmatically: filled dots for score ≤ N, empty for remainder.
 *
 * Score label follows web convention: 1=Poor, 2=Fair, 3=Good, 4=Very Good, 5=Excellent.
 */
class ResultDetailFragment : Fragment() {

    private var _binding: FragmentResultDetailBinding? = null
    private val binding get() = _binding!!

    companion object {
        private val SCORE_LABELS = mapOf(
            1 to "Poor",
            2 to "Fair",
            3 to "Good",
            4 to "Very Good",
            5 to "Excellent"
        )

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
        binding.tvResponseCount.text = "$responses peer response${if (responses != 1) "s" else ""}"

        // Back
        binding.btnBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        // Criteria breakdown — dot indicators (GAP-08 fix)
        val inflater = LayoutInflater.from(requireContext())
        criteriaNames.forEachIndexed { index, name ->
            val score = criteriaScores.getOrElse(index) { 0f }
            val roundedScore = score.toInt().coerceIn(0, 5)
            val row = inflater.inflate(R.layout.item_criteria_dot, binding.criteriaContainer, false)

            row.findViewById<TextView>(R.id.tvCriterionName).text = name
            row.findViewById<TextView>(R.id.tvCriterionScore).text = String.format("%.1f", score)
            row.findViewById<TextView>(R.id.tvScoreLabel).text = SCORE_LABELS[roundedScore] ?: ""

            // Build 5 dot views programmatically
            val dotsContainer = row.findViewById<LinearLayout>(R.id.dotsContainer)
            buildDots(dotsContainer, roundedScore)

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

    /**
     * Renders 5 dots into [container].
     * Positions 1..filledCount get bg_dot_filled; filledCount+1..5 get bg_dot_empty.
     * Each dot is 12dp × 12dp with 4dp right margin.
     */
    private fun buildDots(container: LinearLayout, filledCount: Int) {
        val dp = resources.displayMetrics.density
        val dotSize = (12 * dp).toInt()
        val dotMargin = (4 * dp).toInt()

        for (i in 1..5) {
            val dot = View(requireContext())
            dot.setBackgroundResource(
                if (i <= filledCount) R.drawable.bg_dot_filled else R.drawable.bg_dot_empty
            )
            val params = LinearLayout.LayoutParams(dotSize, dotSize).also {
                it.marginEnd = dotMargin
            }
            dot.layoutParams = params
            container.addView(dot)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
