package com.nacare.capture.ui.v2.registry

import android.app.Application
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
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
import com.nacare.capture.ui.v2.room.MainViewModel
import org.hisp.dhis.android.core.event.EventCreateProjection
import org.hisp.dhis.android.core.event.EventStatus
import org.hisp.dhis.android.core.program.ProgramStageSection

class RegistryActivity : AppCompatActivity() {
    private var overallDone = 0
    private var overallTotal = 0
    private lateinit var progressBar: ProgressBar
    private lateinit var progressTextView: TextView
    private val dataList: MutableList<ProgramCategory> = mutableListOf()
    private lateinit var viewModel: MainViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registry)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        viewModel = MainViewModel((this.applicationContext as Application))
        progressBar = findViewById(R.id.progress_bar)
        progressTextView = findViewById(R.id.text_view_progress)
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
        findViewById<TextView>(R.id.progressTextView).apply {
            text = getPatientProgress()
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
        loadEnrollmentData()
    }

    private fun loadEnrollmentData() {
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
            computeAndDisplayTotalScore(overallDone, overallTotal)

        }
    }

    override fun onResume() {
        loadEnrollmentData()
        super.onResume()
    }

    private fun computeAndDisplayTotalScore(overallDone: Int, overallTotal: Int) {

        val percent = if (overallTotal != 0) {
            (overallDone.toDouble() / overallTotal.toDouble()) * 100
        } else {
            0.0 // handle division by zero if necessary
        }

        progressBar.progress = percent.toInt()
        progressTextView.text = "${percent.toInt()}%"


    }

    private fun getPatientProgress(): String {
        var done = 0
        var total = 0
        val trackedEntity = SyncStatusHelper.trackedEntityAttributes()
        trackedEntity.forEach {
            total++
        }
        overallTotal += total
        overallDone += done
        return "$done/$total"

    }

    private fun retrieveTotalDataElementValues(
        programUid: String,
        stage: List<ProgramStageSection>
    ): String {
        var done = 0
        val enrollmentUid = FormatterClass().getSharedPref(
            "enrollment_id",
            this@RegistryActivity
        )
        if (enrollmentUid != null) {
            done = viewModel.getResponseList(enrollmentUid, programUid)

        }
        overallDone += done

        return "$done"

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
        overallTotal += total
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
                "section_name", data.name,
                this@RegistryActivity
            )
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
                Log.e("TAG", "Proceed to Responder Page.......")
                startActivity(Intent(this@RegistryActivity, ResponderActivity::class.java))
            }
        } else {
            Toast.makeText(this@RegistryActivity, "Please Provide Patient Data", Toast.LENGTH_SHORT)
                .show()
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