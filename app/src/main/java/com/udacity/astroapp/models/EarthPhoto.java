package com.udacity.astroapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "earthphoto")
public class EarthPhoto implements Parcelable {

    @PrimaryKey(autoGenerate = true)
    private int earthPhotoId;

    private String earthPhotoIdentifier;

    private String earthPhotoCaption;

    private String earthPhotoUrl;

    private String earthPhotoDateTime;

    public EarthPhoto(Parcel in) {
        earthPhotoId = in.readInt();
        earthPhotoCaption = in.readString();
        earthPhotoUrl = in.readString();
        earthPhotoDateTime = in.readString();
    }

    public EarthPhoto(String earthPhotoIdentifier, String earthPhotoCaption, String
                      earthPhotoUrl, String earthPhotoDateTime){
        this.earthPhotoIdentifier = earthPhotoIdentifier;
        this.earthPhotoCaption = earthPhotoCaption;
        this.earthPhotoUrl = earthPhotoUrl;
        this.earthPhotoDateTime = earthPhotoDateTime;
    }

    public int getEarthPhotoId() {
        return earthPhotoId;
    }

    public void setEarthPhotoId(int earthPhotoId) {
        this.earthPhotoId = earthPhotoId;
    }

    public String getEarthPhotoIdentifier() {
        return earthPhotoIdentifier;
    }

    public void setEarthPhotoIdentifier(String earthPhotoIdentifier) {
        this.earthPhotoIdentifier = earthPhotoIdentifier;
    }

    public String getEarthPhotoCaption() {
        return earthPhotoCaption;
    }

    public void setEarthPhotoCaption(String earthPhotoCaption) {
        this.earthPhotoCaption = earthPhotoCaption;
    }

    public String getEarthPhotoUrl() {
        return earthPhotoUrl;
    }

    public void setEarthPhotoUrl(String earthPhotoUrl) {
        this.earthPhotoUrl = earthPhotoUrl;
    }

    public String getEarthPhotoDateTime() {
        return earthPhotoDateTime;
    }

    public void setEarthPhotoDateTime(String earthPhotoDateTime) {
        this.earthPhotoDateTime = earthPhotoDateTime;
    }

    /**
     * Creates and returns a new EarthPhoto object, as well as a new EarthPhoto array
     */
    public static final Creator<EarthPhoto> CREATOR = new Creator<EarthPhoto>() {
        @Override
        public EarthPhoto createFromParcel(Parcel in) {
            return new EarthPhoto(in);
        }

        @Override
        public EarthPhoto[] newArray(int size) {
            return new EarthPhoto[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(earthPhotoId);
        dest.writeString(earthPhotoCaption);
        dest.writeString(earthPhotoUrl);
        dest.writeString(earthPhotoDateTime);
    }
}