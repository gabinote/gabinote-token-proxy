package com.gabinote.tokenproxy.common.util.time

import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

object TimeHelper {

    fun isValidLocalDate(input: String, pattern: DateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE): Boolean {
        return try {
            LocalDate.parse(input, pattern)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }

    fun isValidTime(input: String, pattern: String = "HH:mm"): Boolean {
        return try {
            val fmt = DateTimeFormatter.ofPattern(pattern)
            LocalTime.parse(input, fmt)
            true
        } catch (e: DateTimeParseException) {
            false
        }
    }
}