package com.example.spacedrepetition.ui.topic

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spacedrepetition.data.Interval
import com.example.spacedrepetition.data.Topic
import com.example.spacedrepetition.databinding.ItemTopicBinding

class TopicListAdapter(
    private val onDelete: (Topic) -> Unit,
    private val intervals: MutableList<Interval> = mutableListOf()
) : ListAdapter<Topic, TopicListAdapter.ViewHolder>(DiffCallback) {

    private val MILLISECONDS_PER_DAY: Long = 24 * 60 * 60 * 1000

    private fun daysSince(timestamp: Long): String {
        if (timestamp <= 0) return "Never"
        val days = (System.currentTimeMillis() - timestamp) / MILLISECONDS_PER_DAY
        return when {
            days == 0L -> "Today"
            days == 1L -> "1 day ago"
            else -> "$days days ago"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTopicBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun updateIntervals(intervals: List<Interval>) {
        this.intervals.clear()
        this.intervals.addAll(intervals)
        notifyDataSetChanged()
    }

    private fun findIntervalName(intervalId: Long): String {
        return intervals.find { it.id == intervalId }?.name ?: "Unknown"
    }

    private fun findIntervalNotificationCount(intervalId: Long): Int {
        return intervals.find { it.id == intervalId }?.notificationTimes?.size ?: 0
    }

    inner class ViewHolder(private val binding: ItemTopicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(topic: Topic) {
            binding.titleText.text = topic.title
            binding.intervalText.text = "Interval: ${findIntervalName(topic.intervalId)}"

            val totalNotifications = findIntervalNotificationCount(topic.intervalId)
            val arrived = topic.arrivedNotificationCount.coerceAtMost(totalNotifications)
            binding.progressText.text = "$arrived / $totalNotifications"

            if (totalNotifications > 0) {
                binding.progressBar.progress = (arrived * 100) / totalNotifications
                binding.progressBar.visibility = android.view.View.VISIBLE
                binding.progressText.visibility = android.view.View.VISIBLE
            } else {
                binding.progressBar.visibility = android.view.View.GONE
                binding.progressText.visibility = android.view.View.GONE
            }

            // Last notified info
            binding.lastNotifiedText.text = "Last notified: ${daysSince(topic.lastNotifiedAt)}"
            binding.lastNotifiedText.visibility = android.view.View.VISIBLE

            binding.deleteButton.setOnClickListener {
                showDeleteConfirmation(topic)
            }
        }

        private fun showDeleteConfirmation(topic: Topic) {
            val context = binding.root.context
            if (context is FragmentActivity) {
                AlertDialog.Builder(context)
                    .setTitle("Delete Topic")
                    .setMessage("Are you sure you want to delete \"${topic.title}\"?")
                    .setPositiveButton("Delete") { _, _ ->
                        onDelete(topic)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Topic>() {
            override fun areItemsTheSame(old: Topic, new: Topic) = old.id == new.id
            override fun areContentsTheSame(old: Topic, new: Topic) = old == new
        }
    }
}