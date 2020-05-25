package com.udacity.astroapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;
import com.udacity.astroapp.utils.Converters;

import java.util.ArrayList;
import java.util.List;

public class MarsPhotoObject {

    @SerializedName("photos")
    private
    List<MarsPhoto> photos = new ArrayList<>();

    public MarsPhotoObject() {
    }

    public List<MarsPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<MarsPhoto> photos) {
        this.photos = photos;
    }
}