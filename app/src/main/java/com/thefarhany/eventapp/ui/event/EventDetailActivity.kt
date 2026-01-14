package com.thefarhany.eventapp.ui.event

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.thefarhany.eventapp.R
import com.thefarhany.eventapp.data.model.CreateBookingRequest
import com.thefarhany.eventapp.data.model.EventDetail
import com.thefarhany.eventapp.data.remote.RetrofitClient
import com.thefarhany.eventapp.data.repository.EventRepository
import com.thefarhany.eventapp.databinding.ActivityEventDetailBinding
import com.thefarhany.eventapp.ui.booking.BookingViewModel
import com.thefarhany.eventapp.ui.home.HomeActivity
import com.thefarhany.eventapp.ui.tickets.BookingViewModelFactory
import com.thefarhany.eventapp.utils.Resource
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class EventDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEventDetailBinding
    private lateinit var eventDetailViewModel: EventDetailViewModel

    // ✅ Add BookingViewModel
    private val bookingViewModel: BookingViewModel by viewModels {
        BookingViewModelFactory(application)
    }

    private lateinit var ticketAdapter: TicketAdapter
    private var eventId: Long = -1L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEventDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupViewModel()
        setupRecyclerView()
        observeViewModels()

        eventId = intent.getLongExtra("EVENT_ID", -1)
        if (eventId != -1L) {
            eventDetailViewModel.loadEventDetail(eventId)
        } else {
            Toast.makeText(this, "Invalid event ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupViewModel() {
        val apiService = RetrofitClient.instance
        val repository = EventRepository(apiService)
        val factory = EventDetailViewModelFactory(repository)
        eventDetailViewModel = ViewModelProvider(this, factory)[EventDetailViewModel::class.java]
    }

    private fun setupRecyclerView() {
        ticketAdapter = TicketAdapter { ticketSelections ->
            eventDetailViewModel.updateTicketSelections(ticketSelections)
        }

        binding.rvTickets.apply {
            layoutManager = LinearLayoutManager(this@EventDetailActivity)
            adapter = ticketAdapter
        }
    }

    private fun observeViewModels() {
        // Observe event detail
        eventDetailViewModel.eventDetail.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    resource.data?.let { displayEventDetails(it) }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(this, resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // Observe total tickets
        eventDetailViewModel.totalTickets.observe(this) { total ->
            binding.btnBookNow.isEnabled = total > 0
            binding.btnBookNow.text = if (total > 0) {
                "Book Now ($total tickets)"
            } else {
                "Book Now"
            }
        }

        // Observe total price
        eventDetailViewModel.totalPrice.observe(this) { totalPrice ->
            // Display total price if needed
        }

        // ✅ Observe create booking result
        bookingViewModel.createBooking.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showBookingLoading(true)
                }
                is Resource.Success -> {
                    showBookingLoading(false)
                    bookingViewModel.resetCreateBookingState()

                    Toast.makeText(
                        this,
                        "Booking successful! Please proceed to payment",
                        Toast.LENGTH_LONG
                    ).show()

                    // Navigate to MyTicketFragment
                    navigateToMyBookings()
                }
                is Resource.Error -> {
                    showBookingLoading(false)
                    bookingViewModel.resetCreateBookingState()

                    showErrorDialog(resource.message ?: "Failed to create booking")
                }
                else -> {}
            }
        }

        // Book Now button
        binding.btnBookNow.setOnClickListener {
            handleBookNow()
        }
    }

    private fun displayEventDetails(event: EventDetail) {
        binding.apply {
            // Title
            collapsingToolbar.title = event.title
            tvEventTitle.text = event.title
            tvShortSummary.text = event.shortSummary ?: ""
            tvDescription.text = event.description

            // Format date
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val displayFormat = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale("id", "ID"))
            try {
                val date = dateFormat.parse(event.date)
                tvEventDate.text = date?.let { displayFormat.format(it) } ?: event.date
            } catch (e: Exception) {
                tvEventDate.text = event.date
            }

            // Format time
            tvEventTime.text = "${event.time} WIB"

            // Location (only for offline events)
            if (event.location != null) {
                layoutLocation.visibility = View.VISIBLE
                tvVenue.text = event.location.venue
                tvAddress.text = "${event.location.address}, ${event.location.city}"
            } else {
                layoutLocation.visibility = View.GONE
            }

            // Capacity
            tvCapacity.text = "${event.remainingCapacity} / ${event.totalCapacity} seats available"

            // Warning banner
            showWarningIfNeeded(event.remainingCapacity, event.totalCapacity)

            // Load image
            Glide.with(this@EventDetailActivity)
                .load(event.imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background)
                .into(ivEventImage)

            // Submit tickets
            ticketAdapter.submitList(event.tickets)
        }
    }

    private fun showWarningIfNeeded(remainingCapacity: Int, totalCapacity: Int) {
        if (totalCapacity == 0) {
            binding.cardWarning.visibility = View.GONE
            return
        }

        val percentageRemaining = (remainingCapacity.toDouble() / totalCapacity.toDouble()) * 100
        if (percentageRemaining <= 10.0 && remainingCapacity > 0) {
            binding.cardWarning.visibility = View.VISIBLE
            binding.tvWarning.text = "Almost sold out! Only $remainingCapacity spots remaining."
        } else {
            binding.cardWarning.visibility = View.GONE
        }
    }

    // ✅ Handle Book Now button
    private fun handleBookNow() {
        val selectedTickets = eventDetailViewModel.selectedTickets.value
            ?.filter { it.selectedQuantity > 0 }
            ?: emptyList()

        if (selectedTickets.isEmpty()) {
            Toast.makeText(this, "Please select at least one ticket", Toast.LENGTH_SHORT).show()
            return
        }

        // Validate max 10 tickets
        val totalTickets = eventDetailViewModel.totalTickets.value ?: 0
        if (totalTickets > 10) {
            Toast.makeText(
                this,
                "Maximum 10 tickets per booking",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Convert to CreateBookingRequest.TicketOrder
        val ticketOrders = selectedTickets.map { selection ->
            CreateBookingRequest.TicketOrder(
                ticketId = selection.ticket.ticketId,
                quantity = selection.selectedQuantity
            )
        }

        // Show confirmation dialog
        showBookingConfirmationDialog(ticketOrders, totalTickets)
    }

    private fun showBookingConfirmationDialog(
        ticketOrders: List<CreateBookingRequest.TicketOrder>,
        totalTickets: Int
    ) {
        val totalPrice = eventDetailViewModel.totalPrice.value ?: 0.0
        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            .format(totalPrice)

        AlertDialog.Builder(this)
            .setTitle("Confirm Booking")
            .setMessage(
                "You are about to book $totalTickets ticket(s)\n\n" +
                        "Total: $formattedPrice\n\n" +
                        "Proceed to booking?"
            )
            .setPositiveButton("Book") { dialog, _ ->
                // Create booking via API
                bookingViewModel.createBooking(eventId, ticketOrders)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun navigateToMyBookings() {
        // Navigate to HomeActivity with MyTicket tab selected
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("NAVIGATE_TO_MY_BOOKINGS", true)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Booking Failed")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun showBookingLoading(isLoading: Boolean) {
        binding.apply {
            if (isLoading) {
                btnBookNow.isEnabled = false
                btnBookNow.text = "Processing..."
                progressBar.visibility = View.VISIBLE
            } else {
                progressBar.visibility = View.GONE
                // Button state will be updated by totalTickets observer
            }
        }
    }
}
