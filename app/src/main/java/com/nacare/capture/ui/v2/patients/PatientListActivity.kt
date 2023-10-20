package com.nacare.capture.ui.v2.patients

import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.nacare.capture.adapters.EventAdapter
import com.nacare.capture.adapters.PatientAdapter
import com.nacare.capture.data.Sdk
import com.nacare.capture.data.service.SyncStatusHelper
import com.nacare.capture.models.Person
import com.nacare.capture.utils.AppUtils

class PatientListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_list)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        findViewById<TextView>(R.id.tv_title).apply {
            text = "The National Cancer Registry of Kenya Notification Form"
        }

        AppUtils().makeBold(findViewById(R.id.numberTextView))
        AppUtils().makeBold(findViewById(R.id.firstNameTextView))
        AppUtils().makeBold(findViewById(R.id.lastNameTextView))
        AppUtils().makeBold(findViewById(R.id.middleNameTextView))
        AppUtils().makeBold(findViewById(R.id.documentTextView))

        val tracked = SyncStatusHelper.trackedEntityInstanceList()
        if (tracked.isNotEmpty()) {
            val patientList: MutableList<Person> = mutableListOf()
            tracked.forEach {
                var patientId = ""
                var trackedEntityInstance = ""
                var firstName = ""
                var middleName = ""
                var lastName = ""
                var document = ""
                if (it.trackedEntityAttributeValues()?.isNotEmpty() == true) {
                    it.trackedEntityAttributeValues()!!.forEach { k ->
                        trackedEntityInstance = k.trackedEntityInstance().toString()
                        val name = k.trackedEntityAttribute()
                            ?.let { it1 -> SyncStatusHelper.singleAttribute(it1) }

                        if (name != null) {
                            if (name.uid() == "eFbT7iTnljR") {
                                patientId = k.value().toString()

                            }
                            if (name.uid() == "R1vaUuILrDy") {
                                firstName = k.value().toString()

                            }
                            if (name.uid() == "hzVijy6tEUF") {
                                lastName = k.value().toString()

                            }
                            if (name.uid() == "hn8hJsBAKrh") {
                                middleName = k.value().toString()

                            }
                            if (name.uid() == "oob3a4JM7H6") {
                                document = k.value().toString()
                            }
                        }
                    }
                }
                val pp = Person(
                    patientId = patientId,
                    trackedEntityInstance = trackedEntityInstance,
                    firstName = firstName,
                    middleName = middleName,
                    lastName = lastName,
                    document = document,
                    emptyList()
                )

                patientList.add(pp)
                findViewById<RecyclerView>(R.id.recyclerView)
                    .apply {
                        layoutManager = LinearLayoutManager(this@PatientListActivity)
                        val eventAdapter =
                            PatientAdapter(
                                this@PatientListActivity,
                                patientList,
                                this@PatientListActivity::handleClick
                            )
                        adapter = eventAdapter
                    }
            }
        }

    }

    private fun handleClick(person: Person) {

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