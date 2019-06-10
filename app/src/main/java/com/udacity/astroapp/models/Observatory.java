package com.udacity.astroapp.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

@Entity(tableName = "observatory")
public class Observatory implements Parcelable {

    @PrimaryKey
    @NonNull
    private String observatoryId;

    private String observatoryName;

    private String observatoryAddress;

    private boolean observatoryOpenNow;

    private double observatoryLatitude;

    private double observatoryLongitude;

//    private String observatoryOpeningHours;

    private String observatoryUrl;

    private String observatoryPhotoUrl;


    @Ignore
    Observatory() {
    }


    public Observatory (@NonNull String observatoryId, String observatoryName, String observatoryAddress,
                 boolean observatoryOpenNow, double observatoryLatitude, double observatoryLongitude,
                        String observatoryUrl, String observatoryPhotoUrl){
        this.observatoryId = observatoryId;
        this.observatoryName = observatoryName;
        this.observatoryAddress = observatoryAddress;
        this.observatoryOpenNow = observatoryOpenNow;
        this.observatoryLatitude = observatoryLatitude;
        this.observatoryLongitude = observatoryLongitude;
        this.observatoryUrl = observatoryUrl;
        this.observatoryPhotoUrl = observatoryPhotoUrl;
    }

    public Observatory(Parcel in) {
        observatoryId = in.readString();
        observatoryName = in.readString();
        observatoryAddress = in.readString();
        observatoryOpenNow = in.readByte() != 0;
        observatoryUrl = in.readString();
        observatoryPhotoUrl = in.readString();
    }

    @NonNull
    public String getObservatoryId() {
        return observatoryId;
    }

    public String getObservatoryName() {
        return observatoryName;
    }

    public String getObservatoryAddress() {
        return observatoryAddress;
    }

    public boolean getObservatoryOpenNow() {
        return observatoryOpenNow;
    }

    public double getObservatoryLatitude() {
        return observatoryLatitude;
    }

    public double getObservatoryLongitude() {
        return observatoryLongitude;
    }

    public String getObservatoryUrl() {
        return observatoryUrl;
    }

    public String getObservatoryPhotoUrl() {
        return observatoryPhotoUrl;
    }

    public void setObservatoryId(@NonNull String observatoryId) {
        this.observatoryId = observatoryId;
    }

    public void setObservatoryName(String observatoryName) {
        this.observatoryName = observatoryName;
    }

    public void setObservatoryAddress(String observatoryAddress) {
        this.observatoryAddress = observatoryAddress;
    }

    public void setObservatoryOpenNow(boolean observatoryOpenNow) {
        this.observatoryOpenNow = observatoryOpenNow;
    }

    public void setObservatoryLatitude(double observatoryLatitude) {
        this.observatoryLatitude = observatoryLatitude;
    }

    public void setObservatoryLongitude(double observatoryLongitude) {
        this.observatoryLongitude = observatoryLongitude;
    }

    public void setObservatoryUrl(String observatoryUrl) {
        this.observatoryUrl = observatoryUrl;
    }

    public void setObservatoryPhotoUrl(String observatoryPhotoUrl) {
        this.observatoryPhotoUrl = observatoryPhotoUrl;
    }

    public static final Creator<Observatory> CREATOR = new Creator<Observatory>() {
        @Override
        public Observatory createFromParcel(Parcel in) {
            return new Observatory(in);
        }

        @Override
        public Observatory[] newArray(int size) {
            return new Observatory[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(observatoryId);
        dest.writeString(observatoryName);
        dest.writeString(observatoryAddress);
        dest.writeByte((byte) (observatoryOpenNow ? 1 : 0));
        dest.writeDouble(observatoryLatitude);
        dest.writeDouble(observatoryLongitude);
        dest.writeString(observatoryUrl);
        dest.writeString(observatoryPhotoUrl);
    }
}
