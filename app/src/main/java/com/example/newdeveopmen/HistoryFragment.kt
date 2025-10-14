package com.example.newdeveopmen

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import edu.sliit.myapplication.databinding.FragmentHistoryBinding

class HistoryFragment : Fragment() {
    
    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var viewModel: HistoryViewModel
    private lateinit var adapter: HistoryAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupViewModel()
        setupRecyclerView()
        setupFilters()
        observeHistory()
    }
    
    private fun setupViewModel() {
        val repository = ScanHistoryRepository(requireContext())
        val factory = HistoryViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[HistoryViewModel::class.java]
    }
    
    private fun setupRecyclerView() {
        adapter = HistoryAdapter(
            onComplete = { reservationId ->
                viewModel.completeBooking(reservationId)
            },
            onCancel = { reservationId ->
                viewModel.cancelBooking(reservationId)
            }
        )
        
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@HistoryFragment.adapter
        }
    }
    
    private fun setupFilters() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.loadAllHistory()
                    1 -> viewModel.loadHistoryByStatus("completed")
                    2 -> viewModel.loadHistoryByStatus("cancelled")
                    3 -> viewModel.loadHistoryByStatus("in-progress")
                }
            }
            
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })
    }
    
    private fun observeHistory() {
        viewModel.historyList.observe(viewLifecycleOwner) { historyList ->
            adapter.submitList(historyList)
            
            // Update stats
            val completedCount = historyList.count { it.isCompleted }
            val cancelledCount = historyList.count { it.isCancelled }
            val inProgressCount = historyList.count { !it.isCompleted && !it.isCancelled }
            val totalCount = historyList.size
            
            binding.tvTotalCount.text = totalCount.toString()
            binding.tvCompletedCount.text = completedCount.toString()
            binding.tvCancelledCount.text = cancelledCount.toString()
            binding.tvPendingCount.text = inProgressCount.toString()
            
            // Show/hide empty state
            if (historyList.isEmpty()) {
                binding.recyclerView.visibility = View.GONE
                binding.emptyState.visibility = View.VISIBLE
            } else {
                binding.recyclerView.visibility = View.VISIBLE
                binding.emptyState.visibility = View.GONE
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
