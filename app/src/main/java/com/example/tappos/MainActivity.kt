package com.example.tappos

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Accessing button and image view by findViewById
        val btnGoToTransaction: Button = findViewById(R.id.btnGoToTransaction)
        val ivTransactionHistory: ImageView = findViewById(R.id.ivTransactionHistory)

        transactionService.getAll().enqueue(object : Callback<List<Transaction>>{
            override fun onResponse(
                call: Call<List<Transaction>>,
                response: Response<List<Transaction>>
            ) {
                if(response.isSuccessful()){
                    Log.d("Transaction api","transaction successful ${response.body()}")
                }else{
                    Log.e("Transaction api",response.code().toString())
                }
            }

            override fun onFailure(call: Call<List<Transaction>>, t: Throwable) {

            }

        })

        // Set click listener for button to navigate to TransactionActivity
        btnGoToTransaction.setOnClickListener {
            val intent = Intent(this, TransactionActivity::class.java)
            startActivity(intent)
        }

        // Set click listener for image view to navigate to TransactionHistoryActivity
        ivTransactionHistory.setOnClickListener {
            val intent = Intent(this, TransactionHistoryActivity::class.java)
            startActivity(intent)
        }


    }
}
