package com.nacare.capture.ui.v2.registry

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.google.android.material.card.MaterialCardView
import com.nacare.capture.adapters.ProgramAdapter
import com.nacare.capture.data.Constants.PROGRAM_UUID
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.data.Sdk
import com.nacare.capture.data.service.SyncStatusHelper
import com.nacare.capture.models.ProgramCategory
import com.nacare.capture.ui.v2.facility.FacilityListActivity
import com.nacare.capture.ui.v2.patients.PatientListActivity
import com.nacare.capture.ui.v2.patients.PatientRegistrationActivity
import com.nacare.capture.ui.v2.patients.PatientSearchActivity
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.ProgramStageSection

class RegistryActivity : AppCompatActivity() {

    private val dataList: MutableList<ProgramCategory> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registry)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        findViewById<TextView>(R.id.tv_title).apply {
            text = "The National Cancer Registry of Kenya Notification Form"
        }
        findViewById<MaterialCardView>(R.id.materialCardView).apply {
            setOnClickListener {
                startActivity(
                    Intent(
                        this@RegistryActivity,
                        FacilityListActivity::class.java
                    )
                )
            }
        }
        findViewById<MaterialCardView>(R.id.patientCardView).apply {
            setOnClickListener {
                val enroll = FormatterClass().getSharedPref(
                    "enrollment_id",
                    this@RegistryActivity
                )
                if (enroll != null) {
                    startActivity(
                        Intent(
                            this@RegistryActivity,
                            PatientRegistrationActivity::class.java
                        )
                    )
                } else {
                    startActivity(Intent(this@RegistryActivity, PatientSearchActivity::class.java))
                }
            }
        }
        findViewById<TextView>(R.id.tv_sub_title).apply {
            val date = FormatterClass().getSharedPref(
                "event_date",
                this@RegistryActivity
            )
            var org = FormatterClass().getSharedPref(
                "event_organization",
                this@RegistryActivity
            )
            if (date != null && org != null) {
                org = getOrganizationName(org)
                text = "$date | $org"
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val program = FormatterClass().getSharedPref(PROGRAM_UUID, this@RegistryActivity)
        if (program != null) {
            val stageSections = SyncStatusHelper().getProgramStagesForProgram(program)
            dataList.clear()
            stageSections.forEach {
                val stage = SyncStatusHelper().getProgramStageSections(it.uid())
                val data = ProgramCategory(
                    iconResId = null,
                    name = it.displayName().toString(),
                    id = it.uid(),
                    done = retrieveTotalDataElementValues(it.uid(), stage),
                    total = retrieveTotalDataElements(stage),
                    elements = emptyList(),
                    altElements = emptyList(),
                    position = program
                )
                dataList.add(data)
            }
            findViewById<RecyclerView>(R.id.recyclerView)
                .apply {
                    layoutManager = LinearLayoutManager(this@RegistryActivity)
                    val eventAdapter =
                        ProgramAdapter(
                            this@RegistryActivity,
                            dataList,
                            this@RegistryActivity::handleClick
                        )
                    adapter = eventAdapter
                }

        }
    }

    private fun retrieveTotalDataElementValues(
        program: String,
        stage: List<ProgramStageSection>
    ): String {
        var total = 0
        val enroll = FormatterClass().getSharedPref(
            "enrollment_id",
            this@RegistryActivity
        )
        if (enroll != null) {
            val singleEvent = SyncStatusHelper.getEventsPerEnrollment(program, enroll)
            Log.e("TAG", "Tracked Entity View Stage $program")
            Log.e("TAG", "Tracked Entity View Events $singleEvent")
            if (singleEvent != null) {
                val attributes = SyncStatusHelper.getEventAttribute(singleEvent.uid())
                Log.e("TAG", "Tracked Entity View Attributes $attributes")
            }
        }
        return "$total"

    }

    private fun retrieveTotalDataElements(stage: List<ProgramStageSection>): String {
        var total = 0
        stage.forEach {
            if (it.dataElements() != null) {
                it.dataElements()!!.forEach { _ ->
                    total++
                }
            }
        }
        return "$total"

    }

    private fun getOrganizationName(org: String): String? {
        val or = SyncStatusHelper().getOrganizationByUuid(org)
        if (or != null) {
            return or.displayName()
        }
        return ""
    }

    private fun handleClick(data: ProgramCategory) {
        val enroll = FormatterClass().getSharedPref(
            "enrollment_id",
            this@RegistryActivity
        )
        if (enroll != null) {
            FormatterClass().saveSharedPref(
                "section_id", data.id,
                this@RegistryActivity
            )
            val date = FormatterClass().getSharedPref(
                "event_date",
                this@RegistryActivity
            )
            val org = FormatterClass().getSharedPref(
                "event_organization",
                this@RegistryActivity
            )


            if (date != null && org != null) {
                SyncStatusHelper.createEvent()
                startActivity(Intent(this@RegistryActivity, ResponderActivity::class.java))
            }
        }
    }

    private fun handleClickOld(data: ProgramCategory) {
        try {
            val date = FormatterClass().getSharedPref(
                "event_date",
                this@RegistryActivity
            )
            val org = FormatterClass().getSharedPref(
                "event_organization",
                this@RegistryActivity
            )
            val enroll = FormatterClass().getSharedPref(
                "enrollment_id",
                this@RegistryActivity
            )


            if (date != null && org != null) {
                val eventBuilder = EventCreateProjection.builder()
                    .program(data.position)
                    .organisationUnit(org)
                    .programStage(data.id)
                    .enrollment(date)
                    .build()
                // Create the empty event
                val eventUid = Sdk.d2().eventModule().events()
                    .blockingAdd(eventBuilder)
                Sdk.d2().eventModule().events().uid(eventUid).apply {
                    setStatus(EventStatus.ACTIVE)
                    setEventDate(FormatterClass().parseEventDate(date))
                    Log.e("TAG", "Event created with UID: $eventUid")
                }
            }
        } catch (e: Exception) {
            Log.e("TAG", "Program Click Exception ${e.message}")
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