package com.bettyai.assistant.util

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class PersianTextTest {

    @Test
    fun `toPersianDigits converts ascii digits`() {
        assertEquals("۱۲۳۴۵۶۷۸۹۰", PersianText.toPersianDigits("1234567890"))
    }

    @Test
    fun `toPersianDigits leaves non digits untouched`() {
        assertEquals("ساعت ۱۲:۳۰", PersianText.toPersianDigits("ساعت 12:30"))
    }

    @Test
    fun `toPersianDigits int overload`() {
        assertEquals("۴۲", PersianText.toPersianDigits(42))
    }

    @Test
    fun `weekdayName returns persian names`() {
        // 2024-03-20 is a Wednesday (چهارشنبه).
        assertEquals("چهارشنبه", PersianText.weekdayName(LocalDate.of(2024, 3, 20)))
        // 2024-03-21 is a Thursday (پنجشنبه).
        assertEquals("پنجشنبه", PersianText.weekdayName(LocalDate.of(2024, 3, 21)))
    }

    @Test
    fun `jalali conversion matches Nowruz 1403`() {
        // 20 March 2024 == 1 Farvardin 1403 (Nowruz).
        val (jy, jm, jd) = JalaliDate.fromGregorian(2024, 3, 20)
        assertEquals(1403, jy)
        assertEquals(1, jm)
        assertEquals(1, jd)
    }

    @Test
    fun `jalali conversion matches another known date`() {
        // 1 January 2024 == 11 Dey 1402.
        val (jy, jm, jd) = JalaliDate.fromGregorian(2024, 1, 1)
        assertEquals(1402, jy)
        assertEquals(10, jm)
        assertEquals(11, jd)
    }

    @Test
    fun `formatJalali produces persian digits and month name`() {
        val formatted = PersianText.formatJalali(LocalDate.of(2024, 3, 20))
        // Should contain Persian digits for the year and the month name فروردین.
        assertEquals("۱ فروردین ۱۴۰۳", formatted)
    }
}
