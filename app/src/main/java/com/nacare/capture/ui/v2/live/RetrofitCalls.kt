package com.nacare.capture.ui.v2.live

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.nacare.capture.data.Constants.BASE_URL
import com.nacare.capture.data.FormatterClass
import com.nacare.capture.ui.v2.live.room.Converters
import com.nacare.capture.ui.v2.live.room.MainViewModel
import com.nacare.capture.ui.v2.live.room.OrganizationData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class RetrofitCalls {
    fun loadOrganization(context: Context) {

        CoroutineScope(Dispatchers.IO).launch {
            val viewModel = MainViewModel(context.applicationContext as Application)
            val formatterClass = FormatterClass()
            val baseUrl = BASE_URL
            val username = formatterClass.getSharedPref("username", context)
            if (baseUrl != null && username != null) {
                val apiService =
                    RetrofitBuilder.getRetrofit(context, baseUrl).create(Interface::class.java)
                try {
                    val apiInterface = apiService.loadOrganization()
                    if (apiInterface.isSuccessful) {
                        val statusCode = apiInterface.code()
                        val body = apiInterface.body()
                        if (statusCode == 200 || statusCode == 201) {
                            if (body != null) {
                                val converters = Converters().toJsonOrganization(body)
                                try {
                                    val json = Gson().fromJson(converters, JsonObject::class.java)
                                    val data = json.getAsJsonArray("organisationUnits")
                                    data.forEach {
                                        if (it is JsonObject) {
                                            val code = it.get("id").asString
                                            val name = it.get("name").asString
                                            val org = OrganizationData(
                                                name = name,
                                                code = code,
                                                children = ""
                                            )
                                            viewModel.addOrganization(context, org)
                                            handleChildOrganizationUnits(context, code)
                                        }

                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Log.e("TAG", "json:::: ${e.message}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    print(e)
                }
            }
        }
    }

    private fun handleChildOrganizationUnits(context: Context, code: String) {

        CoroutineScope(Dispatchers.IO).launch {
            val viewModel = MainViewModel(context.applicationContext as Application)
            val formatterClass = FormatterClass()
            val baseUrl = BASE_URL

            val username = formatterClass.getSharedPref("username", context)
            if (baseUrl != null && username != null) {
                val apiService =
                    RetrofitBuilder.getRetrofit(context, baseUrl).create(Interface::class.java)
                try {
                    val apiInterface = apiService.loadChildUnits(code)
                    if (apiInterface.isSuccessful) {
                        val statusCode = apiInterface.code()
                        val body = apiInterface.body()
                        if (statusCode == 200 || statusCode == 201) {
                            if (body != null) {
                                val converters = Converters().toJsonOrgUnit(body)
                                try {
                                    val json = Gson().fromJson(converters, JsonObject::class.java)
                                    viewModel.updateChildOrgUnits(context, code, json.toString())
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    Log.e("TAG", "child units error:::: ${e.message}")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    print(e)
                }
            }
        }
    }
}