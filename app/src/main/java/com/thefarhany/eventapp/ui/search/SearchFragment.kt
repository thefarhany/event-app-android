package com.thefarhany.eventapp.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.thefarhany.eventapp.data.remote.RetrofitClient
import com.thefarhany.eventapp.data.repository.EventRepository
import com.thefarhany.eventapp.databinding.FragmentSearchBinding
import com.thefarhany.eventapp.ui.event.EventDetailActivity
import com.thefarhany.eventapp.ui.home.EventAdapter
import com.thefarhany.eventapp.ui.home.EventViewModel
import com.thefarhany.eventapp.ui.home.EventViewModelFactory
import com.thefarhany.eventapp.utils.Resource

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: EventViewModel
    private lateinit var searchAdapter: EventAdapter

    companion object {
        private const val TAG = "SearchFragment"
        private const val SEARCH_DELAY_MS = 500L
    }

    private var searchRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecyclerView()
        setupSearchBar()
        observeSearchResults()
        showInitialState()
    }

    private fun setupViewModel() {
        val repository = EventRepository(RetrofitClient.instance)
        val factory = EventViewModelFactory(repository)
        viewModel = ViewModelProvider(requireActivity(), factory)[EventViewModel::class.java]
    }

    private fun setupRecyclerView() {
        searchAdapter = EventAdapter { event ->
            val intent = Intent(requireContext(), EventDetailActivity::class.java)
            intent.putExtra("EVENT_ID", event.eventId)
            startActivity(intent)
        }

        binding.rvSearchResults.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearchBar() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.btnClearSearch.visibility = if (s.isNullOrEmpty()) {
                    View.GONE
                } else {
                    View.VISIBLE
                }

                // Debounce search
                searchRunnable?.let { binding.root.removeCallbacks(it) }

                if (!s.isNullOrBlank()) {
                    searchRunnable = Runnable {
                        performSearch(s.toString())
                    }
                    binding.root.postDelayed(searchRunnable!!, SEARCH_DELAY_MS)
                } else {
                    showInitialState()
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.btnClearSearch.setOnClickListener {
            binding.etSearch.text?.clear()
            showInitialState()
        }

        binding.etSearch.setOnEditorActionListener { _, _, _ ->
            val keyword = binding.etSearch.text?.toString()
            if (!keyword.isNullOrBlank()) {
                performSearch(keyword)
            }
            false
        }
    }

    private fun performSearch(keyword: String) {
        viewModel.searchEvents(keyword.trim())
    }

    private fun observeSearchResults() {
        viewModel.filteredEvents.observe(viewLifecycleOwner) { resource ->
            val searchKeyword = binding.etSearch.text?.toString()?.trim()

            if (searchKeyword.isNullOrBlank()) {
                return@observe
            }

            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                    hideEmptyState()
                    hideResultCount()
                }
                is Resource.Success -> {
                    showLoading(false)
                    resource.data?.let { events ->
                        if (events.isEmpty()) {
                            showResultCount(0)
                            showEmptyState(
                                "No events match your search",
                                "Try different keywords or browse all events"
                            )
                            searchAdapter.submitList(emptyList())
                        } else {
                            hideEmptyState()
                            showResultCount(events.size)
                            binding.rvSearchResults.visibility = View.VISIBLE
                            searchAdapter.submitList(events)
                        }
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    val errorMessage = resource.message ?: "Failed to search events"
                    if (errorMessage.contains("No events", ignoreCase = true)) {
                        showResultCount(0)
                        showEmptyState(
                            "No events match your search",
                            "Try different keywords or browse all events"
                        )
                    } else {
                        hideResultCount()
                        showEmptyState("Search failed", errorMessage)
                    }
                    searchAdapter.submitList(emptyList())
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.rvSearchResults.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showInitialState() {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.rvSearchResults.visibility = View.GONE
        binding.tvEmptyState.text = "Search for events"
        binding.tvEmptySubtext.text = "Enter keywords to find events"

        searchAdapter.submitList(emptyList())
        hideResultCount()
    }

    private fun showEmptyState(title: String, subtitle: String) {
        binding.layoutEmptyState.visibility = View.VISIBLE
        binding.rvSearchResults.visibility = View.GONE
        binding.tvEmptyState.text = title
        binding.tvEmptySubtext.text = subtitle
    }

    private fun hideEmptyState() {
        binding.layoutEmptyState.visibility = View.GONE
    }

    private fun showResultCount(count: Int) {
        binding.tvResultCount.visibility = View.VISIBLE
        binding.tvResultCount.text = when (count) {
            0 -> "0 results found"
            1 -> "1 result found"
            else -> "$count results found"
        }
    }

    private fun hideResultCount() {
        binding.tvResultCount.visibility = View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        searchRunnable?.let { binding.root.removeCallbacks(it) }
        _binding = null
    }
}
