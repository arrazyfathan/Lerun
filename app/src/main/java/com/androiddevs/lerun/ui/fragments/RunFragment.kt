package com.androiddevs.lerun.ui.fragments

import android.Manifest
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.androiddevs.lerun.R
import com.androiddevs.lerun.adapters.LatestRunAdapter
import com.androiddevs.lerun.databinding.FragmentRunBinding
import com.androiddevs.lerun.ui.viewmodels.MainViewModel
import com.androiddevs.lerun.ui.viewmodels.StatisticViewModel
import com.androiddevs.lerun.utils.Constants.KEY_NAME
import com.androiddevs.lerun.utils.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.androiddevs.lerun.utils.SortType
import com.androiddevs.lerun.utils.TrackingUtility
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import dagger.hilt.android.AndroidEntryPoint
import eightbitlab.com.blurview.RenderScriptBlur
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class RunFragment : Fragment(), EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentRunBinding? = null
    private val binding get() = _binding!!
    private val viewModel: MainViewModel by viewModels()
    private val statsModel: StatisticViewModel by viewModels()
    private lateinit var latestRunAdapter: LatestRunAdapter

    @Inject
    lateinit var sharedPref: SharedPreferences

    companion object {
        const val REMOTE_CONFIG_KEY_BANNER = "title_banner"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentRunBinding.inflate(inflater, container, false)
        changeBannerTitle()
        return binding.root
    }

    private fun changeBannerTitle() {
        val titleFromRemoteConfig = Firebase.remoteConfig.getString(REMOTE_CONFIG_KEY_BANNER)
        binding.lets.text = titleFromRemoteConfig
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                // topMargin = insets.top
            }

            WindowInsetsCompat.CONSUMED
        }

        setupBlured()
        getToday()
        loadName()
        requestPermissions()
        setupRecyclerView()

        when (viewModel.sortType) {
            SortType.DATE -> binding.spFilter.setSelection(0)
            SortType.RUNNING_TIME -> binding.spFilter.setSelection(1)
            SortType.DISTANCE -> binding.spFilter.setSelection(2)
            SortType.AVG_SPEED -> binding.spFilter.setSelection(3)
            SortType.CALORIES_BURNED -> binding.spFilter.setSelection(4)
        }

        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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
            val bundle = Bundle().apply {
                putSerializable("latestrun", it)
            }
            findNavController().navigate(
                R.id.action_runFragment_to_detailRunFragment,
                bundle,
            )
        }
    }

    @Suppress("DEPRECATION")
    private fun setupBlured() {
        val radius = 25f
        val renderScrip = RenderScriptBlur(requireContext())
        binding.headerProfile.setupWith(binding.containerView, renderScrip)
            .setBlurRadius(radius)
    }

    private fun loadName() {
        val name = sharedPref.getString(KEY_NAME, "")
        binding.tvName.text = name
    }

    private fun subscribeObservers() {
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
