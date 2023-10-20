package com.nacare.capture.ui.v2.patients

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.android.androidskeletonapp.R
import com.nacare.capture.data.service.SyncStatusHelper

class PatientSearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_search)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        findViewById<TextView>(R.id.tv_title).apply {
            text = "Patient Search"
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