package com.androiddevs.lerun.ui.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.androiddevs.lerun.R
import com.androiddevs.lerun.databinding.FragmentStatisticsBinding
import com.androiddevs.lerun.ui.viewmodels.StatisticViewModel
import com.androiddevs.lerun.utils.TrackingUtility
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.round

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticViewModel by viewModels()

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeObservers()
        setupBarChart()
    }

    private fun setupBarChart() {
        binding.barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        binding.barChart.axisLeft.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        binding.barChart.axisRight.apply {
            axisLineColor = Color.WHITE
            textColor = Color.WHITE
            setDrawGridLines(false)
        }

        binding.barChart.apply {
            description.text = "Average Speed Over Time"
            legend.isEnabled = false
        }
    }

    private fun subscribeObservers() {
        viewModel.totalTimeRun.observe(
            viewLifecycleOwner,
            Observer {
                it?.let {
                    val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                    binding.tvTotalTime.text = totalTimeRun
                }
            },
        )

        viewModel.totalDistance.observe(
            viewLifecycleOwner,
            Observer {
                it?.let {
                    val km = it / 1000f
                    val totalDistance = round(km * 10f) / 10
                    val totalDistanceString = "${totalDistance}km"

                    binding.tvTotalDistance.text = totalDistanceString
                }
            },
        )

        viewModel.totalAverageSpeed.observe(
            viewLifecycleOwner,
            Observer {
                it?.let {
                    val averageSpeed = round(it * 10f) / 10
                    val averageSpeedString = "${averageSpeed}km/h"
                    binding.tvAverageSpeed.text = averageSpeedString
                }
            },
        )

        viewModel.totalCaloriesBurned.observe(
            viewLifecycleOwner,
            Observer {
                it?.let {
                    val totalCalories = "${it}kcal"
                    binding.tvTotalCalories.text = totalCalories
                }
            },
        )

        viewModel.runsSortedByDate.observe(
            viewLifecycleOwner,
            Observer {
                it?.let {
                    val allAverageSpeed = it.indices.map { i ->
                        BarEntry(i.toFloat(), it[i].avgSpeedInKMH)
                    }

                    val barDataSet = BarDataSet(allAverageSpeed, "Average Speed Over Time").apply {
                        valueTextColor = Color.WHITE
                        color = ContextCompat.getColor(requireContext(), R.color.colorAccent)
                    }

                    binding.barChart.data = BarData(barDataSet)
                    // binding.barChart.marker = CustomMarkerView(it.reversed(), requireContext(), R.layout.marker_view)
                    binding.barChart.invalidate()
                }
            },
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}
