package com.udacity.astroapp.utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object DateUtils {
    private val isoFormatter: DateTimeFormatter = DateTimeFormatter.ISO_DATE

    fun todayIsoDate(): String = LocalDate.now().format(isoFormatter)
}
