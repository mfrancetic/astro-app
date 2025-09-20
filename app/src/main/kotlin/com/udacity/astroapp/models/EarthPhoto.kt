package com.udacity.astroapp.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "earthphoto")
@Parcelize
data class EarthPhoto(
    @PrimaryKey(autoGenerate = true) val earthPhotoId: Int = 0,
    val earthPhotoIdentifier: String = "",
    val earthPhotoCaption: String = "",
    val earthPhotoUrl: String = "",
    val earthPhotoDateTime: String = ""
) : Parcelable
