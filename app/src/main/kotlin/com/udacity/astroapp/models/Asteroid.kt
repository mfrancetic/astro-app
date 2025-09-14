package com.udacity.astroapp.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Entity(tableName = "asteroid")
@Parcelize
data class Asteroid(
    @PrimaryKey
    val asteroidId: Int = 0,
    val asteroidName: String = "",
    val asteroidDiameterMin: Double = 0.0,
    val asteroidDiameterMax: Double = 0.0,
    val asteroidApproachDate: String = "",
    val asteroidVelocity: String = "",
    val asteroidIsHazardous: Boolean = false,
    val asteroidUrl: String = ""
) : Parcelable {

    @Ignore
    constructor() : this(
        asteroidId = 0,
        asteroidName = "",
        asteroidDiameterMin = 0.0,
        asteroidDiameterMax = 0.0,
        asteroidApproachDate = "",
        asteroidVelocity = "",
        asteroidIsHazardous = false,
        asteroidUrl = ""
    )
}