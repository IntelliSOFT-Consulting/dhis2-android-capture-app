package com.nacare.capture.ui.v2.live

import com.nacare.capture.models.OrganizationResponse
import com.nacare.capture.models.OrganizationUnitResponse

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path


interface Interface {
    @GET("/api/40/me.json?fields=id,username,surname,firstName,organisationUnits[name,id]")
    suspend fun loadOrganization(): Response<OrganizationResponse>
    @GET("/api/40/organisationUnits/{code}?fields=name,id,children[name,id,children[name,id,children[name,id,children[name,id,children]]]]")
    suspend fun loadChildUnits(@Path("code") code: String): Response<OrganizationUnitResponse>
}