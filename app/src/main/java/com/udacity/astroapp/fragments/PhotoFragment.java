package com.udacity.astroapp.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.udacity.astroapp.R;
import com.udacity.astroapp.models.Photo;
import com.udacity.astroapp.utils.QueryUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class PhotoFragment extends Fragment {

    private static final String LOG_TAG = PhotoFragment.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            getActivity().setTitle(R.string.menu_photo);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_photo, container, false);

        return rootView;
    }

    private class PhotoAsyncTask extends AsyncTask<String, Void, Photo> {

        Photo photo;


        @Override
        protected Photo doInBackground(String... strings) {

            try {
                URL url = QueryUtils.createPhotoUrl();
                String photoJson = QueryUtils.makeHttpRequest(url);

                JSONObject photoObject = new JSONObject(photoJson);

//                JSONArray photoArray = baseJsonResponse.getJSONArray(baseJsonResponse);

//                for (int i=0; i <baseJsonResponse.length(); i++) {

//                JSONObject photoObject = baseJsonResponse.getJSONObject();

                String photoTitle = photoObject.getString("title");

                String photoDate = photoObject.getString("date");

                String photoDescription = photoObject.getString("explanation");

                String photoUrl = photoObject.getString("url");

                int id = 0;

                photo = new Photo(id, photoTitle, photoDate, photoDescription, photoUrl);

                photo.setPhotoId(id);
                photo.setPhotoTitle(photoTitle);
                photo.setPhotoDate(photoDate);
                photo.setPhotoDescription(photoDescription);
                photo.setPhotoUrl(photoUrl);
            } catch (
                    IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the photo JSON results");
            } catch (
                    JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the photo JSON resposne");
            }
            return photo;
        }

        @Override
        protected void onPostExecute(Photo photo) {
            super.onPostExecute(photo);
        }
    }



}
