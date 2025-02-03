package com.example.tappos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class TransactionHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        val tvTransactionHistoryTitle: TextView = findViewById(R.id.tvTransactionHistoryTitle)

        tvTransactionHistoryTitle.text = "Transaction Details"
    }
}

