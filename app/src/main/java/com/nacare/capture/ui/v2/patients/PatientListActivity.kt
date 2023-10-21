package com.nacare.capture.ui.v2.patients

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.nacare.capture.adapters.EventAdapter
import com.nacare.capture.adapters.PatientAdapter
import com.nacare.capture.data.Constants
import com.nacare.capture.data.Constants.PROGRAM_UUID
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.data.Sdk
import com.nacare.capture.data.service.SyncStatusHelper
import com.nacare.capture.models.CodeValue
import com.nacare.capture.models.Person
import com.nacare.capture.utils.AppUtils
import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection
import org.hisp.dhis.android.core.enrollment.EnrollmentStatus
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

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
        val receivedInputs: List<CodeValue>? = intent.getParcelableArrayListExtra("collectedInputs")

        if (receivedInputs != null) {
            val tracked = SyncStatusHelper.trackedEntityInstanceList()
            if (tracked.isNotEmpty()) {
                val patientList: MutableList<Person> = mutableListOf()
                val filteredPatients = mutableListOf<TrackedEntityInstance>()
                tracked.forEach { patient ->
                    val attributes = patient.trackedEntityAttributeValues()
                    attributes?.forEach { attributeValue ->
                        val entity = attributeValue.trackedEntityAttribute()
                        val value = attributeValue.value()

                        // Replace "yourFilterKey" with the actual filter key you want to use
                        val collectedInput = receivedInputs.find { it.attributeUid == entity }

                        if (collectedInput != null && value == collectedInput.value) {
                            // If the condition is met, add the patient to the filtered list
                            filteredPatients.add(patient)
                        }
                    }
                }

                filteredPatients.forEach {
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
        AppUtils().makeBold(findViewById(R.id.numberTextView))
        AppUtils().makeBold(findViewById(R.id.firstNameTextView))
        AppUtils().makeBold(findViewById(R.id.lastNameTextView))
        AppUtils().makeBold(findViewById(R.id.middleNameTextView))
        AppUtils().makeBold(findViewById(R.id.documentTextView))


    }

    private fun handleClick(person: Person) {
        try {
            val existingEnrollment =
                SyncStatusHelper.getAllActiveEntityEnrolments(person.trackedEntityInstance)
            if (existingEnrollment.isNotEmpty()) {
                Log.e("TAG", "Patient Has an active enrollment")
                val single =
                    SyncStatusHelper.getLatestEntityEnrollment(person.trackedEntityInstance)
                FormatterClass().saveSharedPref(
                    "enrollment_id",
                    single.uid(),
                    this@PatientListActivity
                )
                startActivity(
                    Intent(
                        this@PatientListActivity,
                        PatientRegistrationActivity::class.java
                    )
                )
                this@PatientListActivity.finish()

            } else {
                val programs = SyncStatusHelper.programList()
                if (programs.isNotEmpty()) {
                    Log.e("TAG", "Programs -> :::: $programs")
                    val notification =
                        programs.find { it.name() == "The National Cancer Registry of Kenya Notification Form" }
                    if (notification != null) {
                        FormatterClass().saveSharedPref(
                            PROGRAM_UUID,
                            notification.uid(),
                            this@PatientListActivity
                        )
                    }
                    val program =
                        FormatterClass().getSharedPref(PROGRAM_UUID, this@PatientListActivity)
                    if (program != null) {
                        val date = FormatterClass().getSharedPref(
                            "event_date",
                            this@PatientListActivity
                        )
                        val org = FormatterClass().getSharedPref(
                            "event_organization",
                            this@PatientListActivity
                        )
                        if (date != null && org != null) {
                            val enrollmentBuild = EnrollmentCreateProjection.builder()
                                .program(program)
                                .organisationUnit(org)
                                .trackedEntityInstance(person.trackedEntityInstance)
                                .build()

                            val enrolment = Sdk.d2().enrollmentModule().enrollments()
                                .blockingAdd(enrollmentBuild)
                            Sdk.d2().enrollmentModule().enrollments().uid(enrolment).apply {
                                setEnrollmentDate(FormatterClass().parseEventDate(date))
                                setStatus(EnrollmentStatus.ACTIVE)

                            }
                            FormatterClass().saveSharedPref(
                                "enrollment_id",
                                enrolment,
                                this@PatientListActivity
                            )

                        }
                    }

                } else {
                    Log.e("TAG", "Empty Programs")
                    Toast.makeText(
                        this@PatientListActivity,
                        "Please Sync data first",
                        Toast.LENGTH_SHORT
                    )
                        .show()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(
                this@PatientListActivity,
                "Please Sync data first",
                Toast.LENGTH_SHORT
            )
                .show()
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