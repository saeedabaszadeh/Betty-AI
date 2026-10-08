package com.bettyai.assistant.util

import java.time.LocalDate

/** Small helpers for producing natural Persian text (digits, dates, weekdays). */
object PersianText {

    private val persianDigits = charArrayOf('۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹')

    private val jalaliMonths = arrayOf(
        "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
        "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند"
    )

    // Indexed by java.time.DayOfWeek.value (1=Monday .. 7=Sunday); index 0 unused.
    private val weekdays = arrayOf(
        "", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنجشنبه", "جمعه", "شنبه", "یکشنبه"
    )

    /** Convert ASCII digits to Persian digits. */
    fun toPersianDigits(input: String): String = buildString(input.length) {
        input.forEach { c -> append(if (c in '0'..'9') persianDigits[c - '0'] else c) }
    }

    /** Convert a whole number to its Persian-digit representation. */
    fun toPersianDigits(input: Int): String = toPersianDigits(input.toString())

    /** Persian weekday name for a [LocalDate]. */
    fun weekdayName(date: LocalDate): String =
        weekdays.getOrElse(date.dayOfWeek.value) { "" }

    /** Format a [LocalDate] as a Persian (Jalali) date string, e.g. "۱۵ شهریور ۱۴۰۳". */
    fun formatJalali(date: LocalDate): String {
        val (jy, jm, jd) = JalaliDate.fromGregorian(date.year, date.monthValue, date.dayOfMonth)
        val monthName = jalaliMonths.getOrElse(jm - 1) { "" }
        return toPersianDigits("$jd $monthName $jy")
    }
}

/**
 * Gregorian → Jalali (Solar Hijri) date conversion using the well-known
 * arithmetic algorithm. Validated by unit tests against Nowruz dates.
 */
object JalaliDate {

    fun fromGregorian(gy: Int, gm: Int, gd: Int): Triple<Int, Int, Int> {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        // Reduce the Gregorian year to the algorithm's epoch (1600 / 621).
        val g: Int
        var jy: Int
        if (gy > 1600) {
            g = gy - 1600
            jy = 979
        } else {
            g = gy - 621
            jy = 0
        }
        // Leap-year terms use the year shifted forward by one for months after
        // February (gm > 2). Omitting this shift is the classic off-by-one bug.
        val gy2 = if (gm > 2) g + 1 else g
        var days = 365 * g + (gy2 + 3) / 4 - (gy2 + 99) / 100 + (gy2 + 399) / 400 - 80 +
            gd + gDaysInMonth[gm - 1]

        jy += 33 * (days / 12053)
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm = if (days < 186) 1 + days / 31 else 7 + (days - 186) / 30
        val jd = 1 + if (days < 186) days % 31 else (days - 186) % 30
        return Triple(jy, jm, jd)
    }
}
