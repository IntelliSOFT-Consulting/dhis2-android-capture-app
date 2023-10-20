package com.nacare.capture.ui.v2.patients

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.android.androidskeletonapp.R
import com.nacare.capture.data.Sdk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.Normalizer

class PatientSearchActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_search)

    }
}