package com.udacity.astroapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;
import com.udacity.astroapp.utils.Converters;

@Entity(tableName = "marsphoto")
public class MarsPhoto implements Parcelable {

    public MarsPhoto() {
    }

    @SerializedName("id")
    @PrimaryKey
    private int id;

    @SerializedName("sol")
    private String sol;

    @SerializedName("img_src")
    private String imageUrl;

    @SerializedName("earth_date")
    private String earthDate;

    @TypeConverters(Converters.class)
    @ColumnInfo(name = "camera")
    @SerializedName("camera")
    private Camera camera;

    @TypeConverters(Converters.class)
    @ColumnInfo(name = "rover")
    @SerializedName("rover")
    private Rover rover;

    private MarsPhoto(Parcel in) {
        id = in.readInt();
        sol = in.readString();
        imageUrl = in.readString();
        earthDate = in.readString();
        camera = in.readParcelable(Camera.class.getClassLoader());
        rover = in.readParcelable(Rover.class.getClassLoader());
    }

    public static final Creator<MarsPhoto> CREATOR = new Creator<MarsPhoto>() {
        @Override
        public MarsPhoto createFromParcel(Parcel in) {
            return new MarsPhoto(in);
        }

        @Override
        public MarsPhoto[] newArray(int size) {
            return new MarsPhoto[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(sol);
        dest.writeString(imageUrl);
        dest.writeString(earthDate);
        dest.writeParcelable(camera, 0);
        dest.writeParcelable(rover, 0);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSol() {
        return sol;
    }

    public void setSol(String sol) {
        this.sol = sol;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getEarthDate() {
        return earthDate;
    }

    public void setEarthDate(String earthDate) {
        this.earthDate = earthDate;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Rover getRover() {
        return rover;
    }

    public void setRover(Rover rover) {
        this.rover = rover;
    }
}