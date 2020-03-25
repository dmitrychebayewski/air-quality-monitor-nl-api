package com.minskrotterdam.airquality.extensions

import org.junit.Assert
import org.junit.Test


class ExtensionsTest {

    private val testData = mapOf(0 to listOf<IntRange>(),
            1 to listOf<IntRange>(),
            2 to listOf(2..2),
            3 to listOf(2..3),

            29 to listOf(2..29),
            30 to listOf(2..30),
            31 to listOf(2..31),
            32 to listOf(2..31, 32..32),

            59 to listOf(2..31, 32..59),
            60 to listOf(2..31, 32..60),
            61 to listOf(2..31, 32..61),
            62 to listOf(2..31, 32..61, 62..62),

            88 to listOf(2..31, 32..61, 62..88),
            89 to listOf(2..31, 32..61, 62..89),
            90 to listOf(2..31, 32..61, 62..90),
            91 to listOf(2..31, 32..61, 62..91),
            92 to listOf(2..31, 32..61, 62..91, 92..92)
    )

    @Test
    fun testGetRangesForSafeRequestLaunch() {
        testData.keys.forEach {
            println("Last page: $it")
            val actual = getSafeLaunchRanges(it)
            println(actual)
            println("---")
            Assert.assertEquals(testData[it], actual)
        }
    }
}