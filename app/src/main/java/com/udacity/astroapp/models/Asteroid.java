package com.udacity.astroapp.models;

import android.arch.persistence.room.Entity;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "asteroid")
public class Asteroid implements Parcelable {

    private int asteroidId;

    private String asteroidName;

    private double asteroidDiameterMin;

    private double asteroidDiameterMax;

    private String asteroidApproachDate;

    private String asteroidVelocity;

    private boolean asteroidIsHazardous;

    private String asteroidUrl;

   public Asteroid (int asteroidId, String asteroidName, double asteroidDiameterMin,
              double asteroidDiameterMax,
              String asteroidApproachDate, String asteroidVelocity, boolean asteroidIsHazardous,
                    String asteroidUrl) {
        this.asteroidId = asteroidId;
        this.asteroidName = asteroidName;
        this.asteroidDiameterMin = asteroidDiameterMin;
        this.asteroidDiameterMax = asteroidDiameterMax;
        this.asteroidApproachDate = asteroidApproachDate;
        this.asteroidVelocity = asteroidVelocity;
        this.asteroidIsHazardous = asteroidIsHazardous;
        this.asteroidUrl = asteroidUrl;
    }

     Asteroid(Parcel in) {
        asteroidId = in.readInt();
        asteroidName = in.readString();
        asteroidDiameterMin = in.readDouble();
        asteroidDiameterMax = in.readDouble();
        asteroidApproachDate = in.readString();
        asteroidVelocity = in.readString();
        asteroidIsHazardous = in.readByte() != 0;
        asteroidUrl = in.readString();
    }

    public int getAsteroidId() {
        return asteroidId;
    }

    public String getAsteroidName() {
        return asteroidName;
    }

    public double getAsteroidDiameterMin() {
        return asteroidDiameterMin;
    }

    public double getAsteroidDiameterMax() {
        return asteroidDiameterMax;
    }

    public String getAsteroidApproachDate() {
        return asteroidApproachDate;
    }

    public String getAsteroidVelocity() {
        return asteroidVelocity;
    }

    public boolean getAsteroidIsHazardous() {
        return asteroidIsHazardous;
    }

    public String getAsteroidUrl() {
        return asteroidUrl;
    }

    public void setAsteroidId(int asteroidId) {
        this.asteroidId = asteroidId;
    }

    public void setAsteroidName(String asteroidName) {
        this.asteroidName = asteroidName;
    }

    public void setAsteroidDiameterMin(double asteroidDiameterMin) {
        this.asteroidDiameterMin = asteroidDiameterMin;
    }

    public void setAsteroidDiameterMax(double asteroidDiameterMax) {
        this.asteroidDiameterMax = asteroidDiameterMax;
    }

    public void setAsteroidApproachDate(String asteroidApproachDate) {
        this.asteroidApproachDate = asteroidApproachDate;
    }

    public void setAsteroidVelocity(String asteroidVelocity) {
        this.asteroidVelocity = asteroidVelocity;
    }

    public void setAsteroidIsHazardous(boolean asteroidIsHazardous) {
        this.asteroidIsHazardous = asteroidIsHazardous;
    }

    public void setAsteroidUrl(String asteroidUrl) {
        this.asteroidUrl = asteroidUrl;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(asteroidId);
        dest.writeString(asteroidName);
        dest.writeDouble(asteroidDiameterMin);
        dest.writeDouble(asteroidDiameterMax);
        dest.writeString(asteroidApproachDate);
        dest.writeString(asteroidVelocity);
        dest.writeByte((byte) (asteroidIsHazardous ? 1 : 0));
        dest.writeString(asteroidUrl);
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