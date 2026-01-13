// ui/home/HomeActivity.kt
package com.thefarhany.eventapp.ui.home

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.thefarhany.eventapp.R
import com.thefarhany.eventapp.data.remote.RetrofitClient
import com.thefarhany.eventapp.data.repository.EventRepository
import com.thefarhany.eventapp.databinding.ActivityHomeBinding
import com.thefarhany.eventapp.ui.profile.ProfileFragment
import com.thefarhany.eventapp.ui.search.SearchFragment
import com.thefarhany.eventapp.ui.tickets.MyTicketFragment
import com.thefarhany.eventapp.utils.Resource

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: EventViewModel
    private lateinit var eventAdapter: EventAdapter
    private var currentFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewModel()
        setupRecyclerView()
        setupCategoryChips()
        setupBottomNavigation()
        observeEvents()

        // Default: Show home content
        if (savedInstanceState == null) {
            showHomeContent()
        }
    }

    private fun setupViewModel() {
        val repository = EventRepository(RetrofitClient.instance)
        val factory = EventViewModelFactory(repository)
        viewModel = ViewModelProvider(this, factory)[EventViewModel::class.java]
    }

    private fun setupRecyclerView() {
        eventAdapter = EventAdapter { event ->
            Toast.makeText(this, "Event: ${event.title}", Toast.LENGTH_SHORT).show()
        }

        binding.homeContent.rvEvents.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = eventAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupCategoryChips() {
        val chipGroup = binding.homeContent.chipGroupCategories

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                // No chip selected, show all
                viewModel.loadEventsByCategory("ALL")
            } else {
                val checkedChip = group.findViewById<Chip>(checkedIds[0])
                val category = checkedChip.text.toString()
                viewModel.loadEventsByCategory(category)
            }
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    showHomeContent()
                    true
                }
                R.id.navigation_search -> {
                    showFragment(SearchFragment())
                    true
                }
                R.id.navigation_bookings -> {
                    showFragment(MyTicketFragment())
                    true
                }
                R.id.navigation_profile -> {
                    showFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun observeEvents() {
        viewModel.filteredEvents.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    resource.data?.let { events ->
                        if (events.isEmpty()) {
                            showEmptyState(true)
                            eventAdapter.submitList(emptyList())
                        } else {
                            showEmptyState(false)
                            eventAdapter.submitList(events)
                        }
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    showEmptyState(true)
                    Toast.makeText(
                        this,
                        resource.message ?: "Failed to load events",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.homeContent.progressBar.visibility =
            if (isLoading) View.VISIBLE else View.GONE
        binding.homeContent.rvEvents.visibility =
            if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.homeContent.tvEmptyState.visibility =
            if (isEmpty) View.VISIBLE else View.GONE
        binding.homeContent.rvEvents.visibility =
            if (isEmpty) View.GONE else View.VISIBLE
    }

    private fun showHomeContent() {
        // Show home content ScrollView
        binding.homeContentScrollView.visibility = View.VISIBLE

        // Remove current fragment if exists
        currentFragment?.let { fragment ->
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
            currentFragment = null
        }

        // Refresh events when back to home
        viewModel.loadAllEvents()
    }

    private fun showFragment(fragment: Fragment) {
        // Hide home content
        binding.homeContentScrollView.visibility = View.GONE

        // Replace fragment
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()

        currentFragment = fragment
    }
}
