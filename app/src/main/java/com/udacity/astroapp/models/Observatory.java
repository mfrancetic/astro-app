package com.udacity.astroapp.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

@Entity(tableName = "observatory")
public class Observatory implements Parcelable {

    @PrimaryKey
    @NonNull
    private String observatoryId;

    private String observatoryName;

    private String observatoryAddress;

    private String observatoryPhoneNumber;

    private boolean observatoryOpenNow;

    private String observatoryOpeningHours;

    private double observatoryLatitude;

    private double observatoryLongitude;

    private String observatoryUrl;

    @Ignore
    Observatory() {
    }


    public Observatory (@NonNull String observatoryId, String observatoryName, String observatoryAddress,
                 String observatoryPhoneNumber,
                 boolean observatoryOpenNow, String observatoryOpeningHours, double observatoryLatitude, double observatoryLongitude,
                        String observatoryUrl){
        this.observatoryId = observatoryId;
        this.observatoryName = observatoryName;
        this.observatoryAddress = observatoryAddress;
        this.observatoryPhoneNumber = observatoryPhoneNumber;
        this.observatoryOpenNow = observatoryOpenNow;
        this.observatoryOpeningHours = observatoryOpeningHours;
        this.observatoryLatitude = observatoryLatitude;
        this.observatoryLongitude = observatoryLongitude;
        this.observatoryUrl = observatoryUrl;
    }

    public Observatory(Parcel in) {
        observatoryId = in.readString();
        observatoryName = in.readString();
        observatoryAddress = in.readString();
        observatoryPhoneNumber = in.readString();
        observatoryOpenNow = in.readByte() != 0;
        observatoryOpeningHours = in.readString();
        observatoryLatitude = in.readDouble();
        observatoryLongitude = in.readDouble();
        observatoryUrl = in.readString();
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

    public String getObservatoryPhoneNumber() {
        return observatoryPhoneNumber;
    }

    public boolean getObservatoryOpenNow() {
        return observatoryOpenNow;
    }

    public String getObservatoryOpeningHours() {
        return observatoryOpeningHours;
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

    public void setObservatoryId(@NonNull String observatoryId) {
        this.observatoryId = observatoryId;
    }

    public void setObservatoryName(String observatoryName) {
        this.observatoryName = observatoryName;
    }

    public void setObservatoryAddress(String observatoryAddress) {
        this.observatoryAddress = observatoryAddress;
    }

    public void setObservatoryPhoneNumber(String observatoryPhoneNumber) {
        this.observatoryPhoneNumber = observatoryPhoneNumber;
    }

    public void setObservatoryOpenNow(boolean observatoryOpenNow) {
        this.observatoryOpenNow = observatoryOpenNow;
    }

    public void setObservatoryOpeningHours(String observatoryOpeningHours) {
        this.observatoryOpeningHours = observatoryOpeningHours;
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
        dest.writeString(observatoryPhoneNumber);
        dest.writeByte((byte) (observatoryOpenNow ? 1 : 0));
        dest.writeString(observatoryOpeningHours);
        dest.writeDouble(observatoryLatitude);
        dest.writeDouble(observatoryLongitude);
        dest.writeString(observatoryUrl);
    }
}
