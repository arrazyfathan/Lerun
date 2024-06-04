package com.androiddevs.lerun.presentation.all_run

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.androiddevs.lerun.R
import com.androiddevs.lerun.adapters.RunAdapter
import com.androiddevs.lerun.databinding.FragmentAllRunBinding
import com.androiddevs.lerun.presentation.home.MainViewModel
import com.androiddevs.lerun.utils.viewBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllRunFragment : Fragment(R.layout.fragment_all_run) {

    private val binding by viewBinding(FragmentAllRunBinding::bind)
    private val runAdapter by lazy { RunAdapter() }
    private val viewModel: MainViewModel by viewModels()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observe()
    }

    private fun observe() {
        viewModel.runs.observe(viewLifecycleOwner) { list ->
            runAdapter.submitList(list)
        }
    }

    private fun setupRecyclerView() =
        binding.rvAllRuns.apply {
            adapter = runAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
}
