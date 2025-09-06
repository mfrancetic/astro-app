package com.udacity.astroapp.utils

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

object DateTimeUtils {
    
    private val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
    private val modernFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    fun getCurrentLocalDate(): String {
        return LocalDate.now().format(modernFormatter)
    }
    
    fun getPreviousDate(localDate: String): String? {
        return try {
            val date = LocalDate.parse(localDate, modernFormatter)
            date.minusDays(1).format(modernFormatter)
        } catch (e: Exception) {
            null
        }
    }
    
    fun getNextDate(localDate: String): String? {
        return try {
            val date = LocalDate.parse(localDate, modernFormatter)
            date.plusDays(1).format(modernFormatter)
        } catch (e: Exception) {
            null
        }
    }
    
    fun getFormattedDate(date: Date): String {
        return formatter.format(date)
    }
    
    fun getYear(dateString: String): Int? {
        return try {
            dateString.substring(0, 4).toInt()
        } catch (e: Exception) {
            null
        }
    }
    
    fun getMonth(dateString: String): Int? {
        return try {
            // Convert to 0-based month for Calendar compatibility
            dateString.substring(5, 7).toInt() - 1
        } catch (e: Exception) {
            null
        }
    }
    
    fun getDay(dateString: String): Int? {
        return try {
            dateString.substring(8, 10).toInt()
        } catch (e: Exception) {
            null
        }
    }
    
    fun getFormattedDateFromString(dateString: String): String {
        return dateString.substring(0, 10)
    }
    
    fun isValidDateFormat(dateString: String): Boolean {
        return try {
            LocalDate.parse(dateString, modernFormatter)
            true
        } catch (e: Exception) {
            false
        }
    }
    
    fun isFutureDate(dateString: String): Boolean {
        return try {
            val date = LocalDate.parse(dateString, modernFormatter)
            date.isAfter(LocalDate.now())
        } catch (e: Exception) {
            false
        }
    }
}