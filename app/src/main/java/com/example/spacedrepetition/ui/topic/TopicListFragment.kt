package com.example.spacedrepetition.ui.topic

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spacedrepetition.AddTopicDialog
import com.example.spacedrepetition.databinding.FragmentTopicListBinding

class TopicListFragment : Fragment() {

    private var _binding: FragmentTopicListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TopicViewModel by activityViewModels()
    private lateinit var adapter: TopicListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTopicListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = TopicListAdapter(
            onDelete = { topic ->
                viewModel.deleteTopic(topic)
            },
            onTogglePause = { topic, paused ->
                viewModel.setPaused(topic, paused)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.topics.observe(viewLifecycleOwner, Observer { topics ->
            adapter.submitList(topics)
            binding.emptyView.visibility = if (topics.isEmpty()) View.VISIBLE else View.GONE
        })

        viewModel.intervals.observe(viewLifecycleOwner, Observer { intervals ->
            adapter.updateIntervals(intervals)
        })

        binding.fab.setOnClickListener {
            AddTopicDialog(viewModel).show(parentFragmentManager, "add_topic")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}