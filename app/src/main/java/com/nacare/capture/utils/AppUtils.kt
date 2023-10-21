package com.nacare.capture.utils

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.view.inputmethod.InputMethodManager
import android.widget.DatePicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.android.androidskeletonapp.R
import com.google.android.material.textfield.TextInputEditText
import com.nacare.capture.models.CountyUnit
import com.nacare.capture.models.OrgTreeNode
import org.w3c.dom.Text
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class AppUtils {

    fun containsAnyKeyword(displayName: String, keywords: List<String>): Boolean {
        return keywords.any { keyword -> displayName.contains(keyword) }
    }

    fun showNoOrgUnits(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("No Organization Units")
            .setMessage("There are no organization units, pleas try again later!!")
            .setPositiveButton("Okay") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

    }

    fun generateChild(children: List<CountyUnit>): List<OrgTreeNode> {
        val treeNodes = mutableListOf<OrgTreeNode>()
        for (ch in children) {
            val orgNode = OrgTreeNode(
                label = ch.name,
                code = ch.id,
                children = generateChild(ch.children)
            )
            treeNodes.add(orgNode)

        }

        return treeNodes.sortedBy { it.label }
    }

    fun noConnection(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("No Connection")
            .setMessage("You need active internet to perform global search")
            .setPositiveButton("Okay") { dd, _ ->
                dd.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    fun isOnline(context: Context): Boolean {
        var isOnline = false
        try {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            if (cm != null) {
                val netInfo = cm.activeNetworkInfo
                //should check null because in airplane mode it will be null
                isOnline = netInfo != null && netInfo.isConnectedOrConnecting
            }
        } catch (ex: Exception) {

        }
        return isOnline
    }

    fun hideKeyboard(context: Context) {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        val rootView = (context as AppCompatActivity).window.decorView.rootView
        inputMethodManager.hideSoftInputFromWindow(rootView.windowToken, 0)
    }

    fun formatEventDate(date: Date?): String {
        var data = ""
        data = if (date != null) {
            val simpleDateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            simpleDateFormat.format(date)
        } else {
            ""
        }
        return data
    }

    fun disableTextInputEditText(editText: TextInputEditText) {
        editText.isFocusable = false
        editText.isCursorVisible = false
        editText.keyListener = null
    }

    fun generateUuid(): String {
        return UUID.randomUUID().toString()
    }

    fun showDatePickerDialog(
        context: Context,
        textInputEditText: TextInputEditText,
        setMaxNow: Boolean,
        setMinNow: Boolean
    ) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog =
            DatePickerDialog(
                context,
                { _: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int ->
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val selectedDate = dateFormat.format(Date(year - 1900, monthOfYear, dayOfMonth))

                    textInputEditText.setText(selectedDate)
                },
                year,
                month,
                day
            )

        // Set date picker dialog limits (optional)
        if (setMaxNow) {
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis() + (1000 * 60 * 60 * 24)
        }
        if (setMinNow) {
            datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        }

        datePickerDialog.show()
    }

    fun makeBold(view: TextView) {
        view.apply {
            val currentTypeface = typeface
            // Set a new typeface with the bold style
            setTypeface(currentTypeface, Typeface.BOLD)
        }

    }

    fun generateIcons(context: Context, iconName: String): Int {
        val iconDrawableMap = mapOf(
            "add" to R.drawable.add,
            "arrow_down" to R.drawable.arrowdown,
            "back" to R.drawable.back,
            "Cancer Information" to R.drawable.cancerinfo,
            "capture" to R.drawable.capture,
            "cleaner" to R.drawable.cleaner,
            "Comorbidities" to R.drawable.comorbidities,
            "completed_doc" to R.drawable.completeddoc,
            "dashb" to R.drawable.dashb,
            "Discrimination" to R.drawable.discrimination,
            "editdoc" to R.drawable.editdoc,
            "edit_form" to R.drawable.editform,
            "event_note_FILL0_wght400_GRAD0_opsz24" to R.drawable.event_note,
            "event_vis" to R.drawable.eventvis,
            "ev_viz_2" to R.drawable.evviz2,
            "expand" to R.drawable.expand,
            "facility" to R.drawable.facility,
            "facility_2" to R.drawable.facility2,
            "facility_cap" to R.drawable.facilitycap,
            "filter" to R.drawable.filter,
            "follow_up" to R.drawable.followup,
            "follow_upp" to R.drawable.followupp,
            "form" to R.drawable.form,
            "form_complete" to R.drawable.formcomplete,
            "helpdesk" to R.drawable.helpdesk,
            "info" to R.drawable.info,
            "interp" to R.drawable.interp,
            "key" to R.drawable.key,
            "maps" to R.drawable.maps,
            "menu" to R.drawable.menu,
            "menu_management" to R.drawable.menumanagement,
            "more" to R.drawable.more,
            "nci_form" to R.drawable.nciform,
            "next_next" to R.drawable.nextnext,
            "next_page" to R.drawable.nextpage,
            "Patient Details" to R.drawable.patientdetails,
            "Patient Status" to R.drawable.patientstatus,
            "Post-cancer Treatment Rehabilitation" to R.drawable.posttreatment,
            "remove_FILL0_wght400_GRAD0_opsz24" to R.drawable.remove_,
            "risk" to R.drawable.risk,
            "Risk Factors" to R.drawable.risk2,
            "save" to R.drawable.save,
            "search" to R.drawable.search,
            "settings" to R.drawable.settings,
            "star" to R.drawable.star,
            "Survivorship" to R.drawable.survivorship,
            "sync" to R.drawable.sync,
            "synccc" to R.drawable.synccc,
            "Treatment" to R.drawable.treatment
        )

        return iconDrawableMap[iconName]
            ?: throw IllegalArgumentException("Icon not found: $iconName")


    }


}