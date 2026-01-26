package com.example.myapp.utils

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date

object DateFormatUtils {

    private val inputFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)

    private val dateFormat =
        SimpleDateFormat("dd MMM yyyy", Locale.forLanguageTag("es-MX"))

    private val timeFormat =
        SimpleDateFormat("HH:mm", Locale.US)

    fun formatRange(startAt: String, endAt: String): String {
        return try {
            val start: Date = inputFormat.parse(startAt)!!
            val end: Date = inputFormat.parse(endAt)!!

            val date = dateFormat.format(start)
            val startTime = timeFormat.format(start)
            val endTime = timeFormat.format(end)

            "$date · $startTime – $endTime"
        } catch (e: Exception) {
            "$startAt → $endAt"
        }
    }
}
