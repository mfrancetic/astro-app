package com.udacity.astroapp.models

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

@Parcelize
data class Rover(
    @SerializedName("name")
    val roverName: String = "",
    @SerializedName("launch_date")
    val launchDate: String = "",
    @SerializedName("landing_date")
    val landingDate: String = ""
) : Parcelable