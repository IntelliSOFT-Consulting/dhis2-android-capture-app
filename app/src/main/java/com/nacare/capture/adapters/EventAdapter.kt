package com.nacare.capture.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.nacare.capture.data.service.SyncStatusHelper
import org.hisp.dhis.android.core.event.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class EventAdapter(
    private val context: Context,
    private val eventList: List<Event>
) :
    RecyclerView.Adapter<EventAdapter.EventViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_event, parent, false)
        return EventViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = eventList[position]
        holder.bind(event)
    }

    override fun getItemCount(): Int {
        return eventList.size
    }

    inner class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val eventDate: TextView = itemView.findViewById(R.id.event_date)
        private val organisationUnit: TextView = itemView.findViewById(R.id.organisationUnit)
        private val eventStatus: ImageView = itemView.findViewById(R.id.eventStatus)

        fun bind(event: Event) {
            val formattedDate = formatEventDate(event.eventDate())
            eventDate.text = formattedDate
            val org = SyncStatusHelper().getOrganizationByUuid(event.organisationUnit())
            organisationUnit.text = org.name()
            if (event.status()?.name == "COMPLETED") {
                eventStatus.setImageResource(R.drawable.completeddoc)
                eventStatus.setColorFilter(
                    ContextCompat.getColor(context, R.color.green), PorterDuff.Mode.SRC_IN
                )
            }
        }

        private fun formatEventDate(date: Date?): String? {
            return if (date != null) {
                val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                simpleDateFormat.format(date)
            } else {
                date
            }
        }
    }
}