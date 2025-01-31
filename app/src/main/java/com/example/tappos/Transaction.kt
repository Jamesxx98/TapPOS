package com.example.tappos

data class Transaction(
    val cardNumber: String,
    val createdAt: Any,
    val invoice: String,
    val recordId: Int,
    val updatedAt: Any
)