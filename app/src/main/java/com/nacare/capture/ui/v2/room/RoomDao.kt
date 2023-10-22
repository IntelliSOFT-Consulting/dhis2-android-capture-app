package com.nacare.capture.ui.v2.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RoomDao {
    @Query("SELECT EXISTS (SELECT 1 FROM organizations WHERE code =:code)")
    fun checkOrganizationExists(code: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addOrganization(data: OrganizationData)

    @Query("UPDATE organizations SET name =:name WHERE code =:code")
    fun updateOrganization(name: String, code: String)

    @Query("SELECT * FROM organizations ORDER BY id DESC")
    fun loadOrganizations(): List<OrganizationData>?


    @Query("UPDATE organizations SET children =:children WHERE  code =:code")
    fun updateChildOrgUnits(code: String, children: String)

    @Query("SELECT EXISTS (SELECT 1 FROM programDataValues WHERE enrollmentUid =:enrollmentUid AND programUid =:programUid AND attributeUid =:attributeUid)")
    fun checkResponse(enrollmentUid: String, programUid: String, attributeUid: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addResponse(data: ProgramDataValues)

    @Query("UPDATE programDataValues SET dataValue =:dataValue WHERE enrollmentUid =:enrollmentUid AND programUid =:programUid AND attributeUid =:attributeUid")
    fun updateResponse(
        enrollmentUid: String,
        programUid: String,
        attributeUid: String,
        dataValue: String
    )

    @Query("SELECT dataValue FROM programDataValues WHERE enrollmentUid =:enrollmentUid AND programUid =:programUid AND attributeUid =:attributeUid ")
    fun getResponse(enrollmentUid: String, programUid: String, attributeUid: String): String?
    @Query("SELECT COUNT(*) FROM programDataValues WHERE enrollmentUid =:enrollmentUid AND programUid =:programUid ")
    fun getResponseList(enrollmentUid: String, programUid: String):Int


}
