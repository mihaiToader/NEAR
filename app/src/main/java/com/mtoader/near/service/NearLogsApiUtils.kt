package com.mtoader.near.service

object NearLogsApiUtils {

    val BASE_URL = "http://192.168.0.103:8080"

    val apiService: NearLogsApi
        get() = RetrofitClient.getClient(BASE_URL).create(NearLogsApi::class.java)

    fun getApiService(baseUrl: String): NearLogsApi {
        return RetrofitClient.getClient(addHttp(baseUrl)).create(NearLogsApi::class.java)
    }

    private fun addHttp(baseUrl: String): String {
        if (!baseUrl.contains("http")) {
            return "http://$baseUrl"
        }
        return baseUrl
    }
}