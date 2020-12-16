package com.dm6801.nsplugin

import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcV
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dm6801.nslib_java.LibNfc
import kotlinx.android.synthetic.main.activity_main.*

class NfcActivity : AppCompatActivity(), LibNfc.Listener {

    var TAG = javaClass.simpleName
    private val nfcLib = LibNfc()

    private val textView: TextView? get() = text_view
    private val editText: EditText? get() = edit_text

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        parseIntent(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        parseIntent(intent)
    }

    private fun parseIntent(intent: Intent) {
        val extraTag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: return
        Log.d(TAG, "nfc: tag=$extraTag")
    }

    override fun onResume() {
        super.onResume()
        nfcLib.start(this)
    }

    override fun onConnect(tag: NfcV?) {
        Log.d(TAG, "onConnect: $tag")
        if (tag == null) return
        try {
            val text = editText?.text?.toString() ?: return
            val payloads: List<ByteArray>

            when (text) {
                "id" -> {
                    val tagId = tag.tag.id
                    payloads = listOf(byteArrayOf(0x20, 0x2B) + tagId)
                }
                else -> {
                    payloads = text.split("\n").map { it.asByteArray() }
                }
            }

            payloads.forEach { payload ->
                tag.sendPayload(payload)?.let(::displayResult)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            runOnUiThread {
                Toast.makeText(this, "${e.javaClass.simpleName}: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun NfcV.sendPayload(byteArray: ByteArray): ByteArray? {
        return try {
            transceive(byteArray)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun displayResult(result: ByteArray) {
        val textResult = result.asText()
        textView?.apply {
            setText("${this.text}\n$textResult")
        }
    }

    private fun ByteArray.asText(): String {
        return joinToString(" ") {
            Integer.toHexString(it.toInt() and 0xff).padStart(2, '0').toUpperCase()
        }
    }

    private fun String.asByteArray(): ByteArray {
        return split(" ").map { it.toInt(16).toByte() }.toByteArray()
    }

}