package com.udacity.astroapp.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "earthphoto")
@Parcelize
data class EarthPhoto(
    @PrimaryKey(autoGenerate = true) val earthPhotoId: Int = 0,
    val earthPhotoCaption: String? = null,
    val earthPhotoUrl: String,
    val earthPhotoDateTime: String,
    val cacheTimestamp: Long = System.currentTimeMillis()
) : Parcelable
