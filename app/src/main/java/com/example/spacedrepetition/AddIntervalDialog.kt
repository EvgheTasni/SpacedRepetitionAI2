package com.example.spacedrepetition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.spacedrepetition.databinding.DialogAddIntervalBinding
import com.example.spacedrepetition.ui.interval.IntervalViewModel

class AddIntervalDialog(
    private val viewModel: IntervalViewModel
) : DialogFragment() {

    private var _binding: DialogAddIntervalBinding? = null
    private val binding get() = _binding!!

    private lateinit var timespanAdapter: TimespanAdapter
    private val timespans = mutableListOf<Int>()

    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            setLayout(
                (resources.displayMetrics.widthPixels * 0.9).toInt(),
                android.view.WindowManager.LayoutParams.WRAP_CONTENT
            )
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddIntervalBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        timespanAdapter = TimespanAdapter(
            onAdd = { addTimespan() },
            onRemove = { position -> removeTimespan(position) }
        )

        binding.timespanRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.timespanRecyclerView.adapter = timespanAdapter

        // Start with one timespan
        addTimespan()

        binding.addTimespanButton.setOnClickListener {
            addTimespan()
        }

        binding.doneButton.setOnClickListener {
            saveInterval()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun addTimespan() {
        val position = timespans.size
        timespans.add(1)
        timespanAdapter.notifyItemInserted(position)
        // Scroll to the new item
        binding.timespanRecyclerView.smoothScrollToPosition(position)
    }

    private fun removeTimespan(position: Int) {
        if (timespans.size > 1) {
            timespans.removeAt(position)
            timespanAdapter.notifyItemRemoved(position)
        } else {
            // Clear the last one instead of removing
            timespans[0] = 0
            timespanAdapter.notifyItemChanged(0)
        }
    }

    private fun saveInterval() {
        val name = binding.nameEditText.text?.toString()?.trim() ?: ""
        
        if (name.isEmpty()) {
            binding.nameEditText.error = "Name is required"
            return
        }

        if (!viewModel.isNameUnique(name)) {
            binding.nameEditText.error = "Name already exists"
            return
        }
        binding.nameEditText.error = null

        val days = timespans.filter { it > 0 }
        if (days.isEmpty()) {
            return
        }

        val notificationTimes = days.map { it * 24L * 60 * 60 * 1000 }
        viewModel.addInterval(name, notificationTimes)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    inner class TimespanAdapter(
        private val onAdd: () -> Unit,
        private val onRemove: (Int) -> Unit
    ) : RecyclerView.Adapter<TimespanAdapter.TimespanViewHolder>() {

        override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): TimespanViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_timespan_input, parent, false)
            return TimespanViewHolder(view)
        }

        override fun onBindViewHolder(holder: TimespanViewHolder, position: Int) {
            holder.bind(timespans[position], position)
        }

        override fun getItemCount() = timespans.size

        inner class TimespanViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            private val daysEditText: EditText = view.findViewById(R.id.daysEditText)
            private val removeButton: View = view.findViewById(R.id.removeButton)

            fun bind(days: Int, position: Int) {
                daysEditText.setText(days.toString())
                removeButton.setOnClickListener {
                    onRemove(position)
                }
                // Update the value when text changes
                daysEditText.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        timespans[position] = daysEditText.text.toString().toIntOrNull() ?: 0
                    }
                }
            }
        }
    }
}
