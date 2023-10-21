package com.nacare.capture.ui.v2.patients

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.android.androidskeletonapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.data.service.SyncStatusHelper
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

class PatientRegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_registration)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val lnParent = findViewById<LinearLayout>(R.id.ln_parent)
        findViewById<TextView>(R.id.tv_title).apply {
            text = "Patient Details"
        }
        loadCurrentEnrollment(lnParent)
    }

    private fun loadCurrentEnrollment(lnParent: LinearLayout) {
        val enroll = FormatterClass().getSharedPref(
            "enrollment_id",
            this@PatientRegistrationActivity
        )
        if (enroll != null) {
            val dataUser = SyncStatusHelper.getSingleEnrollment(enroll)
            Log.e("TAG", "Enrollment Found $dataUser")
            val user = SyncStatusHelper.getTrackedEntity(dataUser.trackedEntityInstance())
            if (user!=null){
                Log.e("TAG", "Enrollment Found Tracked $user")
               populateViews(lnParent,user)
            }
        }
    }

    private fun populateViews(lnParent: LinearLayout, user: TrackedEntityInstance) {
        val trackedEntity = SyncStatusHelper.trackedEntityAttributes()
        trackedEntity.forEach {
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
                        lnParent.addView(itemView)
                    }
                }

                else -> {}
            }
        }
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