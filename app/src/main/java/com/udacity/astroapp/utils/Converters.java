package com.udacity.astroapp.utils;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.udacity.astroapp.models.Camera;
import com.udacity.astroapp.models.MarsPhotoObject;
import com.udacity.astroapp.models.Rover;

import java.lang.reflect.Type;

/**
 * Converters for saving the custom objects to the Room database
 */
public class Converters {

    @TypeConverter
    public static Camera cameraFromString(String cameraValue) {
        Type cameraType = new TypeToken<Camera>() {
        }.getType();
        return new Gson().fromJson(cameraValue, cameraType);
    }

    @TypeConverter
    public static String cameraToString(Camera camera) {
        return new Gson().toJson(camera);
    }

    @TypeConverter
    public static Rover roverFromString(String roverValue) {
        Type roverType = new TypeToken<Rover>() {
        }.getType();
        return new Gson().fromJson(roverValue, roverType);
    }

    @TypeConverter
    public static String roverToString(Rover rover) {
        return new Gson().toJson(rover);
    }
}