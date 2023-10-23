package com.nacare.capture.ui.v2.registry

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.nacare.capture.adapters.SummaryAdapter
import com.nacare.capture.data.Constants
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.data.service.SyncStatusHelper
import com.nacare.capture.models.ProgramCategory
import com.nacare.capture.ui.v2.room.MainViewModel

class SummaryActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private val dataList: MutableList<ProgramCategory> = mutableListOf()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)
        viewModel = MainViewModel((this.applicationContext as Application))
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        findViewById<TextView>(R.id.tv_title).apply {
            text = "Summary"
        }
        loadEnrollmentData()
    }
    private fun loadEnrollmentData() {
        val program = FormatterClass().getSharedPref(Constants.PROGRAM_UUID, this@SummaryActivity)
        if (program != null) {
            val stageSections = SyncStatusHelper().getProgramStagesForProgram(program)
            dataList.clear()
            stageSections.forEach {
                val stage = SyncStatusHelper().getProgramStageSections(it.uid())
                val data = ProgramCategory(
                    iconResId = null,
                    name = it.displayName().toString(),
                    id = it.uid(),
                    done ="0",
                    total = "0",
                    elements = emptyList(),
                    altElements = emptyList(),
                    position = program
                )
                dataList.add(data)
            }
            findViewById<RecyclerView>(R.id.recyclerView)
                .apply {
                    layoutManager = LinearLayoutManager(this@SummaryActivity)
                    val eventAdapter =
                        SummaryAdapter(
                            this@SummaryActivity,
                            viewModel,
                            layoutInflater,
                            dataList,
                        )
                    adapter = eventAdapter
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