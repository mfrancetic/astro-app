package com.udacity.astroapp.models;

import android.os.Parcel;
import android.os.Parcelable;

public class Photo implements Parcelable {

    private int photoId;

    private String photoTitle;

    private String photoDate;

    private String photoDescription;

    private String photoUrl;


   public Photo(int photoId, String photoTitle, String photoDate,
          String photoDescription, String photoUrl) {
        this.photoId = photoId;
        this.photoTitle = photoTitle;
        this.photoDate = photoDate;
        this.photoDescription = photoDescription;
        this.photoUrl = photoUrl;
    }

     Photo(Parcel in) {
         in.writeInt(photoId);
         in.writeString(photoTitle);
         in.writeString(photoDate);
         in.writeString(photoDescription);
         in.writeString(photoUrl);
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
    }
}