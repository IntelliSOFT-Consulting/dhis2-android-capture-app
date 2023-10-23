package com.nacare.capture.adapters

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android.androidskeletonapp.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.data.service.SyncStatusHelper
import com.nacare.capture.models.OrganisationUnit
import com.nacare.capture.models.ProgramCategory
import com.nacare.capture.ui.v2.room.MainViewModel
import com.nacare.capture.utils.AppUtils
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttributeValue
import org.hisp.dhis.android.core.trackedentity.TrackedEntityInstance


class SummaryAdapter(
    private val context: Context,
    private val viewModel: MainViewModel,
    private val layoutInflater: LayoutInflater,
    private val items: List<ProgramCategory>
) :
    RecyclerView.Adapter<SummaryAdapter.ParentViewHolder>() {
    private val hashMap = mutableMapOf<String, String>()
    private val hashMapResponse = mutableMapOf<String, String>()
    private val parentViewType = 0
    private var expandedPosition: Int = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParentViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.item_program_summary, parent, false)
        return ParentViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ParentViewHolder, position: Int) {
        val event = items[position]
        holder.bind(event)
        holder.setExpanded(position == expandedPosition)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ParentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.eventTextView)
        private val progressTextView: TextView = itemView.findViewById(R.id.progressTextView)
        private val hiddenLayout: LinearLayout = itemView.findViewById(R.id.ln_parent)
        private val leftIconImageView: ImageView = itemView.findViewById(R.id.leftIconImageView)
        private val smallIconImageView: ImageView = itemView.findViewById(R.id.smallIconImageView)
        private val materialCardView: MaterialCardView =
            itemView.findViewById(R.id.materialCardView)

        init {
            materialCardView.setOnClickListener {
                handleTextViewClick()
            }
        }

        fun bind(item: ProgramCategory) {
            titleTextView.text = item.name
            progressTextView.text = item.id
            // You can bind other data for the parent item here
            try {
                val drawableResourceId = AppUtils().generateIcons(context, item.name)
                leftIconImageView.setImageResource(drawableResourceId)
            } catch (e: Exception) {

            }

        }

        fun setExpanded(expanded: Boolean) {
            hiddenLayout.visibility = if (expanded) View.VISIBLE else View.GONE
            smallIconImageView.setImageResource(if (expanded) R.drawable.baseline_keyboard_arrow_up_24 else R.drawable.baseline_keyboard_arrow_down_24)
            // You may want to add additional logic to handle expanded state
            if (hiddenLayout.visibility == View.VISIBLE) {
                val id = progressTextView.text.toString()
                if (id == "patient-details") {
                    val enroll = FormatterClass().getSharedPref(
                        "enrollment_id",
                        context
                    )
                    val dataUser = SyncStatusHelper.getSingleEnrollment(enroll)
                    val user = SyncStatusHelper.getTrackedEntity(dataUser.trackedEntityInstance())
                    if (user != null) {
                        populatePatientViews(hiddenLayout,layoutInflater, user)
                    }
                } else {
                    val dataEnrollment =
                        SyncStatusHelper().getProgramStageSections(progressTextView.text.toString())
                    dataEnrollment.forEach {
                        it.dataElements()!!.forEach { k ->
                            populateViews(
                                hiddenLayout,
                                layoutInflater,
                                k,
                                progressTextView.text.toString()
                            )
                        }
                    }
                }
            }

        }

        private fun handleTextViewClick() {
            val expanded = adapterPosition == expandedPosition
            expandedPosition = if (expanded) {
                RecyclerView.NO_POSITION
            } else {
                adapterPosition
            }
            notifyDataSetChanged() // Notify the adapter to refresh the views
        }
    }


    private fun populatePatientViews(lnParent: LinearLayout,  layoutInflater: LayoutInflater, user: TrackedEntityInstance) {
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

                        editText.apply {
                            if (user != null) {
                                setText(retrievedRecordedPatientResponse(user.uid(), it.uid()))
                            }

                        }
                    } else {
                        val itemView = layoutInflater.inflate(
                            R.layout.item_autocomplete,
                            lnParent,
                            false
                        ) as LinearLayout
                        val optionsList: MutableList<String> = mutableListOf()
                        val adp = ArrayAdapter(
                            context,
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
//                        autoCompleteTextView.setAdapter(adp)
                        adp.notifyDataSetChanged()


                        autoCompleteTextView.apply {
                            if (user != null) {
                                var dataRes =
                                    retrievedRecordedPatientResponse(user.uid(), it.uid())
                                if (dataRes.isNotEmpty()) {
                                    dataRes = reInvertResponse(dataRes)
                                    if (dataRes != null) {
                                        setText(dataRes, false)
                                    }
                                }
                            }

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
                            setText(retrievedRecordedPatientResponse(user.uid(), it.uid()))
                        }
                        setOnClickListener {
                            AppUtils().showDatePickerDialog(
                                context, editText, setMaxNow = max, setMinNow = false
                            )
                        }

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
                        val dataValue = retrievedRecordedPatientResponse(user.uid(), it.uid())
                        if (dataValue == "true") {
                            radioButtonYes.isChecked = true
                        }
                        if (dataValue == "false") {
                            radioButtonNo.isChecked = true
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
                        if (user != null) {
                            setText(retrievedRecordedPatientResponse(user.uid(), it.uid()))
                        }

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
                        if (user != null) {
                            setText(retrievedRecordedPatientResponse(user.uid(), it.uid()))
                        }

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
                        if (user != null) {
                            setText(retrievedRecordedPatientResponse(user.uid(), it.uid()))
                        }

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
                        val dataValue = retrievedRecordedPatientResponse(user.uid(), it.uid())
                        if (dataValue == "true") {
                            checkBox.isChecked = true
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

                    editText.apply {
                        if (user != null) {
                            when {
                                currentAttribute.uid() == "uR2Mnlh7sqn" -> {
                                    val test = retrievedRecordedPatientResponse(user.uid(), it.uid())
                                    if (test.isNotEmpty()) {
                                        val county = SyncStatusHelper.loadResidence(2, test)
                                        setText("${county.displayName()}")
                                    }
                                }

                                currentAttribute.uid() == "e9e7MiIbpfc" -> {
                                    val test = retrievedRecordedPatientResponse(user.uid(), it.uid())
                                    if (test.isNotEmpty()) {
                                        val county = SyncStatusHelper.loadResidence(3, test)
                                        setText("${county.displayName()}")
                                    }
                                }

                                currentAttribute.uid() == "ud36eYLaM3d" -> {
                                    val test = retrievedRecordedPatientResponse(user.uid(), it.uid())
                                    if (test.isNotEmpty()) {
                                        val county = SyncStatusHelper.loadResidence(4, test)
                                        setText("${county.displayName()}")
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
                            setText(retrievedRecordedPatientResponse(user.uid(), it.uid()))
                        }

                    }
                    lnParent.addView(itemView)
                }

                else -> {}
            }
        }


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


    private fun populateViews(
        lnParent: LinearLayout,
        layoutInflater: LayoutInflater,
        it: DataElement, programUid: String
    ) {
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
                        isEnabled = false
                        setText(retrievedRecordedResponse(it.uid(), programUid))
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

                } else {
                    val itemView = layoutInflater.inflate(
                        R.layout.item_autocomplete,
                        lnParent,
                        false
                    ) as LinearLayout
                    val optionsList: MutableList<String> = mutableListOf()
                    val adp = ArrayAdapter(
                        context,
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
//                    autoCompleteTextView.setAdapter(adp)
                    autoCompleteTextView.isEnabled = false
                    adp.notifyDataSetChanged()

                    autoCompleteTextView.onItemClickListener =
                        AdapterView.OnItemClickListener { _, _, _, _ ->
                            // Prevent selection when the AutoCompleteTextView is disabled
                            if (!autoCompleteTextView.isEnabled) {
                                autoCompleteTextView.dismissDropDown()
                            }
                        }
                    autoCompleteTextView.apply {
                        isEnabled = false
                        var dataRes = retrievedRecordedResponse(it.uid(), programUid)
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
                    isEnabled = false
                    setOnClickListener {
                        AppUtils().showDatePickerDialog(
                            context, editText, setMaxNow = max, setMinNow = false
                        )
                    }
                    setText(retrievedRecordedResponse(it.uid(), programUid))

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
                val response = retrievedRecordedResponse(it.uid(), programUid)
                if (response != null) {
                    if (response == "true") {
                        radioButtonYes.isChecked = true
                    }
                    if (response == "false") {
                        radioButtonNo.isChecked = true
                    }
                }

                radioButtonNo.apply {
                    isEnabled = false
                    setOnCheckedChangeListener { button, isChecked ->
                        if (isChecked) {
                            createUpdateSectionValue(it.uid(), "false")
                        }
                    }
                }
                radioButtonYes.apply {
                    isEnabled = false
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
                    isEnabled = false
                    setText(retrievedRecordedResponse(it.uid(), programUid))
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
                    isEnabled = false
                    setText(retrievedRecordedResponse(it.uid(), programUid))
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
                    isEnabled = false
                    setText(retrievedRecordedResponse(it.uid(), programUid))
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
                val response = retrievedRecordedResponse(it.uid(), programUid)
                if (response != null) {
                    checkBox.isChecked = response == "true"
                }
                checkBox.isEnabled = false

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
                    isEnabled = false
                    setText(retrievedRecordedResponse(it.uid(), programUid))
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
                    isEnabled = false
                    setText(retrievedRecordedResponse(it.uid(), programUid))
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
    private fun retrievedRecordedPatientResponse(entityUid: String, attributeUid: String): String {
        var response = ""
        val res = viewModel.getRecordedResponse(entityUid, attributeUid)
        if (res != null) {
            response = res
        }
        return response


    }
    private fun retrievedRecordedResponse(attributeUid: String, programUid: String): String {
        var response = ""
        val enrollmentUid = FormatterClass().getSharedPref(
            "enrollment_id",
            context
        )

        if (enrollmentUid != null) {
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

    }
}
