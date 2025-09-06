package com.udacity.astroapp.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Entity(tableName = "asteroid")
@Parcelize
data class Asteroid(
    @PrimaryKey
    val asteroidId: Int,
    val asteroidName: String,
    val asteroidDiameterMin: Double,
    val asteroidDiameterMax: Double,
    val asteroidApproachDate: String,
    val asteroidVelocity: String,
    val asteroidIsHazardous: Boolean,
    val asteroidUrl: String,
    val cacheTimestamp: Long = System.currentTimeMillis()
) : Parcelable