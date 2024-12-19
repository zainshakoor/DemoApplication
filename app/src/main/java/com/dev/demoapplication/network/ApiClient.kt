package com.dev.demoapplication.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ApiClient {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://fu8e.space/")  // Base URL
        .addConverterFactory(GsonConverterFactory.create())
        .build()

        val apiService: ApiService = retrofit.create(ApiService::class.java)
}
