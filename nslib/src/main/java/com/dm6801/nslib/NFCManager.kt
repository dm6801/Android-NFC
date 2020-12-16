package com.dm6801.nslib

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.NfcV
import android.os.Bundle
import java.util.*

class NFCManager : Application.ActivityLifecycleCallbacks {

    private var application: Application? = null//by weakRef(null)

    private val _nfcAdapter: NfcAdapter? get() = catch { NfcAdapter.getDefaultAdapter(application) }
    private var nfcAdapter: NfcAdapter? = null
        get() = field ?: _nfcAdapter

    private var callback: NFCCallback? = null
    private val queue: ArrayDeque<ByteArray> = ArrayDeque()

    private var lastNfcV: NfcV? by weakRef(null)

    private var prettyPrint: Boolean = true

    fun hook(application: Application?, listener: NFCCallback, prettyPrint: Boolean = true) {
        Log("hook()")
        this.application = application
        this.callback = listener
        this.prettyPrint = prettyPrint
        application?.registerActivityLifecycleCallbacks(this)
        cacheNfcAdapter()
    }

    private fun cacheNfcAdapter() {
        nfcAdapter = _nfcAdapter
        Log("nfc adapter: $nfcAdapter isEnabled=${nfcAdapter?.isEnabled}")
    }

    fun queue(data: Array<Int>) {
        Log("queue()")
        if (data.isEmpty()) return

        val payload = data.map { it.toByte() }
        val byteArray = payload.toByteArray()
        Log("queue(): payload=" + byteArray.asText())
        queue.add(byteArray)
    }

    fun getQueue(): List<Array<Int>> {
        if (queue.isEmpty()) {
            Log("getQueue(): queue is empty")
            return emptyList()
        }
        Log("getQueue():\n" + queue.joinToString("\n"))
        return queue.map { byteArray -> byteArray.map { it.toInt() }.toTypedArray() }
    }

    fun clearQueue() {
        Log("clearQueue()")
        queue.clear()
    }

    private fun onNfcDetected(tag: Tag) {
        try {
            val nfcv = NfcV.get(tag)
            Log("onNfcDetected: id=${nfcv.tag.id.asText(prettyPrint)}")
            Log("onNfcDetected: techList=${nfcv.tag.techList.joinToString()}")
            lastNfcV = nfcv
            nfcv.connect()
            callback?.onConnect(nfcv)
            Log("onNfcDetected: isConnected=${nfcv.isConnected}")
            flushQueue(nfcv)

        } catch (e: Exception) {
            e.printStackTrace()
            callback?.onError("${e.javaClass}: ${e.message}")
        }
    }

    private fun flushQueue(tag: NfcV) {
        if (queue.isEmpty()) return
        Log("flushQueue(): size=${queue.size}")
        while (queue.iterator().hasNext()) {
            if (!tag.isConnected) break
            val payload = queue.poll() ?: continue
            Log("send: ${payload.asText(prettyPrint)}")
            val result = tag.transceive(payload)
            Log("receive: ${result.asText(prettyPrint)}")
            callback?.onData(result.map { it.toInt() }.toTypedArray())
        }
    }

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        Log("onActivityCreated")
    }

    override fun onActivityStarted(activity: Activity) {
        Log("onActivityStarted")
    }

    override fun onActivityResumed(activity: Activity) {
        Log("onActivityResumed")
        try {
            nfcAdapter?.enableReaderMode(
                activity, ::onNfcDetected, NfcAdapter.FLAG_READER_NFC_V, Bundle()
            )
        } catch (e: Exception) {
            e.printStackTrace()
            callback?.onError("${e.javaClass}: ${e.message}")
        }
    }

    override fun onActivityPaused(activity: Activity) {
        Log("onActivityPaused")
        try {
            nfcAdapter?.disableReaderMode(activity)
        } catch (e: Exception) {
            e.printStackTrace()
            callback?.onError("${e.javaClass}: ${e.message}")
        }
    }

    override fun onActivityStopped(activity: Activity) {
        Log("onActivityStopped")
    }

    override fun onActivityDestroyed(activity: Activity) {
        Log("onActivityDestroyed")
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
        Log("onActivitySaveInstanceState")
    }

    //debug
    /*fun log(text: String): String {
        println(text)
        Log.d(javaClass.simpleName, text)
        return text
    }

    fun callback(action: Any?) {
        Log("callback: ${action?.javaClass.toString()}")
        if (action is Function0<Any?>) {
            Log("callback: is a function. executing..")
            action.invoke()
        }
    }

    fun testInterface(obj: Any) {
        Log("inside testInterface")
        if (obj is TestInterface) obj.hello()
    }

    interface TestInterface {
        fun hello()
    }

    interface Interface {
        fun onConnect(tag: NfcV)
        fun onData(data: Array<Int>)
        fun onError(error: String)
    }*/

}

@SuppressLint("DefaultLocale")
private fun ByteArray.asText(isHex: Boolean = true): String {
    return if (isHex) {
        joinToString(" ") {
            Integer.toHexString(it.toInt() and 0xff).padStart(2, '0').toUpperCase()
        }
    } else {
        joinToString(" ") { it.toInt().toString() }
    }
}

private fun String.asByteArray(): ByteArray {
    return split(" ").map { it.toInt(16).toByte() }.toByteArray()
}