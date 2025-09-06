package com.udacity.astroapp.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.udacity.astroapp.models.Camera
import com.udacity.astroapp.models.Rover

object Converters {
    
    private val gson = Gson()

    @TypeConverter
    fun cameraFromString(cameraValue: String?): Camera? {
        return if (cameraValue.isNullOrEmpty()) {
            null
        } else {
            gson.fromJson(cameraValue, Camera::class.java)
        }
    }

    @TypeConverter
    fun cameraToString(camera: Camera?): String? {
        return camera?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun roverFromString(roverValue: String?): Rover? {
        return if (roverValue.isNullOrEmpty()) {
            null
        } else {
            gson.fromJson(roverValue, Rover::class.java)
        }
    }

    @TypeConverter
    fun roverToString(rover: Rover?): String? {
        return rover?.let { gson.toJson(it) }
    }
}