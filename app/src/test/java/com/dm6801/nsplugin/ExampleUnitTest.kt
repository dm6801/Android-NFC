package com.dm6801.nsplugin

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun bytes() {
        val text = "2B 20"
        println(convertTextToByteArray(text).joinToString())
    }

    private fun convertTextToByteArray(text: String): ByteArray {
        val sections = text.split(" ")
        return sections.map { it.toByte(16) }.toByteArray()
    }
}
