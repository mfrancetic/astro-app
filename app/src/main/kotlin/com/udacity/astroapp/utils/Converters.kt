package com.udacity.astroapp.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.udacity.astroapp.models.Camera
import com.udacity.astroapp.models.Rover

/** Converters for saving the custom objects to the Room database */
object Converters {

    @TypeConverter
    @JvmStatic
    fun cameraFromString(cameraValue: String?): Camera? {
        if (cameraValue.isNullOrEmpty()) return null
        val cameraType = object : TypeToken<Camera>() {}.type
        return Gson().fromJson(cameraValue, cameraType)
    }

    @TypeConverter
    @JvmStatic
    fun cameraToString(camera: Camera?): String? {
        return camera?.let { Gson().toJson(it) }
    }

    @TypeConverter
    @JvmStatic
    fun roverFromString(roverValue: String?): Rover? {
        if (roverValue.isNullOrEmpty()) return null
        val roverType = object : TypeToken<Rover>() {}.type
        return Gson().fromJson(roverValue, roverType)
    }

    @TypeConverter
    @JvmStatic
    fun roverToString(rover: Rover?): String? {
        return rover?.let { Gson().toJson(it) }
    }
}
