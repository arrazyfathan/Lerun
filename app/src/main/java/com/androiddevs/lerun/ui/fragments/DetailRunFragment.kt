package com.androiddevs.lerun.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.androiddevs.lerun.databinding.FragmentDetailRunBinding
import com.androiddevs.lerun.utils.TrackingUtility
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetailRunFragment : Fragment() {

    private var _binding: FragmentDetailRunBinding? = null
    private val binding get() = _binding!!

    val args: RunFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetailRunBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadDetail()
    }

    private fun loadDetail() {
        val run = args.latestrun

        val title = run.title
        binding.titleRun.text = title

        val calendar = Calendar.getInstance().apply {
            timeInMillis = run.timestamp
        }
        val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
        binding.dateRun.text = dateFormat.format(calendar.time)

        Glide.with(requireActivity()).load(run.img).into(binding.snapshotImage)

        val distanceInKm = "${run.distanceInMeters / 1000f} Km"
        binding.distanceDetail.text = distanceInKm

        binding.durationDetail.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

        val calories = "${run.caloriesBurned}kcal"
        binding.caloriesDetail.text = calories

        val speed = "${run.avgSpeedInKMH}km/h"
        binding.averageSpeedDetail.text = speed
    }
}
