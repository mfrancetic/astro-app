package com.udacity.astroapp.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    fun getCurrentLocalDate(): String {
        return formatter.format(getCalendar().time)
    }

    fun getPreviousDate(localDate: String): String? {
        val calendar = getCalendar()
        return try {
            calendar.time = formatter.parse(localDate) ?: return null
            calendar.add(Calendar.DATE, -1)
            println(formatter.format(calendar.time))
            formatter.format(calendar.time)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    fun getCalendar(): Calendar {
        return Calendar.getInstance()
    }

    fun getFormattedDate(date: Date): String {
        return formatter.format(date)
    }

    fun getYear(date: String): Int {
        return date.substring(0, 4).toInt()
    }

    fun getMonth(date: String): Int {
        return date.substring(5, 7).toInt()
    }

    fun getDay(date: String): Int {
        return date.substring(8, 10).toInt()
    }

    fun getNextDate(localDate: String): String? {
        val calendar = getCalendar()
        return try {
            calendar.time = formatter.parse(localDate) ?: return null
            calendar.add(Calendar.DATE, 1)
            formatter.format(calendar.time)
        } catch (e: ParseException) {
            e.printStackTrace()
            null
        }
    }

    fun getDateFromCalendar(year: Int, month: Int, dayOfMonth: Int): String {
        val calendar = getCalendar()
        calendar.set(year, month, dayOfMonth)
        return formatter.format(calendar.time)
    }
}