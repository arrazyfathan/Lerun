package com.androiddevs.lerun.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.lerun.R
import com.androiddevs.lerun.data.local.db.Run
import com.androiddevs.lerun.databinding.ItemRunExpandableBinding
import com.androiddevs.lerun.utils.TrackingUtility
import com.androiddevs.lerun.utils.changeBackgroundColor
import com.androiddevs.lerun.utils.changeTextColor
import com.androiddevs.lerun.utils.changeTintColor
import com.androiddevs.lerun.utils.collapse
import com.androiddevs.lerun.utils.color
import com.androiddevs.lerun.utils.expand
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {
    inner class RunViewHolder(val binding: ItemRunExpandableBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RunViewHolder {
        return RunViewHolder(
            ItemRunExpandableBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )
    }

    override fun onBindViewHolder(
        holder: RunViewHolder,
        position: Int,
    ) {
        val run = differ.currentList[position]
        with(holder) {
            Glide.with(holder.itemView.context).load(run.img).into(binding.ivRunImageExpand)

            binding.tvTitleRun.text = run.title

            val calendar =
                Calendar.getInstance().apply {
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
                    binding.imageButton.apply {
                        animate().rotation(0.0F).duration = 300
                        changeTintColor(
                            color(itemView.context, R.color.main_dark),
                            color(itemView.context, R.color.white),
                        )
                    }
                    binding.tvTitleRun.changeTextColor(
                        color(itemView.context, R.color.main_dark),
                        color(itemView.context, R.color.white),
                    )
                    binding.tvDateExpand.changeTextColor(
                        color(itemView.context, R.color.main_dark),
                        color(itemView.context, R.color.dark_accent_text),
                    )
                    val colorExpanded = color(itemView.context, R.color.ijo)
                    binding.rootView.changeBackgroundColor(
                        colorExpanded,
                        color(itemView.context, R.color.secondary_dark),
                    )
                    expanded = false
                } else {
                    binding.statsViewExpandable.expand()
                    binding.imageButton.apply {
                        animate().rotation(180.0F).duration = 300
                        changeTintColor(
                            color(itemView.context, R.color.white),
                            color(itemView.context, R.color.main_dark),
                        )
                    }
                    binding.tvTitleRun.changeTextColor(
                        color(itemView.context, R.color.white),
                        color(itemView.context, R.color.main_dark),
                    )
                    binding.tvDateExpand.changeTextColor(
                        color(itemView.context, R.color.dark_accent_text),
                        color(itemView.context, R.color.main_dark),
                    )
                    val colorCollapse =
                        color(itemView.context, R.color.secondary_dark)
                    binding.rootView.changeBackgroundColor(
                        colorCollapse,
                        color(itemView.context, R.color.ijo),
                    )
                    expanded = true
                }
            }
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    private val diffCallback =
        object : DiffUtil.ItemCallback<Run>() {
            override fun areItemsTheSame(
                oldItem: Run,
                newItem: Run,
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: Run,
                newItem: Run,
            ): Boolean {
                return oldItem.hashCode() == newItem.hashCode()
            }
        }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)
}
