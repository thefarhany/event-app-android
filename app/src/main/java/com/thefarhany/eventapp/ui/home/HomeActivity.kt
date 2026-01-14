package com.thefarhany.eventapp.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.chip.Chip
import com.thefarhany.eventapp.R
import com.thefarhany.eventapp.data.model.response.EventCategories
import com.thefarhany.eventapp.data.remote.RetrofitClient
import com.thefarhany.eventapp.data.repository.EventRepository
import com.thefarhany.eventapp.databinding.ActivityHomeBinding
import com.thefarhany.eventapp.ui.auth.login.LoginActivity
import com.thefarhany.eventapp.ui.event.EventDetailActivity
import com.thefarhany.eventapp.ui.profile.ProfileFragment
import com.thefarhany.eventapp.ui.search.SearchFragment
import com.thefarhany.eventapp.ui.tickets.MyTicketFragment
import com.thefarhany.eventapp.utils.Resource
import com.thefarhany.eventapp.utils.SessionManager

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var viewModel: EventViewModel
    private lateinit var eventAdapter: EventAdapter
    private lateinit var sessionManager: SessionManager
    private var currentFragment: Fragment? = null

    companion object {
        private const val TAG = "HomeActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        RetrofitClient.init(this)
        sessionManager = SessionManager(this)

        if (!sessionManager.isLoggedIn()) {
            redirectToLogin()
            return
        }

        setupViewModel()
        setupRecyclerView()
        setupCategoryChips()
        setupBottomNavigation()
        observeEvents()

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
            val intent = Intent(this, EventDetailActivity::class.java)
            intent.putExtra("EVENT_ID", event.eventId)
            startActivity(intent)
        }

        binding.homeContent.rvEvents.apply {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = eventAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupCategoryChips() {
        val chipGroup = binding.homeContent.chipGroupCategories

        chipGroup.removeAllViews()

        val allChip = createChip("All", isChecked = true)
        allChip.id = View.generateViewId()
        chipGroup.addView(allChip)

        EventCategories.entries.forEach { category ->
            val chip = createChip(category.displayValue, isChecked = false)
            chip.id = View.generateViewId()
            chip.tag = category.name
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                viewModel.loadAllEvents()
            } else {
                val checkedChip = group.findViewById<Chip>(checkedIds[0])
                val categoryText = checkedChip.text.toString()

                if (categoryText == "All") {
                    viewModel.loadAllEvents()
                } else {
                    val categoryEnum = checkedChip.tag as? String
                    if (categoryEnum != null) {
                        viewModel.loadEventsByCategory(categoryEnum)
                    } else {
                        viewModel.loadAllEvents()
                    }
                }
            }
        }
    }

    private fun createChip(text: String, isChecked: Boolean = false): Chip {
        return Chip(this).apply {
            this.text = text
            this.isCheckable = true
            this.isChecked = isChecked

            chipBackgroundColor = if (isChecked) {
                resources.getColorStateList(R.color.primary, null)
            } else {
                resources.getColorStateList(R.color.gray_200, null)
            }

            setTextColor(if (isChecked) {
                resources.getColor(R.color.white, null)
            } else {
                resources.getColor(R.color.gray_700, null)
            })

            setOnCheckedChangeListener { _, checked ->
                chipBackgroundColor = if (checked) {
                    resources.getColorStateList(R.color.primary, null)
                } else {
                    resources.getColorStateList(R.color.gray_200, null)
                }
                setTextColor(if (checked) {
                    resources.getColor(R.color.white, null)
                } else {
                    resources.getColor(R.color.gray_700, null)
                })
            }

            chipStrokeWidth = 0f
            textSize = 14f
            setPadding(32, 16, 32, 16)
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
                    showEmptyState(false)
                }

                is Resource.Success -> {
                    showLoading(false)
                    resource.data?.let { events ->
                        if (events.isEmpty()) {
                            showEmptyState(true, "No events available")
                            eventAdapter.submitList(emptyList())
                        } else {
                            showEmptyState(false)
                            eventAdapter.submitList(events)
                        }
                    }
                }

                is Resource.Error -> {
                    showLoading(false)

                    val errorMessage = resource.message ?: "Failed to load events. Please try again."

                    if (errorMessage.contains("Unauthorized", ignoreCase = true) ||
                        errorMessage.contains("401", ignoreCase = true)) {
                        handleUnauthorized()
                    } else {
                        showEmptyState(true, errorMessage)
                        eventAdapter.submitList(emptyList())
                    }
                }
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.homeContent.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.homeContent.rvEvents.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun showEmptyState(isEmpty: Boolean, message: String = "No events available") {
        binding.homeContent.tvEmptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
        binding.homeContent.rvEvents.visibility = if (isEmpty) View.GONE else View.VISIBLE

        if (isEmpty) {
            binding.homeContent.tvEmptyState.text = message
        }
    }

    private fun showHomeContent() {
        binding.homeContentScrollView.visibility = View.VISIBLE

        currentFragment?.let { fragment ->
            supportFragmentManager.beginTransaction()
                .remove(fragment)
                .commit()
            currentFragment = null
        }

        viewModel.loadAllEvents()
    }

    private fun showFragment(fragment: Fragment) {
        binding.homeContentScrollView.visibility = View.GONE

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()

        currentFragment = fragment
    }

    private fun handleUnauthorized() {
        sessionManager.clearSession()
        Toast.makeText(
            this,
            "Session expired. Please login again.",
            Toast.LENGTH_LONG
        ).show()
        redirectToLogin()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
