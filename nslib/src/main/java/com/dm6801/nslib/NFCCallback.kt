package com.dm6801.nslib

import android.nfc.tech.NfcV

open class NFCCallback {

    open fun onConnect(tag: NfcV) {}

    open fun onData(data: Array<Int>) {}

    open fun onError(error: String) {}

}