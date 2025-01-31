package com.example.tappos

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val api = Retrofit.Builder()
    .baseUrl("https://69e7-41-220-228-218.ngrok-free.app/api/transactions/")
    .addConverterFactory(GsonConverterFactory.create())
    .build()

val transactionService = api.create(TransactionService::class.java)



