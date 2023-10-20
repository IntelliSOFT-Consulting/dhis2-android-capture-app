package com.nacare.capture.ui.v2.facility

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.nacare.capture.adapters.FacilityAdapter
import com.nacare.capture.data.Constants.FACILITY_PROGRAM_UUID
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.data.service.SyncStatusHelper
import com.nacare.capture.models.FacilityProgramCategory

class FacilityListActivity : AppCompatActivity() {
    private val dataList: MutableList<FacilityProgramCategory> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_facility_list)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        findViewById<TextView>(R.id.tv_title).apply {
            text = "Facility Details"
        }
        findViewById<TextView>(R.id.tv_sub_title).apply {
            val date = FormatterClass().getSharedPref(
                "event_date",
                this@FacilityListActivity
            )
            var org = FormatterClass().getSharedPref(
                "event_organization",
                this@FacilityListActivity
            )
            if (date != null && org != null) {
                org = getOrganizationName(org)
                text = "$date | $org"

            }
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        loadFacilityDetails()


    }

    private fun loadFacilityDetails() {
        val org = FormatterClass().getSharedPref(
            "event_organization",
            this@FacilityListActivity
        )
        val facility = FormatterClass().getSharedPref(
            FACILITY_PROGRAM_UUID,
            this@FacilityListActivity
        )
        try {

            if (facility != null && org != null) {
                dataList.clear()
                val stages = SyncStatusHelper.programStages(facility)
                stages.forEach {
                    val ele = SyncStatusHelper.programStageSections(it.uid())
                    ele.forEach { k ->
                        val fe = FacilityProgramCategory(
                            id = k.uid().toString(),
                            displayName = k.displayName().toString(),
                            dataElements = k.dataElements()
                        )
                        dataList.add(fe)
                    }
                }

                findViewById<RecyclerView>(R.id.recyclerView)
                    .apply {
                        layoutManager = LinearLayoutManager(this@FacilityListActivity)
                        val eventAdapter =
                            FacilityAdapter(this@FacilityListActivity, dataList, layoutInflater)
                        adapter = eventAdapter
                    }
            } else {

                Toast.makeText(
                    this@FacilityListActivity,
                    "Please Sync data first",
                    Toast.LENGTH_SHORT
                )
                    .show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }

    private fun getOrganizationName(org: String): String? {
        val or = SyncStatusHelper().getOrganizationByUuid(org)
        if (or != null) {
            return or.displayName()
        }
        return ""
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