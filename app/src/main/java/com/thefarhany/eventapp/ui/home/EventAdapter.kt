package com.thefarhany.eventapp.ui.home

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.thefarhany.eventapp.R
import com.thefarhany.eventapp.data.model.Event
import com.thefarhany.eventapp.databinding.ItemEventBinding
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class EventAdapter(
    private val onItemClick: (Event) -> Unit
) : ListAdapter<Event, EventAdapter.EventViewHolder>(EventDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return EventViewHolder(binding, onItemClick)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class EventViewHolder(
        private val binding: ItemEventBinding,
        private val onItemClick: (Event) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(event: Event) {
            binding.apply {
                // Event Title
                tvEventTitle.text = event.title

                // Event Description/Summary
                tvEventDescription.text = event.shortSummary
                    ?: "Tap to see more details"

                // Location
                val locationText = "${event.venue}, ${event.city}"
                tvLocation.text = locationText

                // Date & Time
                val dateText = formatDateTime(event.date, event.time)
                tvDate.text = dateText

                // Spots Left (remaining capacity)
                val spotsText = "${ event.remainingCapacity} spots left"
                tvSpotsLeft.text = spotsText

                // Price
                tvPrice.text = formatPrice(event.price.toDouble())

                // Category Badge
                tvBadge.text = event.category
                badgeOnsite.setCardBackgroundColor(
                    getCategoryColor(event.category)
                )

                // Load Image with Glide
                if (!event.imageUrl.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(event.imageUrl)
                        .placeholder(R.color.gray_200)
                        .error(R.color.gray_200)
                        .centerCrop()
                        .into(ivEventImage)
                } else {
                    // Default placeholder
                    ivEventImage.setBackgroundColor(
                        itemView.context.getColor(R.color.gray_200)
                    )
                }

                // Click Listeners
                root.setOnClickListener {
                    onItemClick(event)
                }

                btnViewDetails.setOnClickListener {
                    onItemClick(event)
                }
            }
        }

        @RequiresApi(Build.VERSION_CODES.O)
        private fun formatDateTime(dateString: String, timeString: String): String {
            return try {
                val localDate = LocalDate.parse(dateString)
                val localTime = LocalTime.parse(timeString)

                val dateFormatter = DateTimeFormatter.ofPattern("MMMM dd, yyyy", Locale.ENGLISH)
                val timeFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH)

                "${localDate.format(dateFormatter)} at ${localTime.format(timeFormatter)}"
            } catch (e: Exception) {
                "$dateString at $timeString"
            }
        }

        private fun formatPrice(price: Double): String {
            return if (price == 0.0) {
                "FREE"
            } else {
                val formatter = NumberFormat.getCurrencyInstance(Locale.US)
                formatter.format(price)
            }
        }

        private fun getCategoryColor(category: String): Int {
            return when (category.uppercase()) {
                "TECHNOLOGY" -> itemView.context.getColor(R.color.primary)
                "MUSIC" -> itemView.context.getColor(R.color.success_green)
                "WORKSHOP" -> itemView.context.getColor(R.color.orange)
                "SPORTS" -> itemView.context.getColor(R.color.blue)
                "ART" -> itemView.context.getColor(R.color.purple)
                else -> itemView.context.getColor(R.color.gray_500)
            }
        }
    }

    class EventDiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem.eventId == newItem.eventId
        }

        override fun areContentsTheSame(oldItem: Event, newItem: Event): Boolean {
            return oldItem == newItem
        }
    }
}