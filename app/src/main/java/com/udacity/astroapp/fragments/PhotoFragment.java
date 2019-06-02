package com.udacity.astroapp.fragments;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.astroapp.R;
import com.udacity.astroapp.models.Photo;
import com.udacity.astroapp.utils.QueryUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class PhotoFragment extends Fragment {

    private static final String LOG_TAG = PhotoFragment.class.getSimpleName();

    private ImageView photoImageView;

    private TextView photoTitleTextView;

    private TextView photoDateTextView;

    private TextView photoDescriptionTextView;

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

        photoImageView = rootView.findViewById(R.id.photo_view);
        photoTitleTextView = rootView.findViewById(R.id.photo_title_text_view);
        photoDateTextView = rootView.findViewById(R.id.photo_date_text_view);

        photoDescriptionTextView = rootView.findViewById(R.id.photo_description_text_view);

        new PhotoAsyncTask().execute();

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

                String photoMediaType = photoObject.getString("media_type");

                int id = 0;

                photo = new Photo(id, photoTitle, photoDate, photoDescription, photoUrl, photoMediaType);

                photo.setPhotoId(id);
                photo.setPhotoTitle(photoTitle);
                photo.setPhotoDate(photoDate);
                photo.setPhotoDescription(photoDescription);
                photo.setPhotoUrl(photoUrl);
                photo.setPhotoMediaType(photoMediaType);
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
            if(photo != null) {
                populatePhoto(photo);
            }
            super.onPostExecute(photo);
        }

        private void populatePhoto(Photo photo) {
            photoTitleTextView.setText(photo.getPhotoTitle());
            photoDateTextView.setText(photo.getPhotoDate());
            photoDescriptionTextView.setText(photo.getPhotoDescription());

            if (photo.getPhotoMediaType().equals("video")) {

            } else if (photo.getPhotoMediaType().equals("image")) {
                Uri photoUri = Uri.parse(photo.getPhotoUrl());

                Picasso.get().load(photoUri)
                        .into(photoImageView);
            }
        }
    }
}
