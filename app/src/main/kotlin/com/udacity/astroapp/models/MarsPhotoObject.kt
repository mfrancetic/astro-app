package com.udacity.astroapp.models

import com.google.gson.annotations.SerializedName

data class MarsPhotoObject(
    @SerializedName("photos")
    val photos: List<MarsPhoto> = emptyList()
)