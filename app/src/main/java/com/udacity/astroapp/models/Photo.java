package com.udacity.astroapp.models;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.os.Parcel;
import android.os.Parcelable;

@Entity(tableName = "photo")
public class Photo implements Parcelable {

    @PrimaryKey
    private int photoId;

    private String photoTitle;

    private String photoDate;

    private String photoDescription;

    private String photoUrl;

    private String photoMediaType;


   public Photo(int photoId, String photoTitle, String photoDate,
          String photoDescription, String photoUrl, String photoMediaType) {
        this.photoId = photoId;
        this.photoTitle = photoTitle;
        this.photoDate = photoDate;
        this.photoDescription = photoDescription;
        this.photoUrl = photoUrl;
        this.photoMediaType = photoMediaType;
    }

     Photo(Parcel in) {
         in.writeInt(photoId);
         in.writeString(photoTitle);
         in.writeString(photoDate);
         in.writeString(photoDescription);
         in.writeString(photoUrl);
         in.writeString(photoMediaType);
    }

    public int getPhotoId() {
        return photoId;
    }

    public String getPhotoTitle() {
        return photoTitle;
    }

    public String getPhotoDate() {
        return photoDate;
    }

    public String getPhotoDescription() {
        return photoDescription;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public String getPhotoMediaType() {
        return photoMediaType;
    }

    public void setPhotoId(int photoId) {
        this.photoId = photoId;
    }

    public void setPhotoTitle(String photoTitle) {
        this.photoTitle = photoTitle;
    }

    public void setPhotoDate(String photoDate) {
        this.photoDate = photoDate;
    }

    public void setPhotoDescription(String photoDescription) {
        this.photoDescription = photoDescription;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public void setPhotoMediaType(String photoMediaType) {
        this.photoMediaType = photoMediaType;
    }

    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        @Override
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        @Override
        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(photoId);
        dest.writeString(photoTitle);
        dest.writeString(photoDate);
        dest.writeString(photoDescription);
        dest.writeString(photoUrl);
        dest.writeString(photoMediaType);
    }
}