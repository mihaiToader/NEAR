package com.mtoader.near.service

object NearLogsApiUtils {

    val BASE_URL = "http://192.168.0.103:8080"

    val apiService: NearLogsApi
        get() = RetrofitClient.getClient(BASE_URL).create(NearLogsApi::class.java)
}