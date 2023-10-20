package com.nacare.capture.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.google.android.material.card.MaterialCardView
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.models.Person
import com.nacare.capture.models.ProgramCategory


class PatientAdapter(
    private val context: Context,
    private val dataList: List<Person>,
    private val click: (Person) -> Unit,
) : RecyclerView.Adapter<PatientAdapter.ProgramHolder>() {

    inner class ProgramHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val formatterClass = FormatterClass()
        val numberTextView: TextView = itemView.findViewById(R.id.numberTextView)
        val firstNameTextView: TextView = itemView.findViewById(R.id.firstNameTextView)
        val middleNameTextView: TextView = itemView.findViewById(R.id.middleNameTextView)
        val lastNameTextView: TextView = itemView.findViewById(R.id.lastNameTextView)
        val documentTextView: TextView = itemView.findViewById(R.id.documentTextView)
        private val materialCardView: MaterialCardView =
            itemView.findViewById(R.id.materialCardView)

        init {
            materialCardView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            val patient = dataList[adapterPosition]
            click(patient)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_patient, parent, false)
        return ProgramHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProgramHolder, position: Int) {
        val item = dataList[position]
        try {
            holder.numberTextView.text = item.patientId
            holder.firstNameTextView.text = item.firstName
            holder.middleNameTextView.text = item.middleName
            holder.lastNameTextView.text = item.lastName
            holder.documentTextView.text = item.document
        } catch (e: Exception) {

        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}