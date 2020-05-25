package com.udacity.astroapp.models;

import com.google.gson.annotations.SerializedName;

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