package com.androiddevs.lerun.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.androiddevs.lerun.R
import com.androiddevs.lerun.db.Run
import com.androiddevs.lerun.utils.collapse
import com.androiddevs.lerun.utils.expand
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_run_expandable.view.*
import kotlinx.android.synthetic.main.item_run_expandable.view.tvTitleRun
import java.text.SimpleDateFormat
import java.util.*

class RunAdapter : RecyclerView.Adapter<RunAdapter.RunViewHolder>() {

    inner class RunViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunViewHolder {
        return RunViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_run_expandable,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RunViewHolder, position: Int) {
        val run = differ.currentList[position]
        holder.itemView.apply {
            // img
            Glide.with(this).load(run.img).into(ivRunImageExpand)

            tvTitleRun.text = run.title

            // date
            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
            tvDateExpand.text = dateFormat.format(calendar.time)

            var expanded = false

            root_view.setOnClickListener {
                if (expanded) {
                    stats_view_expandable.collapse()
                    image_button.animate().rotation(0.0F).duration = 300
                    expanded = false
                } else {
                    stats_view_expandable.expand()
                    image_button.animate().rotation(180.0F).duration = 300
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
