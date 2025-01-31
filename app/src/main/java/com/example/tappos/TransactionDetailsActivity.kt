package com.example.tappos

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class TransactionDetailsActivity : AppCompatActivity() {

    private lateinit var tvCardNumber: TextView
    private lateinit var tvCardExpiry: TextView
    private lateinit var tvTransactionAmount: TextView
    private lateinit var tvTransactionStatus: TextView
    private lateinit var btnBackToMain: Button

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_details)

        // Initialize views
        tvCardNumber = findViewById(R.id.tvCardNumber)
        tvCardExpiry = findViewById(R.id.tvCardExpiry)
        tvTransactionAmount = findViewById(R.id.tvTransactionAmount)
        tvTransactionStatus = findViewById(R.id.tvTransactionStatus)
        btnBackToMain = findViewById(R.id.btnBackToMain)

        // Get transaction data from Intent
        val cardNumber = intent.getStringExtra("CARD_NUMBER")
        val expireDate = intent.getStringExtra("CARD_EXPIRY")
        val amount = intent.getStringExtra("AMOUNT")
        val statusMessage = intent.getStringExtra("STATUS")

        // Set values to the TextViews
        tvCardNumber.text = "Card Number: $cardNumber"
        tvCardExpiry.text = "Card Expiry: $expireDate"
        tvTransactionAmount.text = "Amount: $amount"
        tvTransactionStatus.text = "Transaction Status: $statusMessage"

        // Set back button functionality
        btnBackToMain.setOnClickListener {
            finish()
        }
    }
}
