package com.nacare.capture.models

import android.os.Parcel
import android.os.Parcelable
import org.hisp.dhis.android.core.dataelement.DataElement
import org.hisp.dhis.android.core.event.Event
import org.hisp.dhis.android.core.program.ProgramStage
import org.hisp.dhis.android.core.trackedentity.TrackedEntityAttribute

class DataClass {

}

data class CodeValue(val attributeUid: String, val value: String) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(attributeUid)
        parcel.writeString(value)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CodeValue> {
        override fun createFromParcel(parcel: Parcel): CodeValue {
            return CodeValue(parcel)
        }

        override fun newArray(size: Int): Array<CodeValue?> {
            return arrayOfNulls(size)
        }
    }
}

data class FacilityProgramCategory(
    val dataElements: List<DataElement>?,
    val displayName: String,
    val id: String
)

data class OrganizationResponse(

    val id: String,
    val username: String,
    val surname: String,
    val firstName: String,
    val organisationUnits: List<OrganisationUnit>
)

data class OrganisationUnit(val id: String, val name: String)
data class OrgTreeNode(
    val label: String, val code: String,
    val children: List<OrgTreeNode> = emptyList(),
    var isExpanded: Boolean = false
)

data class OrganizationUnitResponse(
    val name: String,
    val children: List<CountyUnit>,
    val id: String
)

data class CountyUnit(
    val name: String,
    val children: List<CountyUnit>,
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