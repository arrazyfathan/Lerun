package com.androiddevs.lerun.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.androiddevs.lerun.R
import com.androiddevs.lerun.ui.viewmodels.StatisticViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class StatisticsFragment : Fragment(R.layout.fragment_statistics) {

    private val viewModel: StatisticViewModel by viewModels()
}

