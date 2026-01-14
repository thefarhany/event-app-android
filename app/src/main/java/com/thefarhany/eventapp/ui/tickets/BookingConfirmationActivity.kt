package com.thefarhany.eventapp.ui.tickets

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.thefarhany.eventapp.R
import com.thefarhany.eventapp.data.model.BookingDetail
import com.thefarhany.eventapp.databinding.ActivityBookingConfirmationBinding
import com.thefarhany.eventapp.ui.booking.BookingViewModel
import com.thefarhany.eventapp.utils.Resource
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class BookingConfirmationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookingConfirmationBinding

    private val bookingViewModel: BookingViewModel by viewModels {
        BookingViewModelFactory(application)
    }

    private lateinit var ticketItemAdapter: TicketItemAdapter
    private var bookingId: Long = -1L
    private var currentBookingDetail: BookingDetail? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookingConfirmationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bookingId = intent.getLongExtra("BOOKING_ID", -1L)
        if (bookingId == -1L) {
            Toast.makeText(this, "Invalid booking ID", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupToolbar()
        setupRecyclerView()
        setupObservers()
        setupListeners()

        bookingViewModel.loadBookingDetail(bookingId)
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        ticketItemAdapter = TicketItemAdapter()
        binding.rvTicketItems.apply {
            layoutManager = LinearLayoutManager(this@BookingConfirmationActivity)
            adapter = ticketItemAdapter
        }
    }

    private fun setupObservers() {
        bookingViewModel.bookingDetail.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showLoading(true)
                }
                is Resource.Success -> {
                    showLoading(false)
                    resource.data?.let { booking ->
                        currentBookingDetail = booking
                        displayBookingDetail(booking)
                    }
                }
                is Resource.Error -> {
                    showLoading(false)
                    showDetailedErrorDialog(
                        title = "Failed to Load Booking",
                        message = resource.message ?: "Failed to load booking detail",
                        shouldFinish = true
                    )
                }
                else -> {}
            }
        }

        // Observe pay booking result
        bookingViewModel.payBooking.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    showPaymentLoading(true)
                }
                is Resource.Success -> {
                    showPaymentLoading(false)
                    bookingViewModel.resetPayBookingState()

                    Toast.makeText(
                        this,
                        "Payment successful!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Reload booking detail to get QR code
                    bookingViewModel.loadBookingDetail(bookingId)
                }
                is Resource.Error -> {
                    showPaymentLoading(false)
                    bookingViewModel.resetPayBookingState()

                    // ✅ Show detailed payment error
                    showDetailedErrorDialog(
                        title = "Payment Failed",
                        message = getPaymentErrorMessage(resource.message),
                        shouldFinish = false
                    )
                }
                else -> {}
            }
        }
    }

    private fun setupListeners() {
        binding.btnPay.setOnClickListener {
            // ✅ Validate before payment
            if (validateBeforePayment()) {
                showPaymentConfirmationDialog()
            }
        }
    }

    // ✅ Validate booking before payment
    private fun validateBeforePayment(): Boolean {
        val booking = currentBookingDetail ?: return false

        // Check if booking is still PENDING
        if (booking.status != "PENDING") {
            showDetailedErrorDialog(
                title = "Cannot Process Payment",
                message = "This booking cannot be paid. Status: ${booking.status}",
                shouldFinish = false
            )
            return false
        }

        // Check if total tickets is valid (1-10)
        val totalTickets = booking.items.sumOf { it.quantity }
        if (totalTickets < 1) {
            showDetailedErrorDialog(
                title = "Invalid Booking",
                message = "No tickets found in this booking",
                shouldFinish = false
            )
            return false
        }

        if (totalTickets > 10) {
            showDetailedErrorDialog(
                title = "Invalid Booking",
                message = "Maximum 10 tickets per transaction. This booking has $totalTickets tickets.",
                shouldFinish = false
            )
            return false
        }

        return true
    }

    // ✅ Get user-friendly payment error message
    private fun getPaymentErrorMessage(errorMessage: String?): String {
        return when {
            errorMessage == null -> "Payment failed. Please try again."

            // Backend error messages mapping
            errorMessage.contains("already paid", ignoreCase = true) ->
                "This booking has already been paid."

            errorMessage.contains("cancelled", ignoreCase = true) ->
                "This booking has been cancelled and cannot be paid."

            errorMessage.contains("not found", ignoreCase = true) ->
                "Booking not found. It may have been deleted."

            errorMessage.contains("access", ignoreCase = true) ->
                "You don't have permission to pay this booking."

            errorMessage.contains("sold out", ignoreCase = true) ->
                "Tickets are sold out. This event is no longer available."

            errorMessage.contains("quota", ignoreCase = true) ||
                    errorMessage.contains("stock", ignoreCase = true) ->
                "Requested tickets exceed available quota. Please create a new booking with fewer tickets."

            errorMessage.contains("connection", ignoreCase = true) ||
                    errorMessage.contains("network", ignoreCase = true) ->
                "Connection failed. Please check your internet connection and try again."

            else -> errorMessage
        }
    }

    private fun displayBookingDetail(booking: BookingDetail) {
        binding.apply {
            layoutContent.visibility = View.VISIBLE

            tvEventTitle.text = booking.eventTitle
            tvBookingId.text = "Booking ID: #${booking.bookingId}"

            ticketItemAdapter.submitList(booking.items)

            val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                .format(booking.totalPrice)
            tvTotalPrice.text = formattedPrice

            when (booking.status) {
                "PENDING" -> setupPendingStatus()
                "PAID" -> setupPaidStatus(booking)
                "CANCELLED" -> setupCancelledStatus()
            }
        }
    }

    private fun setupPendingStatus() {
        binding.apply {
            cardStatus.setCardBackgroundColor(getColor(R.color.white))
            cardStatus.setBackgroundResource(R.drawable.bg_status_pending)
            ivStatusIcon.setColorFilter(getColor(android.R.color.holo_orange_dark))
            tvStatusTitle.text = "Payment Pending"
            tvStatusMessage.text = "Please complete your payment to get your e-ticket"

            btnPay.visibility = View.VISIBLE
            cardQrCode.visibility = View.GONE
        }
    }

    private fun setupPaidStatus(booking: BookingDetail) {
        binding.apply {
            cardStatus.setBackgroundResource(R.drawable.bg_status_paid)
            ivStatusIcon.setColorFilter(getColor(android.R.color.holo_green_dark))
            tvStatusTitle.text = "Payment Successful"
            tvStatusMessage.text = "Your booking is confirmed"

            btnPay.visibility = View.GONE

            if (!booking.qrCode.isNullOrEmpty()) {
                cardQrCode.visibility = View.VISIBLE
                displayQrCode(booking.qrCode)

                booking.paidAt?.let { paidAt ->
                    tvPaidAt.text = "Paid at: ${formatDateTime(paidAt)}"
                }
            }
        }
    }

    private fun setupCancelledStatus() {
        binding.apply {
            cardStatus.setBackgroundResource(R.drawable.bg_status_cancelled)
            ivStatusIcon.setColorFilter(getColor(android.R.color.darker_gray))
            tvStatusTitle.text = "Booking Cancelled"
            tvStatusMessage.text = "This booking has been cancelled"

            btnPay.visibility = View.GONE
            cardQrCode.visibility = View.GONE
        }
    }

    private fun displayQrCode(qrCodeBase64: String) {
        try {
            val decodedBytes = Base64.decode(qrCodeBase64, Base64.DEFAULT)
            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
            binding.ivQrCode.setImageBitmap(bitmap)
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to display QR code", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPaymentConfirmationDialog() {
        val totalPrice = currentBookingDetail?.totalPrice
        val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            .format(totalPrice)

        AlertDialog.Builder(this)
            .setTitle("Confirm Payment")
            .setMessage(
                "You are about to pay $formattedPrice\n\n" +
                        "Proceed with payment?"
            )
            .setPositiveButton("Pay") { dialog, _ ->
                bookingViewModel.payBooking(bookingId)
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    // ✅ Improved error dialog with detailed messages
    private fun showDetailedErrorDialog(
        title: String,
        message: String,
        shouldFinish: Boolean
    ) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                if (shouldFinish) {
                    finish()
                }
            }
            .setCancelable(false)
            .show()
    }

    private fun showLoading(isLoading: Boolean) {
        binding.apply {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            layoutContent.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun showPaymentLoading(isLoading: Boolean) {
        binding.btnPay.apply {
            isEnabled = !isLoading
            text = if (isLoading) "Processing..." else "Pay Now"
        }
    }

    private fun formatDateTime(dateString: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            dateString
        }
    }
}
