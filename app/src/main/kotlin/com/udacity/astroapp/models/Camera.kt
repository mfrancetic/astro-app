package com.udacity.astroapp.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Camera(
    @SerializedName("name") val cameraName: String = "",
    @SerializedName("full_name") val cameraFullName: String = ""
) : Parcelable
