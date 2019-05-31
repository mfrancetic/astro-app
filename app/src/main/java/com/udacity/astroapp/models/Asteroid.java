package com.udacity.astroapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Asteroid implements Parcelable {

    private int asteroidId;

    private String asteroidName;

    private float asteroidDiameter;

    private String asteroidApproachDate;

    private float asteroidVelocity;

    private boolean asteroidIsHazardous;

    Asteroid (int asteroidId, String asteroidName, float asteroidDiameter,
              String asteroidApproachDate, float asteroidVelocity, boolean asteroidIsHazardous) {
        this.asteroidId = asteroidId;
        this.asteroidName = asteroidName;
        this.asteroidDiameter = asteroidDiameter;
        this.asteroidApproachDate = asteroidApproachDate;
        this.asteroidVelocity = asteroidVelocity;
        this.asteroidIsHazardous = asteroidIsHazardous;
    }

     Asteroid(Parcel in) {
        asteroidId = in.readInt();
        asteroidName = in.readString();
        asteroidDiameter = in.readFloat();
        asteroidApproachDate = in.readString();
        asteroidVelocity = in.readFloat();
        asteroidIsHazardous = in.readByte() != 0;
    }

    public int getAsteroidId() {
        return asteroidId;
    }

    public String getAsteroidName() {
        return asteroidName;
    }

    public float getAsteroidDiameter() {
        return asteroidDiameter;
    }

    public String getAsteroidApproachDate() {
        return asteroidApproachDate;
    }

    public float getAsteroidVelocity() {
        return asteroidVelocity;
    }

    public boolean getAsteroidIsHazardous() {
        return asteroidIsHazardous;
    }

    public void setAsteroidId(int asteroidId) {
        this.asteroidId = asteroidId;
    }

    public void setAsteroidName(String asteroidName) {
        this.asteroidName = asteroidName;
    }

    public void setAsteroidDiameter(float asteroidDiameter) {
        this.asteroidDiameter = asteroidDiameter;
    }

    public void setAsteroidApproachDate(String asteroidApproachDate) {
        this.asteroidApproachDate = asteroidApproachDate;
    }

    public void setAsteroidVelocity(float asteroidVelocity) {
        this.asteroidVelocity = asteroidVelocity;
    }

    public void setAsteroidIsHazardous(boolean asteroidIsHazardous) {
        this.asteroidIsHazardous = asteroidIsHazardous;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(asteroidId);
        dest.writeString(asteroidName);
        dest.writeFloat(asteroidDiameter);
        dest.writeString(asteroidApproachDate);
        dest.writeFloat(asteroidVelocity);
        dest.writeByte((byte) (asteroidIsHazardous ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Asteroid> CREATOR = new Creator<Asteroid>() {
        @Override
        public Asteroid createFromParcel(Parcel in) {
            return new Asteroid(in);
        }

        @Override
        public Asteroid[] newArray(int size) {
            return new Asteroid[size];
        }
    };
}