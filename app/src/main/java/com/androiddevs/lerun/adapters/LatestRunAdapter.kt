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
import kotlinx.android.synthetic.main.new_item_run.view.*
import java.text.SimpleDateFormat
import java.util.*

class LatestRunAdapter : RecyclerView.Adapter<LatestRunAdapter.LatestRunViewHolder>() {

    private var onItemClickListener: ((Run) -> Unit)? = null

    fun setOnClickListener(listener: (Run) -> Unit) {
        onItemClickListener= listener
    }

    inner class LatestRunViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LatestRunViewHolder {
        return LatestRunViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.new_item_run,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LatestRunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            // img
            Glide.with(this).load(run.img).into(ivRunImage)

            //date
            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
            tvDate.text = dateFormat.format(calendar.time)

            val title = run.title
            tvTitleRun.text = title

            //distance
            val distanceInKm = "${run.distanceInMeters / 1000f } Km"
            tvDistance.text = distanceInKm

            //time
            tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            setOnClickListener {
                onItemClickListener?.let {
                    it(run)
                }
            }

        }
    }

    val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }

    val differ = AsyncListDiffer(this, diffCallback)

    fun submitLatestList(list: List<Run>) = differ.submitList(list)

    override fun getItemCount(): Int = differ.currentList.size.coerceAtMost(3)

}