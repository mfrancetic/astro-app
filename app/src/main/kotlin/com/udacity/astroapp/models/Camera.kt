package com.udacity.astroapp.models

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class Camera(
    @SerializedName("name")
    val cameraName: String = "",
    @SerializedName("full_name")
    val cameraFullName: String = ""
) : Parcelable