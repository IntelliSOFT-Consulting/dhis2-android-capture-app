package com.nacare.capture.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.models.ProgramCategory
import com.google.android.material.card.MaterialCardView
import com.nacare.capture.utils.AppUtils


class ProgramAdapter(
    private val context: Context,
    private val dataList: List<ProgramCategory>,
    private val click: (ProgramCategory) -> Unit,
) : RecyclerView.Adapter<ProgramAdapter.ProgramHolder>() {

    inner class ProgramHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val formatterClass = FormatterClass()
        val progressTextView: TextView = itemView.findViewById(R.id.progressTextView)
        val eventTextView: TextView = itemView.findViewById(R.id.eventTextView)
        val leftIconImageView: ImageView = itemView.findViewById(R.id.leftIconImageView)
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
            LayoutInflater.from(parent.context).inflate(R.layout.item_program, parent, false)
        return ProgramHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProgramHolder, position: Int) {
        val item = dataList[position]
        holder.eventTextView.text = item.name
        holder.progressTextView.text = " ${item.done}/${item.total}"
        try {
            val drawableResourceId = AppUtils().generateIcons(context, item.name)
            holder.leftIconImageView.setImageResource(drawableResourceId)
        } catch (e: Exception) {

        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}