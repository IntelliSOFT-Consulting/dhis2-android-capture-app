package com.nacare.capture.adapters

import android.app.Application
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckedTextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.nacare.capture.models.OrganisationUnit
import com.nacare.capture.ui.v2.room.MainViewModel
import com.nacare.capture.ui.v2.room.TrackedEntityValues

class OrganizationAdapter(
    private val context: Context,
    private val countyList: List<OrganisationUnit>,
    private val entityUid: String,
    private val attributeUid: String
) : RecyclerView.Adapter<OrganizationAdapter.ProgramHolder>() {
    private val checkedStates = MutableList(countyList.size) { false }

    inner class ProgramHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val viewModel = MainViewModel(context.applicationContext as Application)
        val checkedTextView: CheckedTextView = itemView.findViewById(R.id.checkedTextView)

        init {
            checkedTextView.setOnClickListener {
                val position = adapterPosition
                // Toggle the checked state
                checkedStates[position] = !checkedStates[position]
                val data = countyList[position].id
                val tracked = TrackedEntityValues(
                    entityUid = entityUid,
                    attributeUid = attributeUid,
                    dataValue = data,
                )
                viewModel.addUpdateTrackedEntity(tracked)

                // Disable all other CheckedTextView items
                for (i in 0 until itemCount) {
                    if (i != position) {
                        checkedStates[i] = false
                    }
                }
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_radio_input, parent, false)
        return ProgramHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProgramHolder, position: Int) {
        val item = countyList[position]
        try {
            holder.checkedTextView.text = item.name
            holder.checkedTextView.isChecked = checkedStates[position]
            holder.checkedTextView.isEnabled = !checkedStates[position]
        } catch (e: Exception) {

        }
    }

    override fun getItemCount(): Int {
        return countyList.size
    }
}