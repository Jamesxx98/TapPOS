package com.example.tappos

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.TextView

class TransactionHistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction_history)

        val tvTransactionList: TextView = findViewById(R.id.tvTransactionList)

        tvTransactionList.text = "List of transactions will be displayed here."
    }
}
