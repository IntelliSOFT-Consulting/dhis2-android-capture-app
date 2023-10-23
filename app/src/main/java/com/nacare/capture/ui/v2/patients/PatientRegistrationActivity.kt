package com.nacare.capture.ui.v2.patients

import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nacare.capture.adapters.OrganizationAdapter
import com.nacare.capture.data.Constants.PROGRAM_TRACKED_UUID
import com.nacare.capture.data.Constants.PROGRAM_UUID
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.data.Sdk.d2
import com.nacare.capture.data.service.ActivityStarter
import com.nacare.capture.data.service.SyncStatusHelper
import com.nacare.capture.models.OrganisationUnit
import com.nacare.capture.ui.main.MainActivity
import com.nacare.capture.ui.v2.room.MainViewModel
import com.nacare.capture.ui.v2.room.TrackedEntityValues
import com.nacare.capture.utils.AppUtils
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.enrollment.EnrollmentCreateProjection
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstanceCreateProjection
import java.text.SimpleDateFormat
import java.util.Locale


class PatientRegistrationActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private lateinit var progressBar: ProgressBar
    private lateinit var textViewProgress: TextView
    private val inputFieldMap = mutableMapOf<String, String>()
    private val hashMap = mutableMapOf<String, String>()
    private val hashMapResponse = mutableMapOf<String, String>()
    private var countyList = mutableListOf<OrganisationUnit>()
    private lateinit var dialog: AlertDialog
    private lateinit var lnParent: LinearLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_registration)
        viewModel = MainViewModel((this.applicationContext as Application))
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        progressBar = findViewById(R.id.progress_bar)
        textViewProgress = findViewById(R.id.text_view_progress)
        lnParent = findViewById(R.id.ln_parent)
        findViewById<TextView>(R.id.tv_title).apply {
            text = "Patient Details"
        }
        loadCurrentEnrollment(lnParent)

        loadCounties()
        findViewById<MaterialButton>(R.id.prev_button).apply {
            setOnClickListener {
                this@PatientRegistrationActivity.finish()
            }
        }
        findViewById<MaterialButton>(R.id.next_button).apply {
            setOnClickListener {
                this@PatientRegistrationActivity.finish()
            }
        }
    }

    private fun loadCounties() {
        val organizations = SyncStatusHelper.loadCounties()
        organizations.forEach {
            Log.e("TAG", "Loaded Counties ${it.displayName()}")
            val org = OrganisationUnit(id = it.uid().toString(), name = it.displayName().toString())
            countyList.add(org)
        }
    }

    private fun loadCurrentEnrollment(lnParent: LinearLayout) {
        val enroll = FormatterClass().getSharedPref(
            "enrollment_id",
            this@PatientRegistrationActivity
        )
        if (enroll != null) {
            proceedWithEnrollment(lnParent, true)
        } else {
            proceedWithEnrollment(lnParent, false)
        }
    }

    private fun proceedWithEnrollment(lnParent: LinearLayout, isEnrolled: Boolean) {
        if (isEnrolled) {
            val enroll = FormatterClass().getSharedPref(
                "enrollment_id",
                this@PatientRegistrationActivity
            )
            val dataUser = SyncStatusHelper.getSingleEnrollment(enroll)
            val user = SyncStatusHelper.getTrackedEntity(dataUser.trackedEntityInstance())
            if (user != null) {

                populateViews(lnParent, user)
            }
        } else {
            createNewTrackedEntity()
        }

    }

    private fun createNewTrackedEntity() {
        val date = FormatterClass().getSharedPref("event_date", this)
        val orgUnitUid = FormatterClass().getSharedPref("event_organization", this)
        val trackedEntityTypeUid = FormatterClass().getSharedPref(PROGRAM_TRACKED_UUID, this)
        val programUid = FormatterClass().getSharedPref(PROGRAM_UUID, this)
        if (date != null && orgUnitUid != null && trackedEntityTypeUid != null && programUid != null) {
            val eventBuilder = TrackedEntityInstanceCreateProjection.builder()
                .organisationUnit(orgUnitUid)
                .trackedEntityType(trackedEntityTypeUid)
                .build()
            // Create the empty event
            val eventUid = d2().trackedEntityModule().trackedEntityInstances()
                .blockingAdd(eventBuilder)
            d2().trackedEntityModule().trackedEntityInstances().uid(eventUid).apply {


                val enrollmentBuilder = EnrollmentCreateProjection.builder()
                    .trackedEntityInstance(eventUid)
                    .program(programUid)
                    .organisationUnit(orgUnitUid)
                    .build()
                // Create the empty event
                val enrollmentUid = d2().enrollmentModule().enrollments()
                    .blockingAdd(enrollmentBuilder)
                d2().enrollmentModule().enrollments().uid(enrollmentUid).apply {
                    try {
                        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                        val dateInput = inputFormat.parse(date)
                        // Define the desired output format
                        val outputFormat =
                            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.ENGLISH)
                        // Format the date to the desired output format
                        val formattedDateStr = outputFormat.format(dateInput)
                        val formattedDate = outputFormat.parse(formattedDateStr)

                        // Use the formatted date in your code
                        println("Formatted Date: $formattedDate")
                        setEnrollmentDate(formattedDate)
                        setFollowUp(false)
                        setIncidentDate(formattedDate)
                    } catch (e: Exception) {

                    }
                    FormatterClass().saveSharedPref(
                        "enrollment_id",
                        enrollmentUid,
                        this@PatientRegistrationActivity
                    )
                    ActivityStarter.startActivity(
                        this@PatientRegistrationActivity,
                        MainActivity.getRegistrationActivityIntent(this@PatientRegistrationActivity),
                        true
                    )

                }
            }
        } else {
            Toast.makeText(this, "Please check input data", Toast.LENGTH_SHORT).show()
        }


    }

    private fun initialPatientEnrollment(instanceUid: String, programUid: String, orgUnit: String) {
        try {
            val eventBuilder = EnrollmentCreateProjection.builder()
                .trackedEntityInstance(instanceUid)
                .program(programUid)
                .organisationUnit(orgUnit)
                .build()
            // Create the empty event
            val eventUid = d2().enrollmentModule().enrollments()
                .blockingAdd(eventBuilder)
            d2().enrollmentModule().enrollments().uid(eventUid).apply {

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun populateViews(lnParent: LinearLayout, user: TrackedEntityInstance?) {
        val trackedEntity = SyncStatusHelper.trackedEntityAttributes()
        var totalCount = 0
        var doneCount = 0
        // patient id  and hospital number
        val orderOfUids = listOf(
            "AP13g7NcBOf",
            "zeXqc0lTQ4w",
            "R1vaUuILrDy",
            "hn8hJsBAKrh",
            "hzVijy6tEUF",
            "oob3a4JM7H6",
            "eFbT7iTnljR",
            "mPpjmOxwsEZ",
            "xED9XkpCeUe",
            "oLeKnI7oDRc",
            "zO1NzQhJJwL",
            "j4rtuBIXa67",
            "ITBGHMF16q9",
            "pU4YBVMSaVO",
            "LqSCqNOUn1N",
            "ylvpmyVq8X7",
            "yTU9PxoBN6b",
            "uR2Mnlh7sqn",
            "e9e7MiIbpfc",
            "ud36eYLaM3d",
            "rFv8ampbwIz",
            "yIp9UZ1Bex6",
            "RhplKXZoKsC",
            "QEzr036CMtu",
            "ucu4YRNaTFv",
            "cqa6c8DZX1g",
            "Re1Najhy7ow",
            "oK6AefPsVNh"
        )
        val uidToIndexMap = trackedEntity.mapIndexed { index, item -> item.uid() to index }.toMap()
        val orderedList =
            orderOfUids.mapNotNull { uidToIndexMap[it]?.let { index -> trackedEntity[index] } }


        orderedList.forEach {
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
                        if (user != null) {
                            editText.setText(
                                extractUserInput(
                                    it.uid(),
                                    user.trackedEntityAttributeValues()
                                )
                            )
                        }
                        editText.apply {
                            if (user != null) {
                                setText(retrievedRecordedResponse(user.uid(), it.uid()))
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
                                        if (user != null) {
                                            updateTrackedEntityAttribute(
                                                user.uid(),
                                                it.uid(),
                                                s.toString()
                                            )
                                        }
                                    }
                                }

                                override fun afterTextChanged(s: Editable?) {
                                    // This method is called after the text has changed.
                                    // You can perform actions here based on the updated text.
                                }
                            })
                        }
                    } else {
                        val itemView = layoutInflater.inflate(
                            R.layout.item_autocomplete,
                            lnParent,
                            false
                        ) as LinearLayout
                        val optionsList: MutableList<String> = mutableListOf()
                        val adp = ArrayAdapter(
                            this@PatientRegistrationActivity,
                            android.R.layout.simple_list_item_1,
                            optionsList
                        )
                        val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                        val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                        val textInputLayout =
                            itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                        val autoCompleteTextView =
                            itemView.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
                        val op = SyncStatusHelper().getDataElementOptions(it.optionSet()!!.uid())

                        tvElement.text = it.uid()
                        optionsList.clear()
                        op.forEach { k ->
                            optionsList.add(k.displayName().toString())
                            hashMap[k.displayName().toString()] = k.code().toString()
                            hashMapResponse[k.code().toString()] = k.displayName().toString()
                        }
                        tvName.text = it.displayName()
                        autoCompleteTextView.setAdapter(adp)
                        adp.notifyDataSetChanged()


                        autoCompleteTextView.apply {
                            if (user != null) {
                                var dataRes =
                                    retrievedRecordedResponse(user.uid(), it.uid())
                                if (dataRes.isNotEmpty()) {
                                    dataRes = reInvertResponse(dataRes)
                                    if (dataRes != null) {
                                        setText(dataRes, false)
                                    }
                                }
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
                                        if (user != null) {
                                            val value = generateCode(s.toString())
                                            updateTrackedEntityAttribute(
                                                user.uid(), it.uid(),
                                                value
                                            )
                                        }
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

                    val keywords = listOf("Birth", "Death")
                    val max = AppUtils().containsAnyKeyword(it.displayName().toString(), keywords)
                    AppUtils().disableTextInputEditText(editText)
                    editText.apply {
                        if (user != null) {
                            setText(retrievedRecordedResponse(user.uid(), it.uid()))
                        }
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
                                    if (user != null) {
                                        updateTrackedEntityAttribute(
                                            user.uid(),
                                            it.uid(),
                                            s.toString()
                                        )
                                    }
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
                    if (user != null) {
                        val dataValue = retrievedRecordedResponse(user.uid(), it.uid())
                        if (dataValue == "true") {
                            radioButtonYes.isChecked = true
                        }
                        if (dataValue == "false") {
                            radioButtonNo.isChecked = true
                        }

                    }
                    radioButtonNo.apply {
                        setOnCheckedChangeListener { button, isChecked ->
                            if (isChecked) {
                                if (user != null) {
                                    updateTrackedEntityAttribute(
                                        user.uid(),
                                        it.uid(),
                                        "false"
                                    )
                                }
                            }
                        }
                    }
                    radioButtonYes.apply {
                        setOnCheckedChangeListener { button, isChecked ->
                            if (isChecked) {
                                if (user != null) {
                                    updateTrackedEntityAttribute(
                                        user.uid(),
                                        it.uid(),
                                        "true"
                                    )
                                }
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
                    if (user != null) {
                        val value = extractUserInput(
                            it.uid(),
                            user.trackedEntityAttributeValues()
                        )
                        inputFieldMap[it.uid()] = value
                    }
                    editText.apply {
                        if (user != null) {
                            setText(retrievedRecordedResponse(user.uid(), it.uid()))
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
                                    if (user != null) {
                                        updateTrackedEntityAttribute(
                                            user.uid(),
                                            it.uid(),
                                            s.toString()
                                        )
                                    }
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
                    if (user != null) {
                        val value = extractUserInput(
                            it.uid(),
                            user.trackedEntityAttributeValues()
                        )
                        inputFieldMap[it.uid()] = value
                        editText.setText(value)
                    }

                    editText.apply {
                        if (user != null) {
                            setText(retrievedRecordedResponse(user.uid(), it.uid()))
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
                                    if (user != null) {
                                        updateTrackedEntityAttribute(
                                            user.uid(),
                                            it.uid(),
                                            s.toString()
                                        )
                                    }
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
                    if (user != null) {
                        val value = extractUserInput(
                            it.uid(),
                            user.trackedEntityAttributeValues()
                        )
                        inputFieldMap[it.uid()] = value
                        editText.setText(value)
                    }
                    editText.apply {
                        if (user != null) {
                            setText(retrievedRecordedResponse(user.uid(), it.uid()))
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
                                    if (user != null) {
                                        updateTrackedEntityAttribute(
                                            user.uid(),
                                            it.uid(),
                                            s.toString()
                                        )
                                    }
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
                    if (user != null) {
                        val dataValue = retrievedRecordedResponse(user.uid(), it.uid())
                        if (dataValue == "true") {
                            checkBox.isChecked = true
                        }

                    }
                    checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                        if (isChecked) {
                            if (user != null) {
                                updateTrackedEntityAttribute(
                                    user.uid(),
                                    it.uid(),
                                    "true"
                                )
                            }
                        } else {
                            if (user != null) {
                                updateTrackedEntityAttribute(
                                    user.uid(),
                                    it.uid(),
                                    "false"
                                )
                            }
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

                    val currentAttribute = it

                    if (user != null) {
                        val value = extractUserInput(
                            it.uid(),
                            user.trackedEntityAttributeValues()
                        )
                        editText.setText(value)

                        inputFieldMap[it.uid()] = value
                    }
                    editText.apply {
                        if (user != null) {
                            when {
                                currentAttribute.uid() == "uR2Mnlh7sqn" -> {
                                    val test = retrievedRecordedResponse(user.uid(), it.uid())
                                    if (test.isNotEmpty()) {
                                        val county = SyncStatusHelper.loadResidence(2, test)
                                        setText("${county.displayName()}")
                                    }
                                }

                                currentAttribute.uid() == "e9e7MiIbpfc" -> {
                                    val test = retrievedRecordedResponse(user.uid(), it.uid())
                                    if (test.isNotEmpty()) {
                                        val county = SyncStatusHelper.loadResidence(3, test)
                                        setText("${county.displayName()}")
                                    }
                                }

                                currentAttribute.uid() == "ud36eYLaM3d" -> {
                                    val test = retrievedRecordedResponse(user.uid(), it.uid())
                                    if (test.isNotEmpty()) {
                                        val county = SyncStatusHelper.loadResidence(4, test)
                                        setText("${county.displayName()}")
                                    }
                                }
                            }
                        }
                        setOnClickListener {
                            //IF county, load county list
                            when {
                                //county
                                currentAttribute.uid() == "uR2Mnlh7sqn" -> {
                                    if (user != null) {
                                        showOrganizationUnit(
                                            editText,
                                            user.uid(),
                                            tvElement.text.toString(),
                                            countyList
                                        )

                                        viewModel.deleteTrackedEntity(user.uid(),"e9e7MiIbpfc")
                                        viewModel.deleteTrackedEntity(user.uid(),"ud36eYLaM3d")
                                    }
                                }

                                //sub-county
                                currentAttribute.uid() == "e9e7MiIbpfc" -> {
                                    if (user != null) {
                                        val test =
                                            retrievedRecordedResponse(user.uid(), "uR2Mnlh7sqn")
                                        if (test.isNotEmpty()) {
                                            val subCounties =
                                                SyncStatusHelper.loadSubCounties(test)
                                            countyList.clear()
                                            subCounties.forEach { c ->
                                                val subCounty = OrganisationUnit(
                                                    id = c.uid(),
                                                    name = c.displayName().toString()
                                                )
                                                countyList.add(subCounty)
                                            }
                                            showOrganizationUnit(
                                                editText,
                                                user.uid(),
                                                tvElement.text.toString(),
                                                countyList
                                            )
                                        }
                                    }
                                }
                                //ward
                                currentAttribute.uid() == "ud36eYLaM3d" -> {
                                    if (user != null) {
                                        val test =
                                            retrievedRecordedResponse(user.uid(), "e9e7MiIbpfc")
                                        if (test.isNotEmpty()) {
                                            val subCounties =
                                                SyncStatusHelper.loadWards(test)
                                            countyList.clear()
                                            subCounties.forEach { c ->
                                                val subCounty = OrganisationUnit(
                                                    id = c.uid(),
                                                    name = c.displayName().toString()
                                                )
                                                countyList.add(subCounty)
                                            }
                                            showOrganizationUnit(
                                                editText,
                                                user.uid(),
                                                tvElement.text.toString(),
                                                countyList
                                            )
                                        }
                                    }
                                }
                            }
                        }

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
                        if (user != null) {
                            setText(retrievedRecordedResponse(user.uid(), it.uid()))
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
                                    if (user != null) {
                                        updateTrackedEntityAttribute(
                                            user.uid(),
                                            it.uid(),
                                            s.toString()
                                        )
                                    }
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
        if (user != null) {
            doneCount = viewModel.getTrackedAttributeResponses(user.uid())

        }
        updateProgress(doneCount, totalCount)
    }

    private fun showOrganizationUnit(
        editText: TextInputEditText,
        entityUid: String,
        attributeUid: String,
        countyList: MutableList<OrganisationUnit>
    ) {
        val dialogBuilder = AlertDialog.Builder(this@PatientRegistrationActivity)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_tree, null)
        dialogBuilder.setView(dialogView)
        val recyclerView: RecyclerView = dialogView.findViewById(R.id.recyclerView)
        val adapter = OrganizationAdapter(
            this@PatientRegistrationActivity,
            countyList,
            entityUid,
            attributeUid
        )
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this@PatientRegistrationActivity)
        dialogBuilder.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
            val intent = Intent(this, PatientRegistrationActivity::class.java)
            finish() // Close the current instance of the activity
            startActivity(intent)


        }

        dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun reInvertResponse(dataRes: String): String {
        return hashMapResponse[dataRes].toString()
    }

    private fun generateCode(value: String): String {
        return hashMap[value].toString()
    }

    private fun retrievedRecordedResponse(entityUid: String, attributeUid: String): String {
        var response = ""
        val res = viewModel.getRecordedResponse(entityUid, attributeUid)
        if (res != null) {
            response = res
        }
        return response


    }

    private fun updateTrackedEntityAttribute(
        entityUid: String,
        attributeUid: String,
        dataValue: String
    ) {

        val tracked = TrackedEntityValues(
            entityUid = entityUid,
            attributeUid = attributeUid,
            dataValue = dataValue,
        )
        viewModel.addUpdateTrackedEntity(tracked)


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