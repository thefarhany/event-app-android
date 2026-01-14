package com.thefarhany.eventapp.ui.tickets

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.thefarhany.eventapp.R
import com.thefarhany.eventapp.data.model.BookingItem
import java.text.NumberFormat
import java.util.Locale

class TicketItemAdapter : ListAdapter<BookingItem, TicketItemAdapter.TicketItemViewHolder>(TicketItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ticket_booking, parent, false)
        return TicketItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class TicketItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTicketName: TextView = itemView.findViewById(R.id.tvTicketName)
        private val tvTicketQuantity: TextView = itemView.findViewById(R.id.tvTicketQuantity)
        private val tvTicketSubtotal: TextView = itemView.findViewById(R.id.tvTicketSubtotal)

        @SuppressLint("SetTextI18n")
        fun bind(item: BookingItem) {
            val numberFormat = NumberFormat.getCurrencyInstance(Locale("id", "ID"))

            tvTicketName.text = item.ticketName

            val priceFormatted = numberFormat.format(item.price)
            tvTicketQuantity.text = "${item.quantity}x $priceFormatted"

            tvTicketSubtotal.text = numberFormat.format(item.subtotal)
        }
    }

    class TicketItemDiffCallback : DiffUtil.ItemCallback<BookingItem>() {
        override fun areItemsTheSame(oldItem: BookingItem, newItem: BookingItem): Boolean {
            return oldItem.ticketId == newItem.ticketId
        }

        override fun areContentsTheSame(oldItem: BookingItem, newItem: BookingItem): Boolean {
            return oldItem == newItem
        }
    }
}