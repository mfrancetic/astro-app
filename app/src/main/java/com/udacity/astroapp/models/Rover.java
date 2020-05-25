package com.udacity.astroapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Rover implements Parcelable {

    public Rover() {
    }

    @SerializedName("name")
    private String roverName;

    @SerializedName("launch_date")
    private String launchDate;

    @SerializedName("landing_date")
    private String landingDate;

    private Rover(Parcel in) {
        roverName = in.readString();
        launchDate = in.readString();
        landingDate = in.readString();
    }

    public static Creator<Rover> CREATOR = new Creator<Rover>() {

        @Override
        public Rover createFromParcel(Parcel in) {
            return new Rover(in);
        }

        @Override
        public Rover[] newArray(int size) {
            return new Rover[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(roverName);
        dest.writeString(launchDate);
        dest.writeString(landingDate);
    }

    public String getRoverName() {
        return roverName;
    }

    public void setRoverName(String roverName) {
        this.roverName = roverName;
    }

    public String getLaunchDate() {
        return launchDate;
    }

    public void setLaunchDate(String launchDate) {
        this.launchDate = launchDate;
    }

    public String getLandingDate() {
        return landingDate;
    }

    public void setLandingDate(String landingDate) {
        this.landingDate = landingDate;
    }
}