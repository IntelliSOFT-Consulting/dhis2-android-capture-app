package com.nacare.capture.ui.v2.live.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.nacare.capture.models.OrganizationResponse
import com.nacare.capture.models.OrganizationUnitResponse
import java.util.Date

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toJsonOrganization(data: OrganizationResponse): String {
        return gson.toJson(data)
    }

    fun toJsonOrgUnit(data: OrganizationUnitResponse): String {
        return gson.toJson(data)
    }

    @TypeConverter
    fun fromJsonOrgUnit(json: String): OrganizationUnitResponse {
        // convert json to MyJsonData object
        return gson.fromJson(json, OrganizationUnitResponse::class.java)

    }

  /*  fun toJsonPatientSearch(data: SearchPatientResponse): String {
        return gson.toJson(data)
    }*/

}