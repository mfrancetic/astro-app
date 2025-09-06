package com.udacity.astroapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(tableName = "observatory")
@Parcelize
data class Observatory(
    @PrimaryKey
    val observatoryId: String,
    val observatoryName: String? = null,
    val observatoryAddress: String? = null,
    val observatoryPhoneNumber: String? = null,
    val observatoryOpenNow: Boolean = false,
    val observatoryOpeningHours: String? = null,
    val observatoryLatitude: Double = 0.0,
    val observatoryLongitude: Double = 0.0,
    val observatoryUrl: String? = null,
    val cacheTimestamp: Long = System.currentTimeMillis()
) : Parcelable