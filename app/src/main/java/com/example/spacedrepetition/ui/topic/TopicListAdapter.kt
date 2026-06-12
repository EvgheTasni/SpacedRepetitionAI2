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
    private val onTogglePause: (Topic, Boolean) -> Unit,
    private val intervals: MutableList<Interval> = mutableListOf()
) : ListAdapter<Topic, TopicListAdapter.ViewHolder>(DiffCallback) {

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

            // Pause/Resume button
            if (topic.isPaused) {
                binding.pauseButton.setImageResource(android.R.drawable.ic_media_play)
                binding.pauseButton.contentDescription = "Resume topic"
                binding.root.alpha = 0.6f
            } else {
                binding.pauseButton.setImageResource(android.R.drawable.ic_media_pause)
                binding.pauseButton.contentDescription = "Pause topic"
                binding.root.alpha = 1.0f
            }

            binding.pauseButton.setOnClickListener {
                onTogglePause(topic, !topic.isPaused)
            }

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