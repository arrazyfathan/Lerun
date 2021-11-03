package com.androiddevs.lerun.ui.fragments

import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.androiddevs.lerun.R
import com.androiddevs.lerun.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: MainViewModel by viewModels()
}