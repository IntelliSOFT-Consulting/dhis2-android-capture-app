package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import org.dhis2.commons.extensions.toDate
import org.dhis2.commons.resources.ResourceManager
import org.dhis2.form.R
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.event.RecyclerViewUiEvents
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.AgeInputType
import org.hisp.dhis.mobile.ui.designsystem.component.InputAge
import org.hisp.dhis.mobile.ui.designsystem.component.TimeUnitValues
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ProvideInputAge(
    modifier: Modifier,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    uiEventHandler: (RecyclerViewUiEvents) -> Unit,
    resources: ResourceManager,
) {
    var inputType by remember(fieldUiModel.value) {
        mutableStateOf(
            if (!fieldUiModel.value.isNullOrEmpty()) {
                formatStoredDateToUI(fieldUiModel.value!!)?.let {
                    AgeInputType.DateOfBirth(it)
                } ?: AgeInputType.None
            } else {
                AgeInputType.None
            },
        )
    }

    DisposableEffect(fieldUiModel.value) {
        inputType = if (fieldUiModel.value.isNullOrEmpty()) {
            AgeInputType.None
        } else {
            when (inputType) {
                is AgeInputType.Age ->
                    calculateAgeFromDate(
                        fieldUiModel.value!!,
                        (inputType as AgeInputType.Age).unit,
                    )?.let {
                        (inputType as AgeInputType.Age).copy(value = it)
                    } ?: AgeInputType.None

                is AgeInputType.DateOfBirth ->
                    formatStoredDateToUI(fieldUiModel.value!!)?.let {
                        (inputType as AgeInputType.DateOfBirth).copy(value = it)
                    } ?: AgeInputType.None

                AgeInputType.None -> inputType
            }
        }
        onDispose { }
    }

    InputAge(
        title = fieldUiModel.label,
        inputType = inputType,
        onCalendarActionClicked = {
            uiEventHandler.invoke(
                RecyclerViewUiEvents.OpenCustomCalendar(
                    uid = fieldUiModel.uid,
                    label = fieldUiModel.label,
                    date = fieldUiModel.value?.toDate(),
                    allowFutureDates = fieldUiModel.allowFutureDates ?: false,
                ),
            )
        },
        modifier = modifier,
        state = fieldUiModel.inputState(),
        supportingText = fieldUiModel.supportingText(),
        isRequired = fieldUiModel.mandatory,
        dateOfBirthLabel = resources.getString(R.string.date_birth),
        orLabel = resources.getString(R.string.or),
        ageLabel = resources.getString(R.string.age),
        onValueChanged = { ageInputType ->
            inputType = ageInputType
            when (val type = inputType) {
                is AgeInputType.Age -> {
                    calculateDateFromAge(type)?.let { calculatedDate ->
                        intentHandler.invoke(
                            FormIntent.OnTextChange(
                                fieldUiModel.uid,
                                calculatedDate,
                                fieldUiModel.valueType,
                            ),
                        )
                    }
                }

                is AgeInputType.DateOfBirth -> {
                    if (isValidDate(type.value)) {
                        saveValue(
                            intentHandler,
                            fieldUiModel.uid,
                            formatUIDateToStored(type.value),
                            fieldUiModel.valueType,
                            fieldUiModel.allowFutureDates,
                        )
                    }
                }

                AgeInputType.None -> {
                    saveValue(
                        intentHandler,
                        fieldUiModel.uid,
                        null,
                        fieldUiModel.valueType,
                        fieldUiModel.allowFutureDates,
                    )
                }
            }
        },
    )
}

private fun saveValue(
    intentHandler: (FormIntent) -> Unit,
    uid: String,
    value: String?,
    valueType: ValueType?,
    allowFutureDates: Boolean?,
) {
    intentHandler.invoke(
        FormIntent.OnSaveDate(
            uid,
            value,
            valueType,
            allowFutureDates ?: false,
        ),
    )
}

private fun formatStoredDateToUI(inputDateString: String): String? {
    val components = inputDateString.split("-")
    if (components.size != 3) {
        return null
    }

    val year = components[0]
    val month = components[1]
    val day = components[2]

    return "$day$month$year"
}

private fun formatUIDateToStored(inputDateString: String): String? {
    if (inputDateString.length != 8) {
        return null
    }

    val year = inputDateString.substring(4, 8)
    val month = inputDateString.substring(2, 4)
    val day = inputDateString.substring(0, 2)

    return "$year-$month-$day"
}

private fun isValidDate(dateString: String) = dateString.length == 8

private fun calculateDateFromAge(age: AgeInputType.Age): String? {
    val calendar = Calendar.getInstance()
    return try {
        when (age.unit) {
            TimeUnitValues.YEARS -> calendar.add(Calendar.YEAR, -age.value.toInt())
            TimeUnitValues.MONTHS -> calendar.add(Calendar.MONTH, -age.value.toInt())
            TimeUnitValues.DAYS -> calendar.add(Calendar.DAY_OF_MONTH, -age.value.toInt())
        }

        val dateFormat = SimpleDateFormat(DB_FORMAT, Locale.getDefault())
        dateFormat.format(calendar.time)
    } catch (e: Exception) {
        null
    }
}

private fun calculateAgeFromDate(dateString: String, timeUnit: TimeUnitValues): String? {
    return try {
        val inputFormat = SimpleDateFormat(DB_FORMAT, Locale.getDefault())

        val birthDate = inputFormat.parse(dateString)
        val calendarBirthDate = Calendar.getInstance()
        calendarBirthDate.time = birthDate

        val currentDate = Date()
        val calendarCurrentDate = Calendar.getInstance()
        calendarCurrentDate.time = currentDate

        when (timeUnit) {
            TimeUnitValues.YEARS -> {
                var diff = calendarCurrentDate[Calendar.YEAR] - calendarBirthDate[Calendar.YEAR]
                if (calendarCurrentDate[Calendar.DAY_OF_YEAR] < calendarBirthDate[Calendar.DAY_OF_YEAR]) {
                    diff--
                }
                diff.toString()
            }

            TimeUnitValues.MONTHS -> {
                var diff = calendarCurrentDate[Calendar.MONTH] - calendarBirthDate[Calendar.MONTH]
                if (calendarCurrentDate[Calendar.DAY_OF_YEAR] < calendarBirthDate[Calendar.DAY_OF_YEAR]) {
                    diff--
                }
                diff.toString()
            }

            TimeUnitValues.DAYS -> {
                var diff = currentDate.time - birthDate.time
                diff /= (1000 * 60 * 60 * 24)
                diff.toString()
            }
        }
    } catch (e: Exception) {
        null
    }
}

private const val UI_FORMAT = "ddMMyyyy"
private const val DB_FORMAT = "yyyy-MM-dd"
