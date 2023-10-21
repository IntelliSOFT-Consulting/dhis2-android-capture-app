package com.nacare.capture.models

import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute

class DataClass {

}

data class CodeValue(
    val code: String,
    val value: String
)

data class FacilityProgramCategory(
    val dataElements: List<DataElement>?,
    val displayName: String,
    val id: String
)

data class ProgramCategory(
    val iconResId: Int?,
    val name: String,
    val id: String,
    val done: String?,
    val total: String?,
    val elements: List<ProgramStage>,
    val altElements: List<ProgramStage>?,
    val position: String
)

data class EventWithOrganization(val event: Event, val organizationUnit: OrganisationUnit)

data class Person(
    val patientId: String,
    val trackedEntityInstance: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val document: String,
    val attribute: List<TrackedEntityAttribute>
)