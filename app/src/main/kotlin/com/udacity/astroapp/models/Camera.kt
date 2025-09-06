package com.udacity.astroapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

@Parcelize
data class Camera(
    @SerializedName("name")
    val cameraName: String? = null,
    
    @SerializedName("full_name")
    val cameraFullName: String? = null
) : Parcelable