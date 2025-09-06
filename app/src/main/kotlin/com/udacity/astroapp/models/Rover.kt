package com.udacity.astroapp.models

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

@Parcelize
data class Rover(
    @SerializedName("name")
    val roverName: String? = null,
    
    @SerializedName("launch_date")
    val launchDate: String? = null,
    
    @SerializedName("landing_date")
    val landingDate: String? = null
) : Parcelable