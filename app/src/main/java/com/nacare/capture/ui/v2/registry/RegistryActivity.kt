package com.nacare.capture.ui.v2.registry

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.nacare.capture.adapters.ProgramAdapter
import com.nacare.capture.data.Constants.PROGRAM_UUID
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.data.service.SyncStatusHelper
import com.nacare.capture.models.ProgramCategory

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
        findViewById<TextView>(R.id.tv_sub_title).apply {
            text = "date | Org"
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        val program = FormatterClass().getSharedPref(PROGRAM_UUID, this@RegistryActivity)
        if (program != null) {
            val stageSections = SyncStatusHelper().getProgramStagesForProgram(program)
            dataList.clear()
            stageSections.forEach {
                Log.e("TAG", "Program Stages ${it.displayName()}")
                val data = ProgramCategory(
                    iconResId = null,
                    name = it.displayName().toString(),
                    id = it.uid(),
                    done = "0",
                    total = "0",
                    elements = emptyList(),
                    altElements = emptyList(),
                    position = "0"
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

    private fun handleClick(programCategory: ProgramCategory) {

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