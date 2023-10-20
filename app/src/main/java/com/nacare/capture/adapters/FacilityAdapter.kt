package com.nacare.capture.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.models.FacilityProgramCategory
import org.hisp.dhis.android.core.common.ValueType

class FacilityAdapter(
    private val context: Context,
    private val dataList: List<FacilityProgramCategory>,
    private val inflater: LayoutInflater
) : RecyclerView.Adapter<FacilityAdapter.ProgramHolder>() {

    inner class ProgramHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        val formatterClass = FormatterClass()
        val textView: TextView = itemView.findViewById(R.id.textView)
        val lnParent: LinearLayout = itemView.findViewById(R.id.ln_parent)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProgramHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.item_facility, parent, false)
        return ProgramHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProgramHolder, position: Int) {
        val item = dataList[position]
        try {
            holder.textView.text = item.displayName

            if (item.dataElements?.isNotEmpty() == true) {
                item.dataElements.forEach {
                    when (it.valueType()) {
                        ValueType.valueOf("BOOLEAN") -> {
                            val itemView = inflater.inflate(
                                R.layout.item_radio,
                                holder.lnParent,
                                false
                            ) as LinearLayout

                            val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                            val radioButtonYes = itemView.findViewById<RadioButton>(R.id.radioButtonYes)
                            val radioButtonNo = itemView.findViewById<RadioButton>(R.id.radioButtonNo)
                            tvName.text = it.displayName()
                            holder.lnParent.addView(itemView)
                        }
                        ValueType.valueOf("TEXT") -> {
                            if (it.optionSet() == null) {
                                val itemView = inflater.inflate(
                                    R.layout.item_edittext,
                                    holder.lnParent,
                                    false
                                ) as LinearLayout

                                val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                                val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                                val textInputLayout =
                                    itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                                val editText =
                                    itemView.findViewById<TextInputEditText>(R.id.editText)
                                tvName.text = it.displayName()
                                tvElement.text = it.uid()
                                holder.lnParent.addView(itemView)
                            }
                        }

                        else -> {}
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun getItemCount(): Int {
        return dataList.size
    }
}