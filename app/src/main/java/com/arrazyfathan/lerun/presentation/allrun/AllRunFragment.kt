package com.arrazyfathan.lerun.presentation.allrun

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.arrazyfathan.lerun.R
import com.arrazyfathan.lerun.presentation.adapters.RunAdapter
import com.arrazyfathan.lerun.databinding.FragmentAllRunBinding
import com.arrazyfathan.lerun.presentation.home.MainViewModel
import com.arrazyfathan.lerun.utils.SortType
import com.arrazyfathan.lerun.utils.viewBinding
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
        setupView()
        setupRecyclerView()
        observe()
    }

    private fun setupView() {
        when (viewModel.sortType) {
            SortType.DATE -> binding.spFilter.setSelection(0)
            SortType.DATE_ASC -> binding.spFilter.setSelection(5)
            SortType.RUNNING_TIME -> binding.spFilter.setSelection(1)
            SortType.DISTANCE -> binding.spFilter.setSelection(2)
            SortType.AVG_SPEED -> binding.spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> binding.spFilter.setSelection(4)
        }

        binding.spFilter.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {}

                override fun onItemSelected(
                    adapterView: AdapterView<*>?,
                    view: View?,
                    pos: Int,
                    id: Long,
                ) {
                    when (pos) {
                        0 -> viewModel.sortRuns(SortType.DATE)
                        1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                        2 -> viewModel.sortRuns(SortType.DISTANCE)
                        3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                        4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                        5 -> viewModel.sortRuns(SortType.DATE_ASC)
                    }
                }
            }
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
