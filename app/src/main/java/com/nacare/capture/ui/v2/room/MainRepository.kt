package com.nacare.capture.ui.v2.room

import android.content.Context
import com.google.gson.Gson
import com.nacare.capture.data.FormatterClass

class MainRepository(private val roomDao: RoomDao) {
    private val formatterClass = FormatterClass()
    fun addOrganization(context: Context, data: OrganizationData) {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            //if it exists update
            val exists = roomDao.checkOrganizationExists(data.code)
            if (!exists) {
                roomDao.addOrganization(data)
            } else {
                roomDao.updateOrganization(data.name, data.code)
            }
        }

    }

    fun loadOrganizations(context: Context): List<OrganizationData>? {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            return roomDao.loadOrganizations()
        }
        return emptyList()
    }

    fun updateChildOrgUnits(context: Context, code: String, children: String) {
        val userId = formatterClass.getSharedPref("username", context)
        if (userId != null) {
            roomDao.updateChildOrgUnits(code, children)
        }
    }

    fun addResponse(context: Context, data: ProgramDataValues) {
        val exists = roomDao.checkResponse(data.enrollmentUid, data.programUid, data.attributeUid)
        if (!exists) {
            roomDao.addResponse(data)
        } else {
            roomDao.updateResponse(
                data.enrollmentUid,
                data.programUid,
                data.attributeUid,
                data.dataValue
            )
        }
    }

    fun getResponse(enrollmentUid: String, programUid: String, attributeUid: String): String? {
        return roomDao.getResponse(enrollmentUid, programUid, attributeUid)
    }

    fun getResponseList(enrollmentUid: String, programUid: String): Int {
        return roomDao.getResponseList(enrollmentUid, programUid)
    }

}
