package com.androiddevs.lerun.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.androiddevs.lerun.R
import com.androiddevs.lerun.databinding.FragmentStatisticsBinding
import com.androiddevs.lerun.ui.viewmodels.StatisticViewModel
import com.androiddevs.lerun.utils.TrackingUtility
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
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        subscribeObservers()
    }

    private fun subscribeObservers() {
        viewModel.totalTimeRun.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalTimeRun = TrackingUtility.getFormattedStopWatchTime(it)
                binding.tvTotalTime.text = totalTimeRun
            }
        })

        viewModel.totalDistance.observe(viewLifecycleOwner, Observer {
            it?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10
                val totalDistanceString = "${totalDistance}km"

                binding.tvTotalDistance.text = totalDistanceString
            }
        })

        viewModel.totalAverageSpeed.observe(viewLifecycleOwner, Observer {
            it?.let {
                val averageSpeed = round(it * 10f) / 10
                val averageSpeedString = "${averageSpeed}km/h"
                binding.tvAverageSpeed.text = averageSpeedString
            }
        })

        viewModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer {
            it?.let {
                val totalCalories = "${it}kcal"
                binding.tvTotalCalories.text = totalCalories
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}

