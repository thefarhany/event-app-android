package com.thefarhany.eventapp.ui.tickets

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.thefarhany.eventapp.databinding.FragmentMyTicketBinding
import com.thefarhany.eventapp.ui.booking.BookingViewModel
import com.thefarhany.eventapp.utils.Resource

class MyTicketFragment : Fragment() {

    private var _binding: FragmentMyTicketBinding? = null
    private val binding get() = _binding!!

    // ✅ ViewModel
    private val bookingViewModel: BookingViewModel by viewModels {
        BookingViewModelFactory(requireActivity().application)
    }

    private lateinit var bookingAdapter: BookingAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyTicketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
    }

    override fun onResume() {
        super.onResume()
        // Reload bookings every time fragment is visible
        loadBookings()
    }

    private fun setupRecyclerView() {
        bookingAdapter = BookingAdapter(
            onPayClick = { booking ->
                handlePayClick(booking.bookingId)
            },
            onCancelClick = { booking ->
                handleCancelClick(booking.bookingId)
            },
            onCardClick = { booking ->
                handleCardClick(booking.bookingId)
            }
        )

        binding.rvBookings.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = bookingAdapter
        }
    }

    private fun setupObservers() {
        // Observe my bookings list
        bookingViewModel.myBookings.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    resource.data?.let { bookings ->
                        if (bookings.isEmpty()) {
                            showEmptyState(true)
                            binding.tvBookingCount.text = "You have 0 bookings"
                        } else {
                            showEmptyState(false)
                            bookingAdapter.submitList(bookings)
                            binding.tvBookingCount.text = "You have ${bookings.size} booking(s)"
                        }
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    showEmptyState(true)
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }

        // Observe cancel booking result
        bookingViewModel.cancelBooking.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Success -> {
                    bookingViewModel.resetCancelBookingState()
                    Toast.makeText(context, "Booking cancelled successfully", Toast.LENGTH_SHORT).show()
                    loadBookings() // Refresh list
                }
                is Resource.Error -> {
                    bookingViewModel.resetCancelBookingState()
                    Toast.makeText(context, resource.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    private fun loadBookings() {
        bookingViewModel.loadMyBookings()
    }

    private fun handlePayClick(bookingId: Long) {
        // Navigate to BookingConfirmationActivity
        val intent = Intent(requireContext(), BookingConfirmationActivity::class.java)
        intent.putExtra("BOOKING_ID", bookingId)
        startActivity(intent)
    }

    private fun handleCancelClick(bookingId: Long) {
        // Show confirmation dialog
        AlertDialog.Builder(requireContext())
            .setTitle("Cancel Booking")
            .setMessage("Are you sure you want to cancel this booking?")
            .setPositiveButton("Yes, Cancel") { dialog, _ ->
                bookingViewModel.cancelBooking(bookingId)
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun handleCardClick(bookingId: Long) {
        // Navigate to booking detail
        val intent = Intent(requireContext(), BookingConfirmationActivity::class.java)
        intent.putExtra("BOOKING_ID", bookingId)
        startActivity(intent)
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            rvBookings.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun showEmptyState(isEmpty: Boolean) {
        binding.apply {
            layoutEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            rvBookings.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
