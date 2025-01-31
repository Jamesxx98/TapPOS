package com.example.tappos

import retrofit2.Call
import retrofit2.http.GET

interface TransactionService {
    @GET("getAll")
    fun getAll(): Call<List<Transaction>>
}