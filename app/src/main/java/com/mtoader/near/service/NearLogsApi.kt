package com.mtoader.near.service

import com.mtoader.near.model.dto.Token
import com.mtoader.near.model.dto.NearLogsUser
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface NearLogsApi {

    @POST("login")
    fun makeLogin(@Body nearLogsUser: NearLogsUser): Call<Token>

    @GET("/free/sayHello/{name}")
    fun sayHello(@Path("name") name:String): Call<String>
}