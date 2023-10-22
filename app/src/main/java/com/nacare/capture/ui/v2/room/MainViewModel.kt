package com.nacare.capture.ui.v2.room
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.runBlocking

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: MainRepository

    init {
        val roomDao = MainDatabase.getDatabase(application).roomDao()
        repository = MainRepository(roomDao)
    }

    fun addOrganization(context: Context, data: OrganizationData) {
        repository.addOrganization(context, data)
    }

    fun loadOrganizations(context: Context) = runBlocking {
        repository.loadOrganizations(context)
    }

    fun updateChildOrgUnits(context: Context, code: String, children: String) = runBlocking {
        repository.updateChildOrgUnits(context, code, children)
    }

    fun addResponse(context: Context, data: ProgramDataValues)= runBlocking {
        repository.addResponse(context, data)
    }

    fun getResponse(enrollmentUid: String, programUid: String, attributeUid: String)= runBlocking {
        repository.getResponse(enrollmentUid, programUid,attributeUid)
    }

    fun getResponseList(enrollmentUid: String, programUid: String)= runBlocking {
        repository.getResponseList(enrollmentUid, programUid)
    }

    fun addUpdateTrackedEntity(tracked: TrackedEntityValues) = runBlocking{
        repository.addUpdateTrackedEntity(tracked)
    }

    fun getRecordedResponse(entityUid: String, attributeUid: String)= runBlocking{
        repository.getRecordedResponse(entityUid,attributeUid)
    }

    fun getTrackedAttributeResponses(entityUid: String)= runBlocking {
        repository.getTrackedAttributeResponses(entityUid)
    }

    fun deleteTrackedEntity(entityUid: String, attributeUid: String) = runBlocking{
        repository.deleteTrackedEntity(entityUid,attributeUid)
    }


}
