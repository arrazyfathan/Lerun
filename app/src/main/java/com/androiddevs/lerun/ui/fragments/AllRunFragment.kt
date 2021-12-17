package com.androiddevs.lerun.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.androiddevs.lerun.adapters.LatestRunAdapter
import com.androiddevs.lerun.adapters.RunAdapter
import com.androiddevs.lerun.databinding.FragmentAllRunBinding
import com.androiddevs.lerun.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AllRunFragment : Fragment() {

    private var _binding: FragmentAllRunBinding? = null
    private val binding get() = _binding!!

    private lateinit var runAdapter: RunAdapter

    // main view model
    private val viewModel: MainViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAllRunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()

        viewModel.runs.observe(viewLifecycleOwner) { list ->
            runAdapter.submitList(list)
        }
    }

    private fun setupRecyclerView() = binding.rvAllRuns.apply {
        runAdapter = RunAdapter()
        adapter = runAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }


}