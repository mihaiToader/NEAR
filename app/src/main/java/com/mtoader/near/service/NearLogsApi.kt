package com.mtoader.near.service

import com.mtoader.near.model.dto.LogDto
import com.mtoader.near.model.dto.AuthorizationToken
import com.mtoader.near.model.dto.NearLogsUser
import retrofit2.Call
import retrofit2.http.*


interface NearLogsApi {

    @POST("login")
    fun makeLogin(@Body nearLogsUser: NearLogsUser): Call<AuthorizationToken>

    @POST("api/log/add")
    fun addLog(@Header("Authorization") authorization: String, @Body log: LogDto): Call<Void>

    @GET("/free/sayHello/{name}")
    fun sayHello(@Path("name") name:String): Call<String>
}