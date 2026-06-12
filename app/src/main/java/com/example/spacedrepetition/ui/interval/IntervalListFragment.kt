package com.example.spacedrepetition.ui.interval

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spacedrepetition.databinding.FragmentIntervalListBinding

class IntervalListFragment : Fragment() {

    private var _binding: FragmentIntervalListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: IntervalViewModel by activityViewModels()
    private lateinit var adapter: IntervalListAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIntervalListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = IntervalListAdapter(
            onDelete = { interval ->
                viewModel.deleteInterval(interval)
            },
            onSetDefault = { interval ->
                viewModel.setAsDefault(interval)
            }
        )

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        viewModel.intervals.observe(viewLifecycleOwner, Observer { intervals ->
            adapter.submitList(intervals)
            binding.emptyView.visibility = if (intervals.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
