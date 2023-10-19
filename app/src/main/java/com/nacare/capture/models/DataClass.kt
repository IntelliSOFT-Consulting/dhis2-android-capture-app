package com.nacare.capture.models

import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.organisationunit.OrganisationUnit
import org.hisp.dhis.android.core.program.ProgramStage

class DataClass {

}
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
