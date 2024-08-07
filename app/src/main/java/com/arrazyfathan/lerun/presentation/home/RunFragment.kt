package com.arrazyfathan.lerun.presentation.home

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.arrazyfathan.lerun.R
import com.arrazyfathan.lerun.databinding.FragmentRunBinding
import com.arrazyfathan.lerun.presentation.adapters.LatestRunAdapter
import com.arrazyfathan.lerun.presentation.statistic.StatisticViewModel
import com.arrazyfathan.lerun.ui.customview.CustomMarkerView
import com.arrazyfathan.lerun.utils.BitmapConverter
import com.arrazyfathan.lerun.utils.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.arrazyfathan.lerun.utils.LabelChartFormatter
import com.arrazyfathan.lerun.utils.SortType
import com.arrazyfathan.lerun.utils.TrackingUtility
import com.arrazyfathan.lerun.utils.gone
import com.arrazyfathan.lerun.utils.viewBinding
import com.arrazyfathan.lerun.utils.visible
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.RenderScriptBlur
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.Calendar
import kotlin.math.round

@AndroidEntryPoint
class RunFragment : Fragment(R.layout.fragment_run), EasyPermissions.PermissionCallbacks {
    private val binding by viewBinding(FragmentRunBinding::bind)
    private val viewModel: MainViewModel by viewModels()
    private val statsModel: StatisticViewModel by viewModels()
    private lateinit var latestRunAdapter: LatestRunAdapter

    companion object {
        const val REMOTE_CONFIG_KEY_BANNER = "title_banner"
    }

