package com.gab.gabby

import com.gab.gabby.util.dec
import com.gab.gabby.util.inc
import com.gab.gabby.util.isLessThan
import org.junit.Assert.*
import org.junit.Test

class StringUtilsTest {
    @Test
    fun isLessThan() {
        val lessList = listOf(
                "abc" to "bcd",
                "ab" to "abc",
                "cb" to "abc",
                "1" to "2"
        )
        lessList.forEach { (l, r) -> assertTrue("$l < $r", l.isLessThan(r)) }
        val notLessList = lessList.map { (l, r) -> r to l } + listOf(
                "abc" to "abc"
        )
        notLessList.forEach { (l, r) -> assertFalse("not $l < $r", l.isLessThan(r)) }
    }

    @Test
    fun inc() {
        listOf(
                "122" to "123",
                "12A" to "12B",
                "1" to "2"
        ).forEach { (l, r) -> assertEquals("$l + 1 = $r", r, l.inc()) }
    }

    @Test
    fun dec() {
        listOf(
                "123" to "122",
                "12B" to "12A",
                "120" to "11z",
                "100" to "zz",
                "0" to "",
                "" to "",
                "2" to "1"
        ).forEach { (l, r) -> assertEquals("$l - 1 = $r", r, l.dec()) }
    }
}