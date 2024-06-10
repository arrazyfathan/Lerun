package com.androiddevs.lerun.presentation.detailrun

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.androiddevs.lerun.R
import com.androiddevs.lerun.databinding.FragmentDetailRunBinding
import com.androiddevs.lerun.presentation.home.RunFragmentArgs
import com.androiddevs.lerun.utils.TrackingUtility
import com.androiddevs.lerun.utils.viewBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DetailRunFragment : Fragment(R.layout.fragment_detail_run) {


    private val binding by viewBinding(FragmentDetailRunBinding::bind)
    private val args: RunFragmentArgs by navArgs()

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars())
            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                topMargin = insets.top + 16
            }

            WindowInsetsCompat.CONSUMED
        }
        loadDetail()
    }

    private fun loadDetail() =
        with(binding) {
            val run = args.latestrun
            titleRun.text = run.title

            val calendar =
                Calendar.getInstance().apply {
                    timeInMillis = run.timestamp
                }
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
            dateRun.text = dateFormat.format(calendar.time)

            Glide.with(requireActivity()).load(run.img).into(snapshotImage)

            val distanceInKm = "${run.distanceInMeters / 1000f} Km"
            distanceDetail.text = distanceInKm

            durationDetail.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            val calories = "${run.caloriesBurned}kcal"
            caloriesDetail.text = calories

            val speed = "${run.avgSpeedInKMH}km/h"
            averageSpeedDetail.text = speed
        }
}