    private fun changeBannerTitle() {
        val titleFromRemoteConfig = Firebase.remoteConfig.getString(REMOTE_CONFIG_KEY_BANNER)
        binding.lets.text = titleFromRemoteConfig
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        changeBannerTitle()
        setBlurred()
        getToday()
        loadName()
        requestPermissions()
        setupRecyclerView()
        setupView()

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

        subscribeObservers()

        binding.btnSeeMore.setOnClickListener {
            findNavController().navigate(
                R.id.action_runFragment_to_allRunFragment,
            )
        }

        viewModel.runs.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
                binding.tvLabelLatest.visibility = View.GONE
                binding.rvRuns.visibility = View.GONE
                binding.btnSeeMore.visibility = View.INVISIBLE
            } else {
                binding.tvLabelLatest.visibility = View.VISIBLE
                binding.rvRuns.visibility = View.VISIBLE
                binding.btnSeeMore.visibility = View.VISIBLE
                latestRunAdapter.submitLatestList(list)
            }
        }

        latestRunAdapter.setOnClickListener {
            val bundle =
                Bundle().apply {
                    putSerializable("latestrun", it)
                }
            findNavController().navigate(
                R.id.action_runFragment_to_detailRunFragment,
                bundle,
            )
        }
    }

    private fun setupView() {
        binding.chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawLabels(false)
            axisLineColor = ContextCompat.getColor(requireContext(), R.color.dark_accent_text)
            textColor = ContextCompat.getColor(requireContext(), R.color.dark_accent_text)
            setDrawGridLines(false)
        }

        binding.chart.axisLeft.apply {
            axisLineColor = ContextCompat.getColor(requireContext(), R.color.dark_accent_text)
            textColor = ContextCompat.getColor(requireContext(), R.color.dark_accent_text)
            setDrawGridLines(false)
        }

        binding.chart.axisRight.apply {
            axisLineColor = ContextCompat.getColor(requireContext(), R.color.dark_accent_text)
            textColor = ContextCompat.getColor(requireContext(), R.color.dark_accent_text)
            setDrawGridLines(false)
        }

        binding.chart.apply {
            description.text = "Avg Speed Over Time"
            description.textColor = Color.WHITE
            legend.isEnabled = false
        }
    }

    private fun setBlurred() {
        val radius = 25f
        val renderScrip = RenderScriptBlur(requireContext())
        binding.headerProfile.setupWith(binding.containerView, renderScrip)
            .setBlurRadius(radius)
    }

    private fun loadName() {
        val name = viewModel.getProfileName()
        binding.tvName.text = name
    }

    private fun subscribeObservers() {
        viewModel.userImage().observe(viewLifecycleOwner) { userImage ->
            if (userImage != null) {
                if (!userImage.imageString.isNullOrBlank())
                    binding.tvProfile.setImageBitmap(
                        BitmapConverter.converterStringToBitmap(
                            userImage.imageString!!
                        )
                    )
                else
                    binding.tvProfile.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireContext(),
                            R.drawable.profile_n
                        )
                    )
            } else {
                binding.tvProfile.setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.profile_n
                    )
                )
            }
        }


        statsModel.totalCaloriesBurned.observe(
            viewLifecycleOwner,
        ) { calories ->
            calories?.let {
                val totalCalories = "$calories kcal"
                binding.totalCalories.text = totalCalories
            }
        }

        statsModel.totalAverageSpeed.observe(
            viewLifecycleOwner,
        ) { avgSpeed ->
            avgSpeed?.let {
                val totalAverageSpeed = "${round(avgSpeed * 10f) / 10} km/h"
                binding.averageSpeed.text = totalAverageSpeed
            }
        }

        statsModel.totalDistance.observe(
            viewLifecycleOwner,
        ) { distance ->
            distance?.let {
                val km = it / 1000f
                val totalDistance = round(km * 10f) / 10
                val totalDistanceString = "$totalDistance km"

                binding.totalDistance.text = totalDistanceString
            }
        }

        statsModel.totalTimeRun.observe(
            viewLifecycleOwner,
        ) { duration ->
            duration?.let {
                val totalTimeRun = TrackingUtility.formatDuration(duration)
                binding.totalTimeRun.text = totalTimeRun
            }
        }

        viewModel.runsSortedByDateAsc.observe(viewLifecycleOwner) { runs ->
            if (runs.isNullOrEmpty()) binding.chart.gone() else binding.chart.visible()
            runs?.let { data ->
                val allAvgSpeed = data.indices.map { i -> BarEntry(i.toFloat(), runs[i].avgSpeedInKMH) }
                val barDataSet = LineDataSet(allAvgSpeed, "Average Speed Overtime").apply {
                    valueTextColor = Color.WHITE
                    valueTextSize = 8f
                    lineWidth = 2f
                    valueFormatter = LabelChartFormatter()
                    color = ContextCompat.getColor(requireContext(), R.color.ijo)
                    mode = LineDataSet.Mode.CUBIC_BEZIER
                    setCircleColor(ContextCompat.getColor(requireContext(), R.color.ijo))
                    setDrawCircleHole(false)
                    setDrawFilled(true)
                    fillDrawable = ContextCompat.getDrawable(requireContext(), R.drawable.gradient_green)
                }
                binding.chart.data = LineData(barDataSet)
                binding.chart.marker = CustomMarkerView(runs, requireContext(), R.layout.marker_view)
                binding.chart.invalidate()
            }
        }
    }

    private fun getToday() {
        val calendar = Calendar.getInstance()
        when (calendar.get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> {
                binding.tvDateToday.text = getString(R.string.good_morning)
            }

            in 12..15 -> {
                binding.tvDateToday.text = getString(R.string.good_afternoon)
            }

            in 16..20 -> {
                binding.tvDateToday.text = getString(R.string.good_evening)
            }

            in 21..23 -> {
                binding.tvDateToday.text = getString(R.string.good_night)
            }
        }
    }

    private fun setupRecyclerView() =
        binding.rvRuns.apply {
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
                Manifest.permission.ACCESS_FINE_LOCATION,
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to accept location permissions to use this apps",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            )
        }
    }

    override fun onPermissionsGranted(
        requestCode: Int,
        perms: MutableList<String>,
    ) {}

    override fun onPermissionsDenied(
        requestCode: Int,
        perms: MutableList<String>,
    ) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onResume() {
        super.onResume()
        changeBannerTitle()
    }
}
