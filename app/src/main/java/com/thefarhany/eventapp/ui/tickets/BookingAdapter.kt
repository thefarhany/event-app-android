package com.thefarhany.eventapp.ui.tickets

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.thefarhany.eventapp.R
import com.thefarhany.eventapp.data.model.Booking
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class BookingAdapter(
    private val onPayClick: (Booking) -> Unit,
    private val onCancelClick: (Booking) -> Unit,
    private val onCardClick: (Booking) -> Unit
) : ListAdapter<Booking, BookingAdapter.BookingViewHolder>(BookingDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_booking_card, parent, false)
        return BookingViewHolder(view)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardBooking: MaterialCardView = itemView.findViewById(R.id.cardBooking)
        private val tvEventTitle: TextView = itemView.findViewById(R.id.tvEventTitle)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
        private val tvBookingId: TextView = itemView.findViewById(R.id.tvBookingId)
        private val tvTotalTickets: TextView = itemView.findViewById(R.id.tvTotalTickets)
        private val tvTotalPrice: TextView = itemView.findViewById(R.id.tvTotalPrice)
        private val tvCreatedAt: TextView = itemView.findViewById(R.id.tvCreatedAt)
        private val btnPay: MaterialButton = itemView.findViewById(R.id.btnPay)
        private val btnCancel: MaterialButton = itemView.findViewById(R.id.btnCancel)

        fun bind(booking: Booking) {
            tvEventTitle.text = booking.eventTitle
            tvBookingId.text = "#${booking.bookingId}"

            // Total tickets
            val totalTickets = booking.items.sumOf { it.quantity }
            tvTotalTickets.text = "$totalTickets ticket(s)"

            // Total price
            val formattedPrice = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                .format(booking.totalPrice)
            tvTotalPrice.text = formattedPrice

            // Created at
            tvCreatedAt.text = formatDate(booking.createdAt)

            // Status badge
            setupStatusBadge(booking.status)

            // Button visibility based on status
            setupButtons(booking)

            // Card click
            cardBooking.setOnClickListener {
                onCardClick(booking)
            }
        }

        private fun setupStatusBadge(status: String) {
            tvStatus.text = status

            val backgroundRes = when (status) {
                "PENDING" -> R.drawable.bg_status_pending
                "PAID" -> R.drawable.bg_status_paid
                "CANCELLED" -> R.drawable.bg_status_cancelled
                else -> R.drawable.bg_status_pending
            }

            tvStatus.setBackgroundResource(backgroundRes)
        }

        private fun setupButtons(booking: Booking) {
            when (booking.status) {
                "PENDING" -> {
                    // Show both buttons
                    btnPay.visibility = View.VISIBLE
                    btnCancel.visibility = View.VISIBLE

                    btnPay.setOnClickListener { onPayClick(booking) }
                    btnCancel.setOnClickListener { onCancelClick(booking) }
                }
                "PAID" -> {
                    // Hide both buttons (already paid)
                    btnPay.visibility = View.GONE
                    btnCancel.visibility = View.GONE
                }
                "CANCELLED" -> {
                    // Hide both buttons (cancelled)
                    btnPay.visibility = View.GONE
                    btnCancel.visibility = View.GONE
                }
            }
        }

        private fun formatDate(dateString: String): String {
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

    class BookingDiffCallback : DiffUtil.ItemCallback<Booking>() {
        override fun areItemsTheSame(oldItem: Booking, newItem: Booking): Boolean {
            return oldItem.bookingId == newItem.bookingId
        }

        override fun areContentsTheSame(oldItem: Booking, newItem: Booking): Boolean {
            return oldItem == newItem
        }
    }
}
