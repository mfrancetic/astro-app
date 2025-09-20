package com.udacity.astroapp.models

import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "observatory")
@Parcelize
data class Observatory(
    @PrimaryKey @NonNull val observatoryId: String,
    val observatoryName: String = "",
    val observatoryAddress: String = "",
    val observatoryPhoneNumber: String = "",
    val observatoryOpenNow: Boolean = false,
    val observatoryOpeningHours: String = "",
    val observatoryLatitude: Double = 0.0,
    val observatoryLongitude: Double = 0.0,
    val observatoryUrl: String = ""
) : Parcelable
