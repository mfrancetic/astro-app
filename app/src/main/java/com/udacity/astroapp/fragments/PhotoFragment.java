package com.udacity.astroapp.fragments;

import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInstaller;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.astroapp.R;
import com.udacity.astroapp.activities.MainActivity;
import com.udacity.astroapp.data.AppDatabase;
import com.udacity.astroapp.data.AppExecutors;
import com.udacity.astroapp.data.AstroAppWidget;
import com.udacity.astroapp.data.AstroDao;
import com.udacity.astroapp.data.PhotoViewModel;
import com.udacity.astroapp.data.PhotoViewModelFactory;
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


public class PhotoFragment extends Fragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = PhotoFragment.class.getSimpleName();

    private ImageView photoImageView;

    private TextView photoTitleTextView;

    private TextView photoDateTextView;

    private TextView photoDescriptionTextView;

    private Context context;

    private ScrollView photoScrollView;

    private String videoUrl;

    private static Uri videoUri;

    private Uri photoUri;

    private TextView emptyTextView;

    private ProgressBar loadingIndicator;

    private PhotoViewModel photoViewModel;

    private PhotoViewModelFactory photoViewModelFactory;

    private AppDatabase appDatabase;

    public static Photo photo;

    private int photoId;

    public static String photoTitle;

    private String photoDate;

    private String photoDescription;

    public static String photoUrl;


    private String photoMediaType;

    private Button playVideoButton;

    private static SharedPreferences sharedPreferences;

    private static final String preferenceId = "preferenceId";

    private static final String preferenceName = "preferenceName";

    private static final String preferenceTitle = "preferenceTitle";

    static final String preferences = "preferences";

    public static final String titleKey = "photoTitle";

    public static final String photoUrlKey = "photoUrl";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null) {
            getActivity().setTitle(R.string.menu_photo);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        if (getContext() != null) {
            sharedPreferences = getContext().getSharedPreferences(preferences, Context.MODE_PRIVATE);
        }

        View rootView = inflater.inflate(R.layout.fragment_photo, container, false);

        photoImageView = rootView.findViewById(R.id.photo_view);
        photoTitleTextView = rootView.findViewById(R.id.photo_title_text_view);
        photoDateTextView = rootView.findViewById(R.id.photo_date_text_view);
        playVideoButton = rootView.findViewById(R.id.play_video_button);

        playVideoButton.setVisibility(View.GONE);
//        playVideoButton.setVisibility(View.GONE);


        photoScrollView = rootView.findViewById(R.id.photo_scroll_view);

        loadingIndicator = rootView.findViewById(R.id.photo_loading_indicator);
        loadingIndicator.setVisibility(View.VISIBLE);
        emptyTextView = rootView.findViewById(R.id.photo_empty_text_view);

        appDatabase = AppDatabase.getInstance(getContext());

        photoViewModelFactory = new PhotoViewModelFactory(appDatabase);

        photoViewModel = ViewModelProviders.of(PhotoFragment.this, photoViewModelFactory).get(PhotoViewModel.class);

        photoViewModel.getPhotos().observe(PhotoFragment.this, new Observer<List<Photo>>() {
            @Override
            public void onChanged(@Nullable final List<Photo> photos) {
                photoViewModel.getPhotos().removeObserver(this);
                if (photos != null) {
                    AppExecutors.getExecutors().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            if (photoTitle != null) {
                                appDatabase.astroDao().deleteAllPhotos();
                                appDatabase.astroDao().addPhoto(photo);
//                                populatePhoto(photo);
                            }
                        }
                    });
                    /* Update the app widget */
                    AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                    Intent widgetIntent = new Intent(context, AstroAppWidget.class);
                    widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

                    /* Send the broadcast to update all the app widget id's */
                    int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context.getPackageName(), AstroAppWidget.class.getName()));
                    widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
                    context.sendBroadcast(widgetIntent);
                }
            }
        });


        emptyTextView.setVisibility(View.GONE);

        context = photoDateTextView.getContext();

        if (photoScrollView != null) {
            photoScrollView.requestFocus();
        }

        photoDescriptionTextView = rootView.findViewById(R.id.photo_description_text_view);

        new PhotoAsyncTask().execute();




        return rootView;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }


    private class PhotoAsyncTask extends AsyncTask<String, Void, Photo> {

        @Override
        protected Photo doInBackground(String... strings) {

            try {
                URL url = QueryUtils.createPhotoUrl();
                String photoJson = QueryUtils.makeHttpRequest(url);

                JSONObject photoObject = new JSONObject(photoJson);

//                JSONObject photoObjectPhoto = new JSONObject(photoJson);

//                JSONArray photoObjectArray = new JSONArray(photoJson);

//                for (int i = 0; i<photoObjectArray.length(); i++) {
//                    JSONObject photoObject = photoObjectArray.getJSONObject(i);

                photoTitle = photoObject.getString("title");

                photoDate = photoObject.getString("date");

                photoDescription = photoObject.getString("explanation");

                photoUrl = photoObject.getString("url");

                photoMediaType = photoObject.getString("media_type");

                photo = new Photo(0, photoTitle, photoDate, photoDescription, photoUrl, photoMediaType);

            } catch (
                    IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the photo JSON results");
            } catch (
                    JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the photo JSON response");
            }
            return photo;
        }

        @Override
        protected void onPostExecute(Photo photo) {
//            if (photo != null && photoTitle != null) {
            if (photo != null) {
                populatePhoto(photo);
//                astroDao.addPhoto(photo);
//            } else if (appDatabase.astroDao().loadAllPhotos() != null) {
            } else if (photoViewModel.getPhotos() != null && photoViewModel.getPhotos().getValue().size() != 0) {
//            } else if (appDatabase.astroDao().loadPhotoById(photoId) != null){
                LiveData<List<Photo>> photoDatabaseList = photoViewModel.getPhotos();
                List<Photo> photos = photoDatabaseList.getValue();
                photo = photos.get(0);
                if (photo != null) {
//                    photo = new Photo(photoId, null, null, null, null, null);
                    photoId = photo.getPhotoId();
                    photoTitle = photo.getPhotoTitle();
                    photoDate = photo.getPhotoDate();
                    photoDescription = photo.getPhotoDescription();
                    photoUrl = photo.getPhotoUrl();
                    photoMediaType = photo.getPhotoMediaType();
//                    if (photoDatabase.getValue().getPhotoId() != 0) {
                    photo.setPhotoId(photoId);
                    photo.setPhotoTitle(photoTitle);
                    photo.setPhotoDate(photoDate);
                    photo.setPhotoDescription(photoDescription);
                    photo.setPhotoUrl(photoUrl);
                    photo.setPhotoMediaType(photoMediaType);
                    populatePhoto(photo);

//                    astroDao.addPhoto(photo);
                }
            } else {
                loadingIndicator.setVisibility(View.GONE);
                photoTitleTextView.setVisibility(View.GONE);
                photoDescriptionTextView.setVisibility(View.GONE);
                photoDateTextView.setVisibility(View.GONE);
                photoImageView.setVisibility(View.INVISIBLE);
                emptyTextView.setVisibility(View.VISIBLE);
            }
            super.onPostExecute(photo);
        }
    }

    private void populatePhoto(Photo photo) {
        emptyTextView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);

        photoImageView.setVisibility(View.VISIBLE);
        photoDateTextView.setVisibility(View.VISIBLE);
        photoTitleTextView.setVisibility(View.VISIBLE);
        photoDescriptionTextView.setVisibility(View.VISIBLE);

        photoTitleTextView.setText(photo.getPhotoTitle());
        photoDateTextView.setText(photo.getPhotoDate());
        photoDescriptionTextView.setText(photo.getPhotoDescription());

        if (photoMediaType.equals("video")) {
            videoUrl = photo.getPhotoUrl();
            videoUri = Uri.parse(videoUrl);
            photoTitleTextView.setVisibility(View.VISIBLE);
            photoDescriptionTextView.setVisibility(View.VISIBLE);
            photoDateTextView.setVisibility(View.VISIBLE);
//            photoImageView.setVisibility(View.INVISIBLE);
            playVideoButton.setVisibility(View.VISIBLE);

            playVideoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent openVideoIntent = new Intent(Intent.ACTION_VIEW);
                    openVideoIntent.setData(videoUri);
                    startActivity(openVideoIntent);
                }
            });

        } else if (photo.getPhotoMediaType().equals("image")) {
            photoUri = Uri.parse(photo.getPhotoUrl());

            playVideoButton.setVisibility(View.GONE);

            Picasso.get().load(photoUri)
                    .into(photoImageView);
            photoImageView.setContentDescription(getString(R.string.photo_of_content_description) + " " + photoTitle);
        }

//        if (photo == null) {
//            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getContext());
//            Intent widgetIntent = new Intent(getContext(), AstroAppWidget.class);
//            widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//
//            /* Send a broadcast for all the app widget ids */
//            int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(getContext().getPackageName(),
//                    AstroAppWidget.class.getName()));
//            widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
//            getContext().sendBroadcast(widgetIntent);
//
//            /* Launch a new intent that opens the MainActivity */
//            Intent intent = new Intent(getContext(), MainActivity.class);
//            startActivity(intent);
//        }
//
        /* Update the app widget */
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Intent widgetIntent = new Intent(context, AstroAppWidget.class);
        widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        /* Send the broadcast to update all the app widget id's */
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context.getPackageName(), AstroAppWidget.class.getName()));
        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(widgetIntent);
//
    }
//

}