package com.androiddevs.lerun.adapters

import android.transition.AutoTransition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Interpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Slide
import com.androiddevs.lerun.R
import com.androiddevs.lerun.db.Run
import com.androiddevs.lerun.utils.TrackingUtility
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.item_run.view.*
import kotlinx.android.synthetic.main.item_run.view.ivRunImage
import kotlinx.android.synthetic.main.item_run_expandable.view.*
import kotlinx.android.synthetic.main.item_run_expandable.view.tvTitleRun
import kotlinx.android.synthetic.main.new_item_run.view.*
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

            //date
            val calendar = Calendar.getInstance().apply {
                timeInMillis = run.timestamp
            }
            val dateFormat = SimpleDateFormat("EEEE, d MMMM yyyy", Locale.getDefault())
            tvDateExpand.text = dateFormat.format(calendar.time)

            base_view.setOnClickListener {
                val slide = android.transition.Slide(Gravity.TOP)
                    .setDuration(600)
                    .addTarget(R.id.stats_view_expandable)


                if(stats_view_expandable.visibility == View.VISIBLE) {
                    TransitionManager.beginDelayedTransition(animation_parent, slide)
                    stats_view_expandable.visibility = View.GONE
                    image_button.setImageResource(R.drawable.ic_down)
                } else {
                    TransitionManager.beginDelayedTransition(animation_parent, slide)
                    stats_view_expandable.visibility = View.VISIBLE
                    image_button.setImageResource(R.drawable.ic_up)
                }
            }



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