package com.udacity.astroapp.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "observatory")
public class Observatory implements Parcelable {

    @PrimaryKey
    private int observatoryId;

    private String observatoryName;

    private String observatoryAddress;

    private String observatoryOpeningHours;

    private String observatoryUrl;

    private String observatoryPhotoUrl;

    @Ignore
    Observatory() {
    }


    public Observatory (int observatoryId, String observatoryName, String observatoryAddress,
                 String observatoryOpeningHours, String observatoryUrl, String observatoryPhotoUrl){
        this.observatoryId = observatoryId;
        this.observatoryName = observatoryName;
        this.observatoryAddress = observatoryAddress;
        this.observatoryOpeningHours = observatoryOpeningHours;
        this.observatoryUrl = observatoryUrl;
        this.observatoryPhotoUrl = observatoryPhotoUrl;
    }

    public Observatory(Parcel in) {
        observatoryId = in.readInt();
        observatoryName = in.readString();
        observatoryAddress = in.readString();
        observatoryOpeningHours = in.readString();
        observatoryUrl = in.readString();
        observatoryPhotoUrl = in.readString();
    }

    public int getObservatoryId() {
        return observatoryId;
    }

    public String getObservatoryName() {
        return observatoryName;
    }

    public String getObservatoryAddress() {
        return observatoryAddress;
    }

    public String getObservatoryOpeningHours() {
        return observatoryOpeningHours;
    }

    public String getObservatoryUrl() {
        return observatoryUrl;
    }

    public String getObservatoryPhotoUrl() {
        return observatoryPhotoUrl;
    }

    public void setObservatoryId(int observatoryId) {
        this.observatoryId = observatoryId;
    }

    public void setObservatoryName(String observatoryName) {
        this.observatoryName = observatoryName;
    }

    public void setObservatoryAddress(String observatoryAddress) {
        this.observatoryAddress = observatoryAddress;
    }

    public void setObservatoryOpeningHours(String observatoryOpeningHours) {
        this.observatoryOpeningHours = observatoryOpeningHours;
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
        dest.writeInt(observatoryId);
        dest.writeString(observatoryName);
        dest.writeString(observatoryAddress);
        dest.writeString(observatoryOpeningHours);
        dest.writeString(observatoryUrl);
        dest.writeString(observatoryPhotoUrl);
    }
}
