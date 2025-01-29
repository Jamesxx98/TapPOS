package com.example.tappos

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.tappos.GlobalFunctions.Channel.sendOnline
import com.example.tappos.ISOMessages.BuildISOMessages.buildCustomMessage
import com.example.tappos.ISOMessages.CustomISOPackager
import com.github.devnied.emvnfccard.model.EmvCard
import com.github.devnied.emvnfccard.parser.EmvTemplate
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import org.jpos.iso.ISOMsg
import java.io.IOException
import java.time.LocalDate
import java.time.ZoneId

class TransactionActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private var mNfcAdapter: NfcAdapter? = null
    private lateinit var etAmount: TextInputEditText
    private lateinit var tvTransactionStatus: MaterialTextView
    private lateinit var btnPay: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        // Initialize views
        etAmount = findViewById(R.id.etAmount)
        tvTransactionStatus = findViewById(R.id.tvTransactionStatus)
        btnPay = findViewById(R.id.btnPay)

        // Initialize NFC Adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Set click listener for Pay button although i wanted a progress bar :)
        btnPay.setOnClickListener {
            val amount = etAmount.text.toString()
            if (amount.isNotEmpty()) {
                tvTransactionStatus.text = "Ready to scan NFC card..."
            } else {
                tvTransactionStatus.text = "Please enter an amount."
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mNfcAdapter != null) {
            val options = Bundle()
            options.putInt(NfcAdapter.EXTRA_READER_PRESENCE_CHECK_DELAY, 250)

            mNfcAdapter?.enableReaderMode(
                this,
                this,
                NfcAdapter.FLAG_READER_NFC_A or
                        NfcAdapter.FLAG_READER_NFC_B or
                        NfcAdapter.FLAG_READER_NFC_F or
                        NfcAdapter.FLAG_READER_NFC_V or
                        NfcAdapter.FLAG_READER_NFC_BARCODE or
                        NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
                options
            )
        }
    }

    override fun onPause() {
        super.onPause()
        mNfcAdapter?.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        val isoDep = IsoDep.get(tag)
        try {
            isoDep?.connect()
            if (isoDep != null) {
                // Vibration on successful scan
                (getSystemService(VIBRATOR_SERVICE) as Vibrator).vibrate(
                    VibrationEffect.createOneShot(150, 10)
                )
            }

            val provider = PcscProvider()
            provider.setmTagCom(isoDep!!)

            val config = EmvTemplate.Config()
                .setContactLess(true)
                .setReadAllAids(true)
                .setReadTransactions(true)
                .setRemoveDefaultParsers(false)
                .setReadAt(true)

            val parser = EmvTemplate.Builder()
                .setProvider(provider)
                .setConfig(config)
                .build()

            // Read the EMV card data
            val card: EmvCard? = parser.readEmvCard()
            card?.let {
                val cardNumber = it.cardNumber
                Log.d("PaymentResultCardNumber: ", cardNumber)

                val expireDate = it.expireDate
                val date = expireDate?.toInstant()
                    ?.atZone(ZoneId.systemDefault())
                    ?.toLocalDate() ?: LocalDate.of(1999, 12, 31)
                Log.d("PaymentResultDate: ", date.toString())

                // Get the amount entered by the user
                val amount = etAmount.text.toString()

                val isoMsg = ISOMsg()
                val packager = CustomISOPackager()
                isoMsg.packager = packager

                buildCustomMessage(isoMsg, cardNumber, date.toString(), amount)

                val response = sendOnline(isoMsg, packager)

                if (response != null && response.getString("39") == "00") {
                    // Start the TransactionDetailsActivity with the transaction data
                    val intent = Intent(this, TransactionDetailsActivity::class.java).apply {
                        putExtra("CARD_NUMBER", cardNumber)
                        putExtra("CARD_EXPIRY", date.toString())
                        putExtra("AMOUNT", amount)
                        putExtra("STATUS", "Payment successful")
                    }
                    startActivity(intent)

                    // Update the transaction status in the UI
                    runOnUiThread {
                        tvTransactionStatus.text = "Payment successful!\nCard: $cardNumber\nExpiry: $date"
                    }
                } else {
                    // Start the TransactionDetailsActivity with the transaction data
                    val intent = Intent(this, TransactionDetailsActivity::class.java).apply {
                        putExtra("CARD_NUMBER", cardNumber)
                        putExtra("CARD_EXPIRY", date.toString())
                        putExtra("AMOUNT", amount)
                        putExtra("STATUS", "Payment failed")
                    }
                    startActivity(intent)

                    // Update the transaction status in the UI
                    runOnUiThread {
                        tvTransactionStatus.text = "Payment failed!\nCard: $cardNumber\nExpiry: $date"
                    }
                }

            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                isoDep?.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}