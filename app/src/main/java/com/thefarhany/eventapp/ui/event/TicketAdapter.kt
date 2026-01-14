package com.thefarhany.eventapp.ui.event

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.thefarhany.eventapp.R
import com.thefarhany.eventapp.data.model.CreateBookingRequest
import com.thefarhany.eventapp.data.model.TicketResponse
import com.thefarhany.eventapp.data.model.TicketSelection
import java.text.NumberFormat
import java.util.Locale

class TicketAdapter(
    private val onTicketQuantityChanged: (List<TicketSelection>) -> Unit
) : RecyclerView.Adapter<TicketAdapter.TicketViewHolder>() {

    private var tickets = listOf<TicketSelection>()
    private val MAX_TOTAL_TICKETS = 10

    fun submitList(newTickets: List<TicketResponse>) {
        tickets = newTickets.map { TicketSelection(it, 0) }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TicketViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ticket, parent, false)
        return TicketViewHolder(view)
    }

    override fun onBindViewHolder(holder: TicketViewHolder, position: Int) {
        holder.bind(tickets[position])
    }

    override fun getItemCount() = tickets.size

    // ✅ Get total selected tickets across all ticket types
    fun getTotalSelectedTickets(): Int {
        return tickets.sumOf { it.selectedQuantity }
    }

    // ✅ Get selected tickets for booking API
    fun getSelectedTickets(): List<CreateBookingRequest.TicketOrder> {
        return tickets
            .filter { it.selectedQuantity > 0 }
            .map { selection ->
                CreateBookingRequest.TicketOrder(
                    ticketId = selection.ticket.ticketId,
                    quantity = selection.selectedQuantity
                )
            }
    }

    inner class TicketViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardTicket: MaterialCardView = itemView.findViewById(R.id.cardTicket)
        private val tvTicketName: TextView = itemView.findViewById(R.id.tvTicketName)
        private val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        private val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        private val tvCounter: TextView = itemView.findViewById(R.id.tvCounter)
        private val btnMinus: MaterialButton = itemView.findViewById(R.id.btnMinus)
        private val btnPlus: MaterialButton = itemView.findViewById(R.id.btnPlus)

        fun bind(ticketSelection: TicketSelection) {
            val ticket = ticketSelection.ticket

            tvTicketName.text = ticket.ticketName
            tvQuantity.text = "${ticket.quantity} tickets available"
            tvPrice.text = "Rp ${NumberFormat.getNumberInstance(Locale("id", "ID")).format(ticket.price)}"
            tvCounter.text = ticketSelection.selectedQuantity.toString()

            // Update card style based on selection
            updateCardStyle(ticketSelection.selectedQuantity > 0)

            // Update button states
            updateButtonStates(ticketSelection)

            // Minus button click
            btnMinus.setOnClickListener {
                if (ticketSelection.selectedQuantity > 0) {
                    ticketSelection.selectedQuantity--
                    notifyItemChanged(adapterPosition)
                    onTicketQuantityChanged(tickets)
                }
            }

            // Plus button click
            btnPlus.setOnClickListener {
                val totalSelected = getTotalSelectedTickets()
                when {
                    totalSelected >= MAX_TOTAL_TICKETS -> {
                        // Already at max limit (10 tickets)
                        return@setOnClickListener
                    }
                    ticketSelection.selectedQuantity >= ticket.quantity -> {
                        // No more tickets available
                        return@setOnClickListener
                    }
                    else -> {
                        ticketSelection.selectedQuantity++
                        notifyItemChanged(adapterPosition)
                        onTicketQuantityChanged(tickets)
                    }
                }
            }
        }

        private fun updateCardStyle(isSelected: Boolean) {
            val strokeColor = if (isSelected) {
                ContextCompat.getColor(itemView.context, R.color.primary)
            } else {
                ContextCompat.getColor(itemView.context, R.color.pink)
            }

            cardTicket.strokeColor = strokeColor
            cardTicket.strokeWidth = if (isSelected) 4 else 2
        }

        private fun updateButtonStates(ticketSelection: TicketSelection) {
            val totalSelected = getTotalSelectedTickets()

            // Minus button state
            btnMinus.isEnabled = ticketSelection.selectedQuantity > 0
            btnMinus.backgroundTintList = if (btnMinus.isEnabled) {
                ContextCompat.getColorStateList(itemView.context, R.color.primary)
            } else {
                ContextCompat.getColorStateList(itemView.context, R.color.pink)
            }

            // Plus button state
            val canAddMore = totalSelected < MAX_TOTAL_TICKETS &&
                    ticketSelection.selectedQuantity < ticketSelection.ticket.quantity
            btnPlus.isEnabled = canAddMore
            btnPlus.backgroundTintList = if (btnPlus.isEnabled) {
                ContextCompat.getColorStateList(itemView.context, R.color.primary)
            } else {
                ContextCompat.getColorStateList(itemView.context, R.color.pink)
            }
        }
    }
}
