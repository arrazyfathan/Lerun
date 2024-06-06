package com.androiddevs.lerun.presentation.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.lerun.data.local.db.Run
import com.androiddevs.lerun.databinding.NewItemRunBinding
import com.androiddevs.lerun.utils.TrackingUtility
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LatestRunAdapter : RecyclerView.Adapter<LatestRunAdapter.LatestRunViewHolder>() {
    private var onItemClickListener: ((Run) -> Unit)? = null

    fun setOnClickListener(listener: (Run) -> Unit) {
        onItemClickListener = listener
    }

    inner class LatestRunViewHolder(val binding: NewItemRunBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): LatestRunViewHolder {
        return LatestRunViewHolder(
            NewItemRunBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            ),
        )
    }

    override fun onBindViewHolder(
        holder: LatestRunViewHolder,
        position: Int,
    ) {
        val run = differ.currentList[position]
        with(holder) {
            // img
            Glide.with(holder.itemView.context).load(run.img).into(binding.ivRunImage)

            // date
            val calendar =
                Calendar.getInstance().apply {
                    timeInMillis = run.timestamp
                }
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
            binding.tvDate.text = dateFormat.format(calendar.time)

            val title = run.title
            binding.tvTitleRun.text = title

            // distance
            val distanceInKm = "${run.distanceInMeters / 1000f} Km"
            binding.tvDistance.text = distanceInKm

            // time
            binding.tvTime.text = TrackingUtility.getFormattedStopWatchTime(run.timeInMillis)

            itemView.setOnClickListener {
                onItemClickListener?.let {
                    it(run)
                }
            }
        }
    }

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

    val differ = AsyncListDiffer(this, diffCallback)

    fun submitLatestList(list: List<Run>) = differ.submitList(list)

    override fun getItemCount(): Int = differ.currentList.size.coerceAtMost(3)
}
