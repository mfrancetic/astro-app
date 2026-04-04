package com.udacity.astroapp.utils

import java.util.Locale

fun String.formatKmH(): String {
    val regex = """([\d.]+)\s*km/h""".toRegex()
    val match = regex.find(this) ?: return this

    val value = match.groupValues[1].toDoubleOrNull() ?: return this
    val formatted = String.format(Locale.US, "%.2f", value)

    return "$formatted km/h"
}
