package com.example.spacedrepetition.ui.interval

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.spacedrepetition.data.Interval
import com.example.spacedrepetition.data.Topic
import com.example.spacedrepetition.databinding.ItemIntervalBinding

class IntervalListAdapter(
    private val onDelete: (Interval) -> Unit,
    private val onSetDefault: (Interval) -> Unit,
    private val getLinkedTopics: (Long) -> List<Topic> = { emptyList() }
) : ListAdapter<Interval, IntervalListAdapter.ViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemIntervalBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(private val binding: ItemIntervalBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(interval: Interval) {
            binding.nameText.text = interval.name
            binding.durationText.text = formatInterval(interval.notificationTimes)

            // Update star icon based on default status
            binding.defaultButton.setImageResource(
                if (interval.isDefault) android.R.drawable.star_big_on
                else android.R.drawable.star_big_off
            )
            binding.defaultButton.contentDescription = if (interval.isDefault) {
                "Unset as Default"
            } else {
                "Set as Default"
            }

            binding.defaultButton.setOnClickListener {
                onSetDefault(interval)
            }

            binding.deleteButton.setOnClickListener {
                showDeleteConfirmation(interval)
            }
        }

        private fun showDeleteConfirmation(interval: Interval) {
            val context = binding.root.context
            if (context is FragmentActivity) {
                val linkedTopics = getLinkedTopics(interval.id)
                val message = buildString {
                    append("Are you sure you want to delete \"${interval.name}\"?")
                    if (linkedTopics.isNotEmpty()) {
                        append("\n\nThe following topics will also be deleted:\n")
                        linkedTopics.forEach { t ->
                            append("• ${t.title}\n")
                        }
                    }
                }
                AlertDialog.Builder(context)
                    .setTitle("Delete Interval")
                    .setMessage(message)
                    .setPositiveButton("Delete") { _, _ ->
                        onDelete(interval)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
        }

        private fun formatInterval(times: List<Long>): String {
            if (times.isEmpty()) return "Never"

            val daysInMillis = 24 * 60 * 60 * 1000L
            val formatted = times.map { millis ->
                when {
                    millis >= daysInMillis * 7 -> "${millis / daysInMillis / 7}w"
                    millis >= daysInMillis -> "${millis / daysInMillis}d"
                    millis >= 60_000 -> "${millis / 60_000}h"
                    else -> "${millis / 1000}s"
                }
            }
            return formatted.joinToString(", ")
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<Interval>() {
        override fun areItemsTheSame(old: Interval, new: Interval) = old.id == new.id
        override fun areContentsTheSame(old: Interval, new: Interval) = old == new
    }
}
