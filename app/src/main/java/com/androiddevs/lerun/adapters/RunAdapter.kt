package com.androiddevs.lerun.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.lerun.databinding.ItemRunExpandableBinding
import com.androiddevs.lerun.db.Run
import com.androiddevs.lerun.utils.TrackingUtility
import com.androiddevs.lerun.utils.collapse
import com.androiddevs.lerun.utils.expand
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_run_expandable.view.tvTitleRun
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(val binding: ItemRunExpandableBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            ItemRunExpandableBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        with(holder) {
            Glide.with(holder.itemView.context).load(run.img).into(binding.ivRunImageExpand)

            binding.tvTitleRun.text = run.title

            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
            binding.tvDateExpand.text = dateFormat.format(calendar.time)

            val distanceInKm = "${run.distanceInMeters / 1000f} Km"
            binding.distanceDetail.text = distanceInKm

            binding.durationDetail.text =
                TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            val calories = "${run.caloriesBurned}kcal"
            binding.caloriesDetail.text = calories

            val speed = "${run.avgSpeedInKMH}km/h"
            binding.averageSpeedDetail.text = speed

            var expanded = false

            binding.rootView.setOnClickListener {
                if (expanded) {
                    binding.statsViewExpandable.collapse()
                    binding.imageButton.animate().rotation(0.0F).duration = 300
                    expanded = false
                } else {
                    binding.statsViewExpandable.expand()
                    binding.imageButton.animate().rotation(180.0F).duration = 300
                    expanded = true
                }
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    private val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)
}
