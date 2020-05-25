package com.udacity.astroapp.utils;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.udacity.astroapp.models.MarsPhotoObject;

import java.lang.reflect.Type;

/**
 * Converters for saving the custom objects to the Room database
 */
public class Converters {

    @TypeConverter
    public static MarsPhotoObject.MarsPhoto.Camera cameraFromString(String cameraValue) {
        Type cameraType = new TypeToken<MarsPhotoObject.MarsPhoto.Camera>() {
        }.getType();
        return new Gson().fromJson(cameraValue, cameraType);
    }

    @TypeConverter
    public static String cameraToString(MarsPhotoObject.MarsPhoto.Camera camera) {
        return new Gson().toJson(camera);
    }

    @TypeConverter
    public static MarsPhotoObject.MarsPhoto.Rover roverFromString(String roverValue) {
        Type roverType = new TypeToken<MarsPhotoObject.MarsPhoto.Rover>() {
        }.getType();
        return new Gson().fromJson(roverValue, roverType);
    }

    @TypeConverter
    public static String roverToString(MarsPhotoObject.MarsPhoto.Rover rover) {
        return new Gson().toJson(rover);
    }
}