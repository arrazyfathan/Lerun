package com.androiddevs.lerun.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.lerun.R
import com.androiddevs.lerun.db.Run
import com.androiddevs.lerun.utils.TrackingUtility
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_run.view.*
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {


    inner class RunViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_run,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            // img
            Glide.with(this).load(run.img).into(ivRunImage)

            //date
            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("dd.MM.yy", Locale.getDefault())
            tvDate.text = dateFormat.format(calendar.time)

            //average speed
            val averageSpeed = "${run.avgSpeedInKMH} km/h"
            tvAvgSpeed.text = averageSpeed

            //distance
            val distanceInKm = "${run.distanceInMeters / 1000f } km"
            tvDistance.text = distanceInKm

            //time
            tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            //calories
            val caloriesBurned = "${run.caloriesBurned} kcal"
            tvCalories.text = caloriesBurned
        }
    }

    override fun getItemCount(): Int = differ.currentList.size

    val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)



}