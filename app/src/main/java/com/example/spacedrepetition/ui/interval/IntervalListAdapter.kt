package com.example.spacedrepetition.ui.interval

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spacedrepetition.data.Interval
import com.example.spacedrepetition.databinding.ItemIntervalBinding

class IntervalListAdapter :
    ListAdapter<Interval, IntervalListAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIntervalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemIntervalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(interval: Interval) {
            binding.nameText.text = interval.name
            binding.durationText.text = formatDuration(interval.durationMillis)
        }

        private fun formatDuration(millis: Long): String {
            val hours = millis / 3_600_000
            val minutes = (millis % 3_600_000) / 60_000
            return when {
                hours > 0 -> "${hours}h ${minutes}m"
                minutes > 0 -> "${minutes}m"
                else -> "${millis / 1000}s"
            }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Interval>() {
        override fun areItemsTheSame(old: Interval, new: Interval) = old.id == new.id
        override fun areContentsTheSame(old: Interval, new: Interval) = old == new
    }
}
