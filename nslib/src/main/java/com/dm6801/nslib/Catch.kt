package com.dm6801.nslib

fun <E, R> E.catch(action: E.() -> R): R? {
    return try {
        action()
    } catch (t: Throwable) {
        t.printStackTrace()
        null
    }
}

fun <R> catch(action: () -> R): R? {
    return try {
        action()
    } catch (t: Throwable) {
        t.printStackTrace()
        null
    }
}