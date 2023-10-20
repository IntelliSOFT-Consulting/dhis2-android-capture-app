package com.nacare.capture.adapters

import android.content.Context
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.google.android.material.card.MaterialCardView
import com.nacare.capture.data.service.SyncStatusHelper
import com.nacare.capture.models.ProgramCategory
import org.hisp.dhis.android.core.enrollment.Enrollment
import org.hisp.dhis.android.core.event.Event
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class EventAdapter(
    private val context: Context,
    private val eventList: List<Enrollment>,
    private val click: (Enrollment) -> Unit,
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
        private val eventCard: MaterialCardView = itemView.findViewById(R.id.eventCard)

        fun bind(event: Enrollment) {
            val formattedDate = formatEventDate(event.enrollmentDate())
            Log.e("TAG", "Enrollment $event")
            eventDate.text = formattedDate
            val org = SyncStatusHelper().getOrganizationByUuid(event.organisationUnit())
            organisationUnit.text = org.name()
            if (event.syncState()!!.name == "SYNCED") {
                eventStatus.setImageResource(R.drawable.completeddoc)
                eventStatus.setColorFilter(
                    ContextCompat.getColor(context, R.color.green), PorterDuff.Mode.SRC_IN
                )
            }
            eventCard.apply {
                setOnClickListener {
                    click(event)
                }
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