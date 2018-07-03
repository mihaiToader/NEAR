package com.mtoader.near.service

import com.mtoader.near.model.dto.*
import retrofit2.Call
import retrofit2.http.*


interface NearLogsApi {

    @POST("login")
    fun makeLogin(@Body nearLogsUser: NearLogsUser): Call<AuthorizationToken>

    @POST("api/log/add")
    fun addLog(@Header("Authorization") authorization: String, @Body log: LogDto): Call<Void>

    @GET("/free/sayHello/{name}")
    fun sayHello(@Path("name") name:String): Call<String>

    @POST("api/graph/setSource")
    fun setSource(@Header("Authorization") authorization: String, @Body node: NodeDto): Call<Void>

    @POST("api/graph/addEdge")
    fun addEdge(@Header("Authorization") authorization: String, @Body edge: EdgeWithNodesDto): Call<Void>

    @POST("api/graph/removeNode")
    fun removeNode(@Header("Authorization") authorization: String, @Body node: NodeDto): Call<Void>

    @POST("api/graph/clearGraph/{device}/{session}")
    fun clearGraph(@Header("Authorization") authorization: String, @Path("device") device:String, @Path("session") session:String): Call<Void>

    @POST("api/payload/addPayload")
    fun addPayload(@Header("Authorization") authorization: String, @Body payload: PayloadDto): Call<Void>

    @POST("api/payload/setHistory/{device}/{session}")
    fun setHistory(@Header("Authorization") authorization: String, @Path("device") device:String, @Path("session") session:String): Call<Void>
}