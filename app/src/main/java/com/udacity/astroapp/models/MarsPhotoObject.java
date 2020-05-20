package com.udacity.astroapp.models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class MarsPhotoObject {

    @SerializedName("photos")
    private
    List<MarsPhoto> photos = new ArrayList<>();

    @Entity(tableName = "marsphoto")
    public static class MarsPhoto {

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

        @Ignore
        @SerializedName("camera")
        private Camera camera;

        @Ignore
        @SerializedName("rover")
        private Rover rover;

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

        public class Camera {

            public Camera() {
            }

            @SerializedName("name")
            private String cameraName;

            @SerializedName("full_name")
            private String cameraFullName;

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
        }

        public class Rover {

            public Rover() {
            }

            @SerializedName("name")
            private String roverName;

            @SerializedName("launch_date")
            private String launchDate;

            @SerializedName("landing_date")
            private String landingDate;

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