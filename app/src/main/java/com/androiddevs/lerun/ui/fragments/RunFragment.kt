package com.androiddevs.lerun.ui.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.androiddevs.lerun.R
import com.androiddevs.lerun.adapters.LatestRunAdapter
import com.androiddevs.lerun.adapters.RunAdapter
import com.androiddevs.lerun.databinding.FragmentRunBinding
import com.androiddevs.lerun.ui.viewmodels.MainViewModel
import com.androiddevs.lerun.ui.viewmodels.StatisticViewModel
import com.androiddevs.lerun.utils.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.androiddevs.lerun.utils.SortType
import com.androiddevs.lerun.utils.TrackingUtility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*
import kotlinx.android.synthetic.main.new_item_run.view.*
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

@AndroidEntryPoint
class RunFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentRunBinding? = null
    private val binding get() = _binding!!

    // main view model
    private val viewModel: MainViewModel by viewModels()

    //stats model
    private val statsModel: StatisticViewModel by viewModels()

    private lateinit var latestRunAdapter: LatestRunAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRunBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getToday()
        requestPermissions()
        setupRecyclerView()

        when(viewModel.sortType) {
            SortType.DATE -> spFilter.setSelection(0)
            SortType.RUNNING_TIME -> spFilter.setSelection(1)
            SortType.DISTANCE -> spFilter.setSelection(2)
            SortType.AVG_SPEED -> spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> spFilter.setSelection(4)
        }

        spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {}

            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                when(pos) {
                    0 -> viewModel.sortRuns(SortType.DATE)
                    1 -> viewModel.sortRuns(SortType.RUNNING_TIME)
                    2 -> viewModel.sortRuns(SortType.DISTANCE)
                    3 -> viewModel.sortRuns(SortType.AVG_SPEED)
                    4 -> viewModel.sortRuns(SortType.CALORIES_BURNED)
                }
            }
        }


        subscribeObservers()

        viewModel.runs.observe(viewLifecycleOwner, Observer {
            latestRunAdapter.submitLatestList(it)
        })

    }

    private fun subscribeObservers() {
        statsModel.totalCaloriesBurned.observe(viewLifecycleOwner, Observer { calories ->
            calories?.let {
                val totalCalories = "$calories kcal"
                binding.totalCalories.text = totalCalories
            }

        })

        statsModel.totalAverageSpeed.observe(viewLifecycleOwner, Observer { avgSpeed ->
            avgSpeed?.let {
                val totalAverageSpeed = "${round(avgSpeed * 10f) / 10} km/h"
                binding.averageSpeed.text = totalAverageSpeed
            }
        })

        statsModel.totalDistance.observe(viewLifecycleOwner, Observer { distance ->
            distance?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10
                val totalDistanceString = "$totalDistance km"

                binding.totalDistance.text = totalDistanceString
            }
        })

        statsModel.totalTimeRun.observe(viewLifecycleOwner, Observer { duration ->
            duration?.let {
                val totalTimeRun = TrackingUtility.formatDuration(duration)
                binding.totalTimeRun.text = totalTimeRun
            }
        })
    }

    private fun getToday() {
        val calendar = Calendar.getInstance()
        when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> {
                binding.tvDateToday.text = "Good Morning"
            }
            in 12..15 -> {
                binding.tvDateToday.text = "Good Afternoon"
            }
            in 16..20 -> {
                binding.tvDateToday.text = "Good Evening"
            }
            in 21..23 -> {
                binding.tvDateToday.text = "Good Night"
            }
        }
    }

    private fun setupRecyclerView() = binding.rvRuns.apply {
        latestRunAdapter = LatestRunAdapter()
        adapter = latestRunAdapter
        layoutManager = LinearLayoutManager(requireContext())
    }

    private fun requestPermissions() {
        if (TrackingUtility.hasLocationPermissions(requireContext())) {
            return
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this apps",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this apps",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {}

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}