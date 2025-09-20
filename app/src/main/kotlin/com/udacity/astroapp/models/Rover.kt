package com.udacity.astroapp.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Rover(
    @SerializedName("name") val roverName: String = "",
    @SerializedName("launch_date") val launchDate: String = "",
    @SerializedName("landing_date") val landingDate: String = ""
) : Parcelable
