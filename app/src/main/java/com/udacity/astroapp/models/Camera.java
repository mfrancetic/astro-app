package com.udacity.astroapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Camera implements Parcelable {

    public Camera() {
    }

    @SerializedName("name")
    private String cameraName;

    @SerializedName("full_name")
    private String cameraFullName;

    private Camera(Parcel in) {
        cameraName = in.readString();
        cameraFullName = in.readString();
    }

    public static Creator<Camera> CREATOR = new Creator<Camera>() {
        @Override
        public Camera createFromParcel(Parcel in) {
            return new Camera(in);
        }

        @Override
        public Camera[] newArray(int size) {
            return new Camera[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(cameraName);
        dest.writeString(cameraFullName);
    }

    public String getCameraName() {
        return cameraName;
    }

    public void setCameraName(String cameraName) {
        this.cameraName = cameraName;
    }

    public String getCameraFullName() {
        return cameraFullName;
    }

    public void setCameraFullName(String cameraFullName) {
        this.cameraFullName = cameraFullName;
    }
}