package com.dm6801.nslib

fun <E : Any> E.Log(arg: Any, level: Int = android.util.Log.DEBUG) {
    android.util.Log.println(
        level,
        javaClass.simpleName.substringAfterLast(".").substringBefore("$"),
        arg.toString()
    )
}