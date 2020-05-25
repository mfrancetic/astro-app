package com.udacity.astroapp.models;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.google.gson.annotations.SerializedName;
import com.udacity.astroapp.utils.Constants;
import com.udacity.astroapp.utils.Converters;

import java.util.ArrayList;
import java.util.List;

public class MarsPhotoObject {

    @SerializedName("photos")
    private
    List<MarsPhoto> photos = new ArrayList<>();

    @Entity(tableName = "marsphoto")
    public static class MarsPhoto implements Parcelable {

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

        public MarsPhoto(Parcel in) {
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

        public static class Camera implements Parcelable {

            public Camera() {
            }

            @SerializedName("name")
            private String cameraName;

            @SerializedName("full_name")
            private String cameraFullName;

            protected Camera(Parcel in) {
                cameraName = in.readString();
                cameraFullName = in.readString();
            }

            public static Creator<Camera> CREATOR = new Creator<Camera>() {
                @Override
                public Camera createFromParcel(Parcel in) {
                    return new Camera(in);
                }

                @Override
                public Camera[] newArray(int size) {
                    return new Camera[size];
                }
            };

            public String getCameraName() {
                return cameraName;
            }

            public void setCameraName(String cameraName) {
                this.cameraName = cameraName;
            }

            public String getCameraFullName() {
                return cameraFullName;
            }

            public void setCameraFullName(String cameraFullName) {
                this.cameraFullName = cameraFullName;
            }

            @Override
            public int describeContents() {
                return 0;
            }

            @Override
            public void writeToParcel(Parcel dest, int flags) {
                dest.writeString(cameraName);
                dest.writeString(cameraFullName);
            }
        }

        public static class Rover implements Parcelable {

            public Rover() {
            }

            @SerializedName("name")
            private String roverName;

            @SerializedName("launch_date")
            private String launchDate;

            @SerializedName("landing_date")
            private String landingDate;

            public Rover(Parcel in) {
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
        }
    }

    public MarsPhotoObject() {
    }

    public List<MarsPhoto> getPhotos() {
        return photos;
    }

    public void setPhotos(List<MarsPhoto> photos) {
        this.photos = photos;
    }
}