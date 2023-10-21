package com.nacare.capture.ui.v2.filters

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import com.example.android.androidskeletonapp.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class FilterBottomSheetFragment : BottomSheetDialogFragment() {
    private var listener: FilterBottomSheetListener? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_filter, container, false)

        val gridLayout: GridLayout = view.findViewById(R.id.gridLayout)
        val tvDate: TextView = view.findViewById(R.id.tv_date)
        val lnDates: LinearLayout = view.findViewById(R.id.ln_dates)
        val lnNotSynced: LinearLayout = view.findViewById(R.id.ln_not_synced)
        val lnDraft: LinearLayout = view.findViewById(R.id.ln_draft)
        val lnCompleted: LinearLayout = view.findViewById(R.id.ln_completed)
        val lnDuplicate: LinearLayout = view.findViewById(R.id.ln_duplicate)

        for (i in 0 until gridLayout.childCount) {
            val childView = gridLayout.getChildAt(i)

            if (childView is RadioButton) {
                childView.setOnClickListener {
                    // Uncheck all other RadioButtons in the GridLayout
                    for (j in 0 until gridLayout.childCount) {
                        val otherRadioButton = gridLayout.getChildAt(j)
                        if (otherRadioButton is RadioButton && otherRadioButton != childView) {
                            otherRadioButton.isChecked = false
                        }
                    }
                }
            }
        }
        tvDate.setOnClickListener {
            // check if ln_dates is visible, if visible hide else show
            when (lnDates.visibility) {
                View.VISIBLE -> {
                    // If visible, hide it
                    lnDates.visibility = View.GONE
                }

                else -> {
                    // If not visible, show it
                    lnDates.visibility = View.VISIBLE
                }
            }
        }
        lnNotSynced.setOnClickListener {
            listener?.onStatusClicked("not synced")
            dismiss()
        }
        lnDraft.setOnClickListener {
            listener?.onStatusClicked("draft")
            dismiss()
        }
        lnCompleted.setOnClickListener {
            listener?.onStatusClicked("completed")
            dismiss()
        }
        lnDuplicate.setOnClickListener {
            listener?.onStatusClicked("duplicates")
            dismiss()
        }

        return view
    }

    fun setFilterBottomSheetListener(listener: FilterBottomSheetListener) {
        this.listener = listener
    }
}