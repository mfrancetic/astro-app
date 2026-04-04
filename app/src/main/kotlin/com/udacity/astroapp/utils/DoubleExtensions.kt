package com.udacity.astroapp.utils

import java.util.Locale

fun Double.formatDiameter(): String {
    return String.format(Locale.getDefault(), "%.2f", this)
}
