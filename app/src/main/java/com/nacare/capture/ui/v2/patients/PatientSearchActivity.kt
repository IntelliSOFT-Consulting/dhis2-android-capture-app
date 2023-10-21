package com.nacare.capture.ui.v2.patients

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.android.androidskeletonapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nacare.capture.data.service.SyncStatusHelper
import com.nacare.capture.models.CodeValue
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance


class PatientSearchActivity : AppCompatActivity() {
    private val collectedInputs = mutableListOf<CodeValue>()
    private val inputFieldMap = mutableMapOf<String, View>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_search)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val lnParent = findViewById<LinearLayout>(R.id.ln_parent)
        findViewById<TextView>(R.id.tv_title).apply {
            text = "Patient Search"
        }
        findViewById<MaterialButton>(R.id.next_button).apply {
            setOnClickListener {
                performPatientSearch()
            }
        }
        val trackedEntity = SyncStatusHelper.trackedEntityAttributes()
        val params = listOf(
            "R1vaUuILrDy",
            "eFbT7iTnljR",
            "AP13g7NcBOf",
            "hn8hJsBAKrh",
            "hzVijy6tEUF",
            "oob3a4JM7H6",
            "eFbT7iTnljR"
        )
        trackedEntity.filter { it.uid() in params }.forEach {
            Log.e("Tracked", "Tracked Entity $it")
            when (it.valueType()) {


                ValueType.valueOf("TEXT") -> {
                    if (it.optionSet() == null) {
                        val itemView = layoutInflater.inflate(
                            R.layout.item_edittext,
                            lnParent,
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
                        inputFieldMap[it.uid()] = editText
                        lnParent.addView(itemView)
                    }
                }

                else -> {}
            }

        }

    }

    private fun performPatientSearch() {
        collectedInputs.clear()
        for ((id, view) in inputFieldMap) {
            when (view) {
                is TextInputEditText -> {
                    val input = view.text.toString()
                    if (input.isNotEmpty()) {
                        val dt = CodeValue(
                            attributeUid = id,
                            value = input
                        )
                        collectedInputs.add(dt)
                    }
                }
                // Handle other view types if needed
            }
        }
        if (collectedInputs.size > 0) {

            val patients = SyncStatusHelper.trackedEntityInstanceList()
            if (patients.isNotEmpty()) {
                val filteredPatients = mutableListOf<TrackedEntityInstance>()
                patients.forEach { patient ->

                    val attributes = patient.trackedEntityAttributeValues()
                    Log.e("TAG", "Patients Found Attributes $attributes")
                    attributes?.forEach { attributeValue ->
                        val entity = attributeValue.trackedEntityAttribute()
                        val value = attributeValue.value()

                        // Replace "yourFilterKey" with the actual filter key you want to use
                        val collectedInput = collectedInputs.find { it.attributeUid == entity }
                        if (value != null) {
                            if (collectedInput != null && value.contains(collectedInput.value)) {

                                filteredPatients.add(patient)
                            }
                        }
                    }
                }

                if (filteredPatients.isNotEmpty()) {
                    val intent = Intent(
                        this@PatientSearchActivity,
                        PatientListActivity::class.java
                    )
                    intent.putParcelableArrayListExtra(
                        "collectedInputs",
                        ArrayList(collectedInputs)
                    )
                    startActivity(intent)
                    this@PatientSearchActivity.finish()
                } else {
                    showNoPatientFound()
                }

            } else {
                showNoPatientFound()
            }

        } else {
            Toast.makeText(
                this@PatientSearchActivity, "Please provide an input", Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun showNoPatientFound() {
        val dialog: AlertDialog
        val dialogBuilder = AlertDialog.Builder(this@PatientSearchActivity)
        val dialogView = layoutInflater.inflate(R.layout.confirmation_dialog, null)
        dialogBuilder.setView(dialogView)

        val tvTitle: TextView = dialogView.findViewById(R.id.tv_title)
        val tvMessage: TextView = dialogView.findViewById(R.id.tv_message)
        val nextButton: MaterialButton = dialogView.findViewById(R.id.next_button)
        dialog = dialogBuilder.create()
        tvMessage.text = "No Record found of Patient Searched with those parameters"
        nextButton.text = "Register New Patient"
        nextButton.setOnClickListener {

            startActivity(
                Intent(
                    this@PatientSearchActivity,
                    PatientRegistrationActivity::class.java
                )
            )
            this@PatientSearchActivity.finish()
        }
        dialog.show()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Handle the back press (e.g., finish the activity)
                onBackPressed()
                return true
            }
            // Add other menu item handling if needed
        }
        return super.onOptionsItemSelected(item)
    }
}