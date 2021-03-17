package com.io.ellipse.data.utils

import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

private const val TIMEZONE_UTC = "UTC"
private const val UTC_DATE_FORMAT = "yyyy.MM.dd HH:mm:ss"


fun Date.toUTC(): Date {
    try {
        val dateFormat = SimpleDateFormat(UTC_DATE_FORMAT, Locale.US)
        dateFormat.timeZone = TimeZone.getTimeZone(TIMEZONE_UTC)
        val strDate: String = dateFormat.format(this)
        val dateFormatLocal = SimpleDateFormat(UTC_DATE_FORMAT, Locale.US)
        return dateFormatLocal.parse(strDate) ?: this
    } catch (ex: Exception) {
        return this
    }
}