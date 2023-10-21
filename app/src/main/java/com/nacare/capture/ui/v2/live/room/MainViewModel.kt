package com.nacare.capture.ui.v2.live.room

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

}
