package com.androiddevs.lerun.ui.fragments

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.androiddevs.lerun.R
import com.androiddevs.lerun.databinding.FragmentTrackingBinding
import com.androiddevs.lerun.db.Run
import com.androiddevs.lerun.services.Polyline
import com.androiddevs.lerun.services.TrackingService
import com.androiddevs.lerun.ui.viewmodels.MainViewModel
import com.androiddevs.lerun.utils.Constants.ACTION_PAUSE_SERVICE
import com.androiddevs.lerun.utils.Constants.ACTION_START_OR_RESUME_SERVICE
import com.androiddevs.lerun.utils.Constants.ACTION_STOP_SERVICE
import com.androiddevs.lerun.utils.Constants.MAP_CAMERA_ZOOM
import com.androiddevs.lerun.utils.Constants.POLYLINE_WIDTH
import com.androiddevs.lerun.utils.TrackingUtility
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.round

const val CANCEL_TRACKING_DIALOG_TAG = "CancelDialog"

@AndroidEntryPoint
class TrackingFragment :
    Fragment(),
    OnMapReadyCallback,
    GoogleMap.OnMyLocationButtonClickListener,
    GoogleMap.OnMyLocationClickListener {

    private var _binding: FragmentTrackingBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by viewModels()

    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var map: GoogleMap? = null

    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    private var currentTimeMillis = 0L

    private var menu: Menu? = null

    @set:Inject
    var weight = 80f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        _binding = FragmentTrackingBinding.inflate(inflater, container, false)
        val view = binding.root
        setHasOptionsMenu(true)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.onCreate(savedInstanceState)

        fusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(requireActivity())

        if (savedInstanceState != null) {
            val cancelTrackingDialog = parentFragmentManager.findFragmentByTag(
                CANCEL_TRACKING_DIALOG_TAG,
            ) as CancelTrackingDialog?

            cancelTrackingDialog?.setYesListener {
                stopRun()
            }
        }

        binding.btnToggleRun.setOnClickListener {
            toggleRun()
        }

        binding.mapView.getMapAsync(this)

        binding.mapView.getMapAsync {
            map = it
            addAllPolylines()
        }

        binding.cardCancelRun!!.setOnClickListener {
            showCancelTrackingDialog()
        }

        subscribeToObservers()

        binding.btnFinishRun.setOnClickListener {
            showDialogFinish()
        }
    }

    private fun showDialogFinish() {
        val dialog = BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
        val view = layoutInflater.inflate(R.layout.bottom_sheet, null)
        dialog.setContentView(view)

        val tvName = view.findViewById<EditText>(R.id.et_name)
        val btnSave = view.findViewById<MaterialButton>(R.id.btn_save_dialog)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btn_save_cancel)
        val duration = view.findViewById<TextView>(R.id.timer_sheet)
        val calories = view.findViewById<TextView>(R.id.calories_sheet)
        val distance = view.findViewById<TextView>(R.id.distance_sheet)
        val speed = view.findViewById<TextView>(R.id.speed_sheet)
        val validation = view.findViewById<TextView>(R.id.validation)

        var distanceInMeters = 0
        for (polyline in pathPoints) {
            distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
        }
        val averageSpeed =
            round((distanceInMeters / 1000f) / (currentTimeMillis / 1000f / 60 / 60) * 10) / 10f
        val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()

        // set data
        duration.text = TrackingUtility.getFormattedStopWatchTime(currentTimeMillis, true)
        distance.text = distanceInMeters.toString()
        calories.text = caloriesBurned.toString()
        speed.text = averageSpeed.toString()

        tvName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.isNullOrEmpty() || p0.isBlank()) {
                    validation.visibility = View.VISIBLE
                }
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                validation.visibility = View.GONE
            }
        })

        btnSave.setOnClickListener {
            if (tvName.text.isBlank() || tvName.text.isNullOrEmpty()) {
                validation.visibility = View.VISIBLE
            } else {
                val title = tvName.text.toString()

                zoomToSeeWholeTrack()
                endRunAndSaveToDatabase(title)
                dialog.dismiss()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        dialog.show()
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(
            viewLifecycleOwner,
            Observer {
                updateTracking(it)
            },
        )

        TrackingService.pathPoint.observe(
            viewLifecycleOwner,
            Observer {
                pathPoints = it
                addLatestPolyline()
                moveCameraToUser()
            },
        )

        TrackingService.timeRunInMillis.observe(
            viewLifecycleOwner,
            Observer {
                currentTimeMillis = it
                val formattedTime =
                    TrackingUtility.getFormattedStopWatchTime(currentTimeMillis, true)
                binding.tvTimer.text = formattedTime

                if (currentTimeMillis > 0L) {
                    binding.cardCancelRun!!.visibility = View.VISIBLE
                }
            },
        )
    }

    private fun toggleRun() {
        if (isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        } else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if (currentTimeMillis > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    private fun showCancelTrackingDialog() {
        CancelTrackingDialog().apply {
            setYesListener {
                stopRun()
            }
        }.show(parentFragmentManager, CANCEL_TRACKING_DIALOG_TAG)
    }

    private fun stopRun() {
        binding.tvTimer.text = "00:00:00:00"
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_trackingFragment_to_runFragment)
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        if (!isTracking && currentTimeMillis > 0L) {
            binding.textStart?.text = "Start"
            binding.icBtnStart?.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_play,
                ),
            )
            binding.btnFinishRun.visibility = View.VISIBLE
        } else if (isTracking) {
            binding.textStart?.text = "Stop"
            binding.icBtnStart?.setImageDrawable(
                ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_stop,
                ),
            )
            menu?.getItem(0)?.isVisible = true
            binding.btnFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_CAMERA_ZOOM,
                ),
            )
        }
    }

    private fun zoomToSeeWholeTrack() {
        val bounds = LatLngBounds.Builder()
        for (polyline in pathPoints) {
            for (position in polyline) {
                bounds.include(position)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt(),
            ),
        )
    }

    private fun endRunAndSaveToDatabase(title: String) {
        map?.snapshot { bitmap ->

            var distanceInMeters = 0
            for (polyline in pathPoints) {
                distanceInMeters += TrackingUtility.calculatePolylineLength(polyline).toInt()
            }

            val averageSpeed =
                round((distanceInMeters / 1000f) / (currentTimeMillis / 1000f / 60 / 60) * 10) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeters / 1000f) * weight).toInt()

            val run = Run(
                bitmap,
                title,
                dateTimeStamp,
                averageSpeed,
                distanceInMeters,
                currentTimeMillis,
                caloriesBurned,
            )
            viewModel.insertRun(run)

            Snackbar.make(
                requireActivity().findViewById(R.id.rootView),
                "Run saved successfully",
                Snackbar.LENGTH_LONG,
            ).show()

            stopRun()
        }
    }

    private fun addAllPolylines() {
        for (polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(ContextCompat.getColor(requireContext(), R.color.ijo))
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if (pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()

            val polylineOption = PolylineOptions()
                .color(ContextCompat.getColor(requireContext(), R.color.ijo))
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOption)
        }
    }

    private fun sendCommandToService(action: String) =
        Intent(requireContext(), TrackingService::class.java).also {
            it.action = action
            requireContext().startService(it)
        }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        map = googleMap
        map?.let { loadTheme(it) }

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        map?.isMyLocationEnabled = true
        map?.setOnMyLocationButtonClickListener(this)
        map?.setOnMyLocationClickListener(this)
        map?.uiSettings?.isMyLocationButtonEnabled = false

        fusedLocationProviderClient.lastLocation
            .addOnSuccessListener { location ->
                try {
                    val myLocation = LatLng(location.latitude, location.longitude)
                    map?.animateCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 16f))
                } catch (e: Exception) {
                    Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun loadTheme(googleMap: GoogleMap) {
        try {
            val isSuccess =
                googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireActivity(),
                        R.raw.style_json,
                    ),
                )

            if (!isSuccess) {
                Log.e("MAPS", "Failed")
            }
        } catch (e: Exception) {
            e.message?.let { Log.e("MAPS", it) }
        }
    }

    override fun onMyLocationButtonClick(): Boolean {
        return false
    }

    override fun onMyLocationClick(location: Location) {
    }
}
