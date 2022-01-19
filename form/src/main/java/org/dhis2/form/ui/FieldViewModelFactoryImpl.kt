package org.dhis2.form.ui

import androidx.databinding.ObservableField
import java.util.Arrays
import org.dhis2.commons.extensions.Preconditions.Companion.isNull
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.model.FieldUiModelImpl
import org.dhis2.form.model.LegendValue
import org.dhis2.form.model.SectionUiModelImpl
import org.dhis2.form.ui.event.UiEventFactoryImpl
import org.dhis2.form.ui.provider.DisplayNameProvider
import org.dhis2.form.ui.provider.HintProvider
import org.dhis2.form.ui.provider.KeyboardActionProvider
import org.dhis2.form.ui.provider.LayoutProvider
import org.dhis2.form.ui.provider.UiEventTypesProvider
import org.dhis2.form.ui.provider.UiStyleProvider
import org.hisp.dhis.android.core.common.FeatureType
import org.hisp.dhis.android.core.common.ObjectStyle
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.android.core.common.ValueTypeDeviceRendering
import org.hisp.dhis.android.core.common.ValueTypeRenderingType
import org.hisp.dhis.android.core.option.Option
import org.hisp.dhis.android.core.program.ProgramStageSectionRenderingType
import org.hisp.dhis.android.core.program.ProgramTrackedEntityAttribute
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute

class FieldViewModelFactoryImpl(
    private val valueTypeHintMap: Map<ValueType, String>,
    private val searchMode: Boolean,
    private val uiStyleProvider: UiStyleProvider,
    private val layoutProvider: LayoutProvider,
    private val hintProvider: HintProvider,
    private val displayNameProvider: DisplayNameProvider,
    private val uiEventTypesProvider: UiEventTypesProvider,
    private val keyboardActionProvider: KeyboardActionProvider
) : FieldViewModelFactory {
    private val currentSection = ObservableField("")
    private val optionSetTextRenderings = Arrays.asList(
        ValueTypeRenderingType.HORIZONTAL_CHECKBOXES,
        ValueTypeRenderingType.VERTICAL_CHECKBOXES,
        ValueTypeRenderingType.HORIZONTAL_RADIOBUTTONS,
        ValueTypeRenderingType.VERTICAL_RADIOBUTTONS
    )

    override fun create(
        id: String,
        label: String,
        valueType: ValueType,
        mandatory: Boolean,
        optionSet: String?,
        value: String?,
        programStageSection: String?,
        allowFutureDates: Boolean?,
        editable: Boolean,
        renderingType: ProgramStageSectionRenderingType?,
        description: String?,
        fieldRendering: ValueTypeDeviceRendering?,
        optionCount: Int?,
        objectStyle: ObjectStyle,
        fieldMask: String?,
        legendValue: LegendValue?,
        options: List<Option>?,
        featureType: FeatureType?
    ): FieldUiModel {
        var isMandatory = mandatory
        isNull(valueType, "type must be supplied")
        if (searchMode) isMandatory = false
        return FieldUiModelImpl(
            id,
            layoutProvider.getLayoutByType(
                valueType,
                if (fieldRendering != null) fieldRendering.type() else null,
                optionSet,
                renderingType
            ),
            value,
            false,
            null,
            editable,
            null,
            isMandatory,
            label,
            programStageSection,
            uiStyleProvider.provideStyle(valueType),
            hintProvider.provideDateHint(valueType),
            description,
            valueType,
            legendValue,
            optionSet,
            allowFutureDates,
            UiEventFactoryImpl(
                id,
                label,
                description,
                valueType,
                allowFutureDates,
                optionSet,
                editable
            ),
            displayNameProvider.provideDisplayName(valueType, value, optionSet),
            uiEventTypesProvider.provideUiRenderType(
                featureType,
                fieldRendering?.type(),
                renderingType
            ),
            options,
            keyboardActionProvider.provideKeyboardAction(valueType),
            fieldMask
        )
    }

    override fun createForAttribute(
        trackedEntityAttribute: TrackedEntityAttribute,
        programTrackedEntityAttribute: ProgramTrackedEntityAttribute?,
        value: String?,
        editable: Boolean,
        options: List<Option>?
    ): FieldUiModel {
        isNull(trackedEntityAttribute.valueType(), "type must be supplied")
        return create(
            id = trackedEntityAttribute.uid(),
            label = trackedEntityAttribute.displayFormName() ?: "",
            valueType = trackedEntityAttribute.valueType()!!,
            mandatory = programTrackedEntityAttribute?.mandatory() == true,
            optionSet = trackedEntityAttribute.optionSet()?.uid(),
            value = value,
            programStageSection = null,
            allowFutureDates = programTrackedEntityAttribute?.allowFutureDate() ?: true,
            editable = editable,
            renderingType = ProgramStageSectionRenderingType.LISTING,
            description = programTrackedEntityAttribute?.displayDescription()
                ?: trackedEntityAttribute.displayDescription(),
            fieldRendering = programTrackedEntityAttribute?.renderType()?.mobile(),
            optionCount = null,
            objectStyle = trackedEntityAttribute.style() ?: ObjectStyle.builder().build(),
            fieldMask = trackedEntityAttribute.fieldMask(),
            legendValue = null,
            options = options!!,
            featureType = if (trackedEntityAttribute.valueType() === ValueType.COORDINATE) {
                FeatureType.POINT
            } else null
        )
    }

    override fun createSingleSection(singleSectionName: String): FieldUiModel {
        return SectionUiModelImpl(
            SectionUiModelImpl.SINGLE_SECTION_UID,
            layoutProvider.getLayoutForSection(),
            null,
            false,
            null,
            false,
            null,
            false,
            singleSectionName,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            null,
            null,
            null,
            null,
            null,
            true,
            0,
            0,
            0,
            0,
            ProgramStageSectionRenderingType.LISTING.name,
            currentSection
        )
    }

    override fun createSection(
        sectionUid: String,
        sectionName: String?,
        description: String?,
        isOpen: Boolean,
        totalFields: Int,
        completedFields: Int,
        rendering: String?
    ): FieldUiModel {
        return SectionUiModelImpl(
            sectionUid,
            layoutProvider.getLayoutForSection(),
            null,
            false,
            null,
            false,
            null,
            false,
            sectionName ?: "",
            sectionUid,
            null,
            null,
            description,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            isOpen,
            totalFields,
            completedFields,
            0,
            0,
            rendering,
            currentSection
        )
    }

    override fun createClosingSection(): FieldUiModel {
        return SectionUiModelImpl(
            SectionUiModelImpl.Companion.CLOSING_SECTION_UID,
            layoutProvider.getLayoutForSection(),
            null,
            false,
            null,
            false,
            null,
            false,
            SectionUiModelImpl.Companion.CLOSING_SECTION_UID,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            0,
            0,
            0,
            0,
            ProgramStageSectionRenderingType.LISTING.name,
            currentSection
        )
    }

    private fun getLayout(type: Class<*>): Int {
        return layoutProvider.getLayoutByModel(type.kotlin)
    }
}
