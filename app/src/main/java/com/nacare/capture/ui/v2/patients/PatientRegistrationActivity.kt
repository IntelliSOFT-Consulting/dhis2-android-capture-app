package com.nacare.capture.ui.v2.patients

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import com.example.android.androidskeletonapp.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.data.service.SyncStatusHelper
import com.nacare.capture.utils.AppUtils
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance

class PatientRegistrationActivity : AppCompatActivity() {
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewProgress: TextView
    private val inputFieldMap = mutableMapOf<String, String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_registration)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        progressBar = findViewById(R.id.progress_bar)
        textViewProgress = findViewById(R.id.text_view_progress)
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
            if (user != null) {
                Log.e("TAG", "Enrollment Found Tracked $user")
                populateViews(lnParent, user)
            }
        }
    }

    private fun populateViews(lnParent: LinearLayout, user: TrackedEntityInstance) {
        val trackedEntity = SyncStatusHelper.trackedEntityAttributes()
        var totalCount = 0
        var doneCount = 0
        trackedEntity.forEach {
            totalCount++
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
                        editText.setText(
                            extractUserInput(
                                it.uid(),
                                user.trackedEntityAttributeValues()
                            )
                        )
                        editText.apply {
                            addTextChangedListener(object : TextWatcher {
                                override fun beforeTextChanged(
                                    s: CharSequence?,
                                    start: Int,
                                    count: Int,
                                    after: Int
                                ) {
                                    // This method is called before the text is changed.
                                }

                                override fun onTextChanged(
                                    s: CharSequence?,
                                    start: Int,
                                    before: Int,
                                    count: Int
                                ) {
                                    if (s != null) {
                                        inputFieldMap[it.uid()] = s.toString()
                                    }
                                }

                                override fun afterTextChanged(s: Editable?) {
                                    // This method is called after the text has changed.
                                    // You can perform actions here based on the updated text.
                                }
                            })
                        }
                    }
                }

                ValueType.valueOf("DATE") -> {

                    val itemView = layoutInflater.inflate(
                        R.layout.item_date_edittext,
                        lnParent,
                        false
                    ) as LinearLayout

                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                    val textInputLayout =
                        itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                    val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                    tvName.text = it.displayName()
                    tvElement.text = it.uid()
                    val value = extractUserInput(
                        it.uid(),
                        user.trackedEntityAttributeValues()
                    )
                    inputFieldMap[it.uid()] = value
                    val keywords = listOf("Birth", "Death")
                    val max = AppUtils().containsAnyKeyword(it.displayName().toString(), keywords)
                    AppUtils().disableTextInputEditText(editText)
                    editText.apply {
                        setText(value)
                        setOnClickListener {
                            AppUtils().showDatePickerDialog(
                                context, editText, setMaxNow = max, setMinNow = false
                            )
                        }

                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                                // This method is called before the text is changed.
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                if (s != null) {
                                    inputFieldMap[it.uid()] = s.toString()
                                }
                            }

                            override fun afterTextChanged(s: Editable?) {
                                // This method is called after the text has changed.
                                // You can perform actions here based on the updated text.
                            }
                        })
                    }
                    lnParent.addView(itemView)
                }

                ValueType.valueOf("BOOLEAN") -> {
                    val itemView = layoutInflater.inflate(
                        R.layout.item_radio,
                        lnParent,
                        false
                    ) as LinearLayout

                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    val radioButtonYes = itemView.findViewById<RadioButton>(R.id.radioButtonYes)
                    val radioButtonNo = itemView.findViewById<RadioButton>(R.id.radioButtonNo)
                    tvName.text = it.displayName()
                    val value = extractUserInput(
                        it.uid(),
                        user.trackedEntityAttributeValues()
                    )
                    inputFieldMap[it.uid()] = value

                    radioButtonNo.apply {
                        setOnCheckedChangeListener { button, isChecked ->
                            if (isChecked) {

                            }
                        }
                    }
                    radioButtonYes.apply {
                        setOnCheckedChangeListener { button, isChecked ->
                            if (isChecked) {

                            }
                        }
                    }
                    lnParent.addView(itemView)
                }


                ValueType.valueOf("LONG_TEXT") -> {

                    val itemView = layoutInflater.inflate(
                        R.layout.item_long_edittext,
                        lnParent,
                        false
                    ) as LinearLayout

                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                    val textInputLayout =
                        itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                    val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                    tvName.text = it.displayName()
                    tvElement.text = it.uid()
                    val value = extractUserInput(
                        it.uid(),
                        user.trackedEntityAttributeValues()
                    )
                    inputFieldMap[it.uid()] = value

                    editText.apply {
                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                                // This method is called before the text is changed.
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                if (s != null) {

                                }
                            }

                            override fun afterTextChanged(s: Editable?) {
                                // This method is called after the text has changed.
                                // You can perform actions here based on the updated text.
                            }
                        })
                    }
                    lnParent.addView(itemView)
                }


                ValueType.valueOf("NUMBER") -> {

                    val itemView = layoutInflater.inflate(
                        R.layout.item_number_edittext,
                        lnParent,
                        false
                    ) as LinearLayout

                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                    val textInputLayout =
                        itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                    val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                    tvName.text = it.displayName()
                    tvElement.text = it.uid()
                    val value = extractUserInput(
                        it.uid(),
                        user.trackedEntityAttributeValues()
                    )
                    inputFieldMap[it.uid()] = value

                    editText.apply {
                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                                // This method is called before the text is changed.
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                if (s != null) {

                                }
                            }

                            override fun afterTextChanged(s: Editable?) {
                                // This method is called after the text has changed.
                                // You can perform actions here based on the updated text.
                            }
                        })
                    }
                    lnParent.addView(itemView)
                }


                ValueType.valueOf("INTEGER_POSITIVE") -> {

                    val itemView = layoutInflater.inflate(
                        R.layout.item_number_edittext,
                        lnParent,
                        false
                    ) as LinearLayout

                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                    val textInputLayout =
                        itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                    val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                    tvName.text = it.displayName()
                    tvElement.text = it.uid()
                    val value = extractUserInput(
                        it.uid(),
                        user.trackedEntityAttributeValues()
                    )
                    inputFieldMap[it.uid()] = value
                    editText.apply {
                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                                // This method is called before the text is changed.
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                if (s != null) {

                                }
                            }

                            override fun afterTextChanged(s: Editable?) {
                                // This method is called after the text has changed.
                                // You can perform actions here based on the updated text.
                            }
                        })
                    }
                    lnParent.addView(itemView)
                }


                ValueType.valueOf("TRUE_ONLY") -> {
                    val itemView = layoutInflater.inflate(
                        R.layout.item_check_box,
                        lnParent,
                        false
                    ) as LinearLayout

                    val checkBox = itemView.findViewById<CheckBox>(R.id.checkBox)
                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    tvName.text = it.displayName()
                    val value = extractUserInput(
                        it.uid(),
                        user.trackedEntityAttributeValues()
                    )
                    inputFieldMap[it.uid()] = value
                    if (value == "true") {
                        checkBox.isChecked = true
                    }
                    checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                        if (isChecked) {

                        } else {

                        }
                    }
                    lnParent.addView(itemView)
                }


                ValueType.valueOf("ORGANISATION_UNIT") -> {

                    val itemView = layoutInflater.inflate(
                        R.layout.item_org_edittext,
                        lnParent,
                        false
                    ) as LinearLayout

                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                    val textInputLayout =
                        itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                    val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                    tvName.text = it.displayName()
                    tvElement.text = it.uid()

                    val value = extractUserInput(
                        it.uid(),
                        user.trackedEntityAttributeValues()
                    )
                    inputFieldMap[it.uid()] = value
                    editText.apply {
                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                                // This method is called before the text is changed.
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                if (s != null) {

                                }
                            }

                            override fun afterTextChanged(s: Editable?) {
                                // This method is called after the text has changed.
                                // You can perform actions here based on the updated text.
                            }
                        })
                    }
                    lnParent.addView(itemView)
                }


                ValueType.valueOf("PHONE_NUMBER") -> {

                    val itemView = layoutInflater.inflate(
                        R.layout.item_phone_number_edittext,
                        lnParent,
                        false
                    ) as LinearLayout

                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                    val textInputLayout =
                        itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                    val editText = itemView.findViewById<TextInputEditText>(R.id.editText)
                    tvName.text = it.displayName()
                    tvElement.text = it.uid()


                    editText.apply {
                        addTextChangedListener(object : TextWatcher {
                            override fun beforeTextChanged(
                                s: CharSequence?,
                                start: Int,
                                count: Int,
                                after: Int
                            ) {
                                // This method is called before the text is changed.
                            }

                            override fun onTextChanged(
                                s: CharSequence?,
                                start: Int,
                                before: Int,
                                count: Int
                            ) {
                                if (s != null) {

                                }
                            }

                            override fun afterTextChanged(s: Editable?) {
                                // This method is called after the text has changed.
                                // You can perform actions here based on the updated text.
                            }
                        })
                    }
                    lnParent.addView(itemView)
                }

                else -> {}
            }
        }
        user.trackedEntityAttributeValues()?.forEach {
            doneCount++
        }
        updateProgress(doneCount, totalCount)
    }

    private fun extractUserInput(
        attribute: String,
        trackedEntityAttributeValues: List<TrackedEntityAttributeValue>?
    ): String {
        if (trackedEntityAttributeValues != null) {
            val matchingAttribute =
                trackedEntityAttributeValues.find { it.trackedEntityAttribute() == attribute }
            return matchingAttribute?.value() ?: ""
        }
        return ""

    }

    private fun updateProgress(doneCount: Int, totalCount: Int) {

        val percent = if (totalCount != 0) {
            (doneCount.toDouble() / totalCount.toDouble()) * 100
        } else {
            0.0 // handle division by zero if necessary
        }
        Log.e("TAG", "Percentage $percent Done $doneCount Total $totalCount")

        progressBar.progress = percent.toInt()
        textViewProgress.text = "${percent.toInt()}%"
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