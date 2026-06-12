package com.example.spacedrepetition

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.DialogFragment
import com.example.spacedrepetition.databinding.DialogAddTopicBinding
import com.example.spacedrepetition.ui.topic.TopicViewModel

class AddTopicDialog(
    private val viewModel: TopicViewModel
) : DialogFragment() {

    private var _binding: DialogAddTopicBinding? = null
    private val binding get() = _binding!!

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
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddTopicBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var selectedInterval: com.example.spacedrepetition.data.Interval? = null

        binding.doneButton.isEnabled = false

        fun updateDoneButton() {
            val titleOk = binding.titleEditText.text?.toString()?.trim()?.isNotEmpty() == true
            binding.doneButton.isEnabled = titleOk && selectedInterval != null
        }

        // Enable/disable Done button based on title input
        binding.titleEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateDoneButton()
            }
        })

        // Populate interval spinner
        viewModel.intervals.observe(viewLifecycleOwner) { intervals ->
            val names = intervals.map { it.name }
            val adapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                names
            )
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.intervalSpinner.adapter = adapter
            // Reset selection when intervals change
            selectedInterval = null
            updateDoneButton()
        }

        binding.intervalSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>, view: View?, position: Int, id: Long) {
                val intervals = viewModel.intervals.value
                selectedInterval = if (intervals != null && position < intervals.size) {
                    intervals[position]
                } else {
                    null
                }
                updateDoneButton()
            }
            override fun onNothingSelected(parent: android.widget.AdapterView<*>) {
                selectedInterval = null
                updateDoneButton()
            }
        }

        binding.doneButton.setOnClickListener {
            saveTopic()
        }

        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }

    private fun saveTopic() {
        val title = binding.titleEditText.text?.toString()?.trim() ?: ""
        val intervals = viewModel.intervals.value ?: return
        val position = binding.intervalSpinner.selectedItemPosition
        val selectedInterval = if (position >= 0 && position < intervals.size) intervals[position] else null
        if (selectedInterval == null) return

        viewModel.addTopic(title, selectedInterval.id)
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}