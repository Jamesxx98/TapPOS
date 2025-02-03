package com.example.tappos

import android.annotation.SuppressLint
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.IsoDep
import android.os.*
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
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors

class TransactionActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private var mNfcAdapter: NfcAdapter? = null
    private lateinit var etAmount: TextInputEditText
    private lateinit var tvTransactionStatus: MaterialTextView
    private lateinit var btnPay: MaterialButton
    private val executorService = Executors.newSingleThreadExecutor()  // Runs NFC processing in a separate thread

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_transaction)

        etAmount = findViewById(R.id.etAmount)
        tvTransactionStatus = findViewById(R.id.tvTransactionStatus)
        btnPay = findViewById(R.id.btnPay)

        // Initialize NFC adapter
        mNfcAdapter = NfcAdapter.getDefaultAdapter(this)

        // Check if NFC is supported and enabled
        checkNfcSupportAndEnable()

        btnPay.setOnClickListener {
            val amount = etAmount.text.toString()
            tvTransactionStatus.text = if (amount.isNotEmpty()) "Ready to scan NFC card..." else "Please enter an amount."
        }
    }

    // Function to check if NFC is supported and enabled
    private fun checkNfcSupportAndEnable() {
        if (mNfcAdapter == null) {
            // Device does not support NFC
            tvTransactionStatus.text = "NFC is not supported on this device. Please use a device with NFC capability."
            btnPay.isEnabled = false // Disable payment button
        } else {
            if (!mNfcAdapter!!.isEnabled) {
                // NFC is disabled, prompt user to enable it
                tvTransactionStatus.text = "NFC is not enabled. Please enable NFC in your device's settings."
                btnPay.isEnabled = false // Disable payment button
            } else {
                // NFC is supported and enabled
                tvTransactionStatus.text = "Ready to scan NFC card."
                btnPay.isEnabled = true // Enable payment button
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mNfcAdapter?.enableReaderMode(
            this,
            this,
            NfcAdapter.FLAG_READER_NFC_A or
                    NfcAdapter.FLAG_READER_NFC_B or
                    NfcAdapter.FLAG_READER_NFC_F or
                    NfcAdapter.FLAG_READER_NFC_V or
                    NfcAdapter.FLAG_READER_NFC_BARCODE or
                    NfcAdapter.FLAG_READER_NO_PLATFORM_SOUNDS,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        mNfcAdapter?.disableReaderMode(this)
    }

    @SuppressLint("DefaultLocale")
    override fun onTagDiscovered(tag: Tag?) {
        executorService.execute {
            val isoDep = IsoDep.get(tag)
            try {
                isoDep?.connect()

                // Vibration feedback
                val vibrator = getSystemService(VIBRATOR_SERVICE) as Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(150)
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

                val card: EmvCard? = parser.readEmvCard()
                card?.let {
                    val cardNumber = it.cardNumber?.takeLast(4) ?: "Unknown"  // Get last 4 digits of card number
                    val expireDate = it.expireDate?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
                        ?: LocalDate.of(1999, 12, 31)

                    Log.d("PaymentResult", "Card Number: $cardNumber, Expiry: $expireDate")

                    val amount = etAmount.text.toString()
                    if (amount.isEmpty()) {
                        runOnUiThread {
                            tvTransactionStatus.text = "Enter a valid amount before scanning."
                        }
                        return@execute
                    }

                    val isoMsg = ISOMsg()
                    val packager = CustomISOPackager()
                    isoMsg.packager = packager
                    buildCustomMessage(isoMsg, cardNumber, expireDate.toString(), amount)

                    val response = sendOnline(isoMsg, packager)
                    val isSuccess = response?.getString("39") == "00"

                    val statusMessage = if (isSuccess) "Payment successful" else "Payment failed"
                    val formattedExpiry = expireDate.format(DateTimeFormatter.ofPattern("MM/yy")) // Format expiry date as MM/yy

                    val intent = Intent(this@TransactionActivity, TransactionDetailsActivity::class.java).apply {
                        putExtra("CARD_NUMBER", cardNumber)
                        putExtra("CARD_EXPIRY", formattedExpiry)
                        putExtra("AMOUNT", amount)
                        putExtra("STATUS", statusMessage)
                    }

                    startActivity(intent)

                    runOnUiThread {
                        tvTransactionStatus.text = "$statusMessage!\nCard: **** **** **** $cardNumber\nExpiry: $formattedExpiry"
                    }
                }
            } catch (e: IOException) {
                Log.e("NFC Error", "IOException: ${e.message}")
                runOnUiThread { tvTransactionStatus.text = "NFC Error: ${e.localizedMessage}" }
            } catch (e: Exception) {
                Log.e("NFC Error", "Exception: ${e.message}")
                runOnUiThread { tvTransactionStatus.text = "Processing Error: ${e.localizedMessage}" }
            } finally {
                try {
                    isoDep?.close()
                } catch (e: IOException) {
                    Log.e("NFC Error", "Failed to close IsoDep", e)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        executorService.shutdown()
    }
}