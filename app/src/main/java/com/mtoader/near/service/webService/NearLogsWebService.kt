package com.mtoader.near.service.webService

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path


interface NearLogsWebService {

    @GET("/free/sayHello/{name}")
    fun sayHello(@Path("name") hello: String): Call<String>

}