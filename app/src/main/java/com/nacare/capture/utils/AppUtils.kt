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
            .setMessage("There are not organization units, pleas try again later!!")
            .setPositiveButton("Okay") { dialog, _ ->
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()

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

    fun  isOnline(context: Context): Boolean {
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
//                    val selectedDate =                        String.format("%02d/%02d/%04d", dayOfMonth, monthOfYear + 1, year)
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

}