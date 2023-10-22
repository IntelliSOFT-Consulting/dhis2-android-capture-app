package com.nacare.capture.ui.v2.registry

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.Html
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
import androidx.appcompat.widget.Toolbar
import com.example.android.androidskeletonapp.R
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.data.service.SyncStatusHelper
import com.nacare.capture.ui.v2.room.MainViewModel
import com.nacare.capture.ui.v2.room.ProgramDataValues
import com.nacare.capture.utils.AppUtils
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.option.Option

class ResponderActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel
    private val hashMap = mutableMapOf<String, String>()
    private val hashMapResponse = mutableMapOf<String, String>()
    private lateinit var progressBar: ProgressBar
    private lateinit var progressTextView: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_responder)
        viewModel = MainViewModel((this.applicationContext as Application))
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        progressBar = findViewById(R.id.progress_bar)
        progressTextView = findViewById(R.id.text_view_progress)
        val lnParent = findViewById<LinearLayout>(R.id.ln_parent)
        Log.e("TAG", "Reached Responder Class...")
        findViewById<MaterialButton>(R.id.next_button).apply {
            setOnClickListener {
                this@ResponderActivity.finish()
            }
        }
        findViewById<MaterialButton>(R.id.prev_button).apply {
            setOnClickListener {
                this@ResponderActivity.finish()
            }
        }
        findViewById<TextView>(R.id.tv_title).apply {
            val name = FormatterClass().getSharedPref(
                "section_name",
                this@ResponderActivity
            )
            text = name
        }
        findViewById<TextView>(R.id.textView).apply {
            val name = FormatterClass().getSharedPref(
                "section_name",
                this@ResponderActivity
            )
            val org = FormatterClass().getSharedPref(
                "event_organization",
                this@ResponderActivity
            )
            val orgData = SyncStatusHelper().getOrganizationByUuid(org)
            val formattedText = "Saving to <b>$name</b> in <b>${orgData.name()}</b>"
            text = Html.fromHtml(formattedText, Html.FROM_HTML_MODE_LEGACY)

        }
        val section = FormatterClass().getSharedPref(
            "section_id",
            this@ResponderActivity
        )
        if (section != null) {
            val dataEnrollment = SyncStatusHelper().getProgramStageSections(section)
            dataEnrollment.forEach {
                it.dataElements()!!.forEach { k ->
                    populateViews(lnParent, k)
                }
            }
            calculateProgress()
        }

    }

    private fun calculateProgress() {
        val programUid = FormatterClass().getSharedPref(
            "section_id",
            this@ResponderActivity
        )
        val enrollmentUid = FormatterClass().getSharedPref(
            "enrollment_id",
            this@ResponderActivity
        )
        if (programUid != null && enrollmentUid != null) {
            var total = 0
            val dataEnrollment = SyncStatusHelper().getProgramStageSections(programUid)
            dataEnrollment.forEach {
                it.dataElements()!!.forEach { k ->
                    total++

                }
            }
            val count = viewModel.getResponseList(enrollmentUid, programUid)

            val percent = if (total != 0) {
                (count.toDouble() / total.toDouble()) * 100
            } else {
                0.0 // handle division by zero if necessary
            }

            progressBar.progress = percent.toInt()
            progressTextView.text = "${percent.toInt()}%"

        }
    }

    private fun populateViews(lnParent: LinearLayout, it: DataElement) {
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
                    editText.apply {
                        setText(retrievedRecordedResponse(it.uid()))
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
                                    createUpdateSectionValue(it.uid(), s.toString())
                                }
                            }

                            override fun afterTextChanged(s: Editable?) {
                                // This method is called after the text has changed.
                                // You can perform actions here based on the updated text.
                            }
                        })
                    }

                }
                else {
                    val itemView = layoutInflater.inflate(
                        R.layout.item_autocomplete,
                        lnParent,
                        false
                    ) as LinearLayout
                    val optionsList: MutableList<String> = mutableListOf()
                    val adp = ArrayAdapter(
                        this@ResponderActivity,
                        android.R.layout.simple_list_item_1,
                        optionsList
                    )
                    val tvName = itemView.findViewById<TextView>(R.id.tv_name)
                    val tvElement = itemView.findViewById<TextView>(R.id.tv_element)
                    val textInputLayout =
                        itemView.findViewById<TextInputLayout>(R.id.textInputLayout)
                    val autoCompleteTextView =
                        itemView.findViewById<AutoCompleteTextView>(R.id.autoCompleteTextView)
                    val op = SyncStatusHelper().getDataElementOptions(it.optionSetUid())

                    tvElement.text = it.uid()
                    optionsList.clear()
                    op.forEach {
                        optionsList.add(it.displayName().toString())
                        hashMap[it.displayName().toString()] = it.code().toString()
                        hashMapResponse[it.code().toString()] = it.displayName().toString()
                    }
                    tvName.text = it.displayName()
                    autoCompleteTextView.setAdapter(adp)
                    adp.notifyDataSetChanged()


                    autoCompleteTextView.apply {
                        var dataRes = retrievedRecordedResponse(it.uid())
                        if (dataRes.isNotEmpty()) {
                            dataRes = reInvertResponse(dataRes)
                            if (dataRes != null) {
                                setText(dataRes, false)
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
                                    val value = generateCode(s.toString())
                                    createUpdateSectionValue(it.uid(), value)
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
                    setOnClickListener {
                        AppUtils().showDatePickerDialog(
                            context, editText, setMaxNow = max, setMinNow = false
                        )
                    }
                    setText(retrievedRecordedResponse(it.uid()))

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
                                createUpdateSectionValue(it.uid(), s.toString())
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
                val response = retrievedRecordedResponse(it.uid())
                if (response != null) {
                    if (response == "true") {
                        radioButtonYes.isChecked = true
                    }
                    if (response == "false") {
                        radioButtonNo.isChecked = true
                    }
                }

                radioButtonNo.apply {
                    setOnCheckedChangeListener { button, isChecked ->
                        if (isChecked) {
                            createUpdateSectionValue(it.uid(), "false")
                        }
                    }
                }
                radioButtonYes.apply {
                    setOnCheckedChangeListener { button, isChecked ->
                        if (isChecked) {
                            createUpdateSectionValue(it.uid(), "true")
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


                editText.apply {
                    setText(retrievedRecordedResponse(it.uid()))
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
                                createUpdateSectionValue(it.uid(), s.toString())
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


                editText.apply {
                    setText(retrievedRecordedResponse(it.uid()))
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
                                createUpdateSectionValue(it.uid(), s.toString())
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


                editText.apply {
                    setText(retrievedRecordedResponse(it.uid()))
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
                                createUpdateSectionValue(it.uid(), s.toString())
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
                val response = retrievedRecordedResponse(it.uid())
                if (response != null) {
                    checkBox.isChecked = response == "true"
                }

                checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        createUpdateSectionValue(it.uid(), "true")
                    } else {
                        createUpdateSectionValue(it.uid(), "false")
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


                editText.apply {
                    setText(retrievedRecordedResponse(it.uid()))
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
                                createUpdateSectionValue(it.uid(), s.toString())
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
                    setText(retrievedRecordedResponse(it.uid()))
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
                                createUpdateSectionValue(it.uid(), s.toString())
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

    private fun reInvertResponse(dataRes: String): String {
        return hashMapResponse[dataRes].toString()
    }

    private fun retrievedRecordedResponse(attributeUid: String): String {
        var response = ""
        val enrollmentUid = FormatterClass().getSharedPref(
            "enrollment_id",
            this@ResponderActivity
        )

        val programUid = FormatterClass().getSharedPref(
            "section_id",
            this@ResponderActivity
        )
        if (enrollmentUid != null && programUid != null) {
            val res = viewModel.getResponse(enrollmentUid, programUid, attributeUid)
            if (res != null) {
                response = res
            }
        }

        return response

    }

    private fun generateCode(value: String): String {
        return hashMap[value].toString()
    }

    private fun createUpdateSectionValue(attributeUid: String, dataValue: String) {
        val enrollmentUid = FormatterClass().getSharedPref(
            "enrollment_id",
            this@ResponderActivity
        )

        val programUid = FormatterClass().getSharedPref(
            "section_id",
            this@ResponderActivity
        )
        if (enrollmentUid != null && programUid != null) {
            val data = ProgramDataValues(
                enrollmentUid = enrollmentUid,
                programUid = programUid,
                attributeUid = attributeUid,
                dataValue = dataValue
            )
            viewModel.addResponse(this@ResponderActivity, data)
            calculateProgress()
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