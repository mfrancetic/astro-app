package com.udacity.astroapp.fragments;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.udacity.astroapp.R;
import com.udacity.astroapp.data.AppDatabase;
import com.udacity.astroapp.data.AppExecutors;
import com.udacity.astroapp.data.AstroAppWidget;
import com.udacity.astroapp.data.PhotoViewModel;
import com.udacity.astroapp.data.PhotoViewModelFactory;
import com.udacity.astroapp.models.Photo;
import com.udacity.astroapp.utils.QueryUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoFragment extends Fragment {

    /* Tag for log messages */
    private static final String LOG_TAG = PhotoFragment.class.getSimpleName();

    /* Views of the PhotoFragment */
    @BindView(R.id.photo_view)
    ImageView photoImageView;

    @BindView(R.id.photo_title_text_view)
    TextView photoTitleTextView;

    @BindView(R.id.photo_date_text_view)
    TextView photoDateTextView;

    @BindView(R.id.photo_description_text_view)
    TextView photoDescriptionTextView;

    @BindView(R.id.photo_scroll_view)
    ScrollView photoScrollView;

    @BindView(R.id.photo_empty_text_view)
    TextView emptyTextView;

    @BindView(R.id.photo_loading_indicator)
    ProgressBar loadingIndicator;

    @BindView(R.id.photo_empty_image_view)
    ImageView emptyImageView;

    @BindView(R.id.play_video_button)
    Button playVideoButton;

    private Context context;

    /* Boolean that indicates the API call was not successful */
    private boolean jsonNotSuccessful;

    /* ViewModel and database instances */
    private PhotoViewModel photoViewModel;
    private PhotoViewModelFactory photoViewModelFactory;
    private AppDatabase appDatabase;

    /* Photo object and its values*/
    public static Photo photo;
    private static final String photoKey = "photo";
    public static String photoTitle;
    private String photoDate;
    private String photoDescription;
    public static String photoUrl;
    private String photoMediaType;
    public static Uri videoUri;

    private String localDate;

    /* Scroll position X and Y keys */
    private static final String SCROLL_POSITION_X = "scrollPositionX";
    private static final String SCROLL_POSITION_Y = "scrollPositionY";

    /* Scroll positions X and Y values */
    private int scrollX;
    private int scrollY;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null) {
            /* Set the title of the activity */
            getActivity().setTitle(R.string.menu_photo);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        jsonNotSuccessful = false;

        if (getActivity() != null) {
            /* Set the title of the activity */
            getActivity().setTitle(R.string.menu_photo);
        }

        /* Inflate the fragment_photo.xml layout */
        View rootView = inflater.inflate(R.layout.fragment_photo, container, false);
        ButterKnife.bind(this, rootView);

        /* Hide the playVideoButton and empty views, and show the loadingIndicator */
        playVideoButton.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);
        emptyImageView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);

        appDatabase = AppDatabase.getInstance(getContext());

        /* In case there is a photoViewModelFactory, create a new instance */
        if (photoViewModelFactory == null) {
            photoViewModelFactory = new PhotoViewModelFactory(appDatabase);
        }

        photoViewModel = ViewModelProviders.of(PhotoFragment.this, photoViewModelFactory).get(PhotoViewModel.class);

        /* Observe the photos in the PhotoFragment */
        photoViewModel.getPhotos().observe(PhotoFragment.this, new Observer<List<Photo>>() {
            @Override
            public void onChanged(@Nullable final List<Photo> photos) {
                photoViewModel.getPhotos().removeObserver(this);
                if (photos != null) {
                    AppExecutors.getExecutors().diskIO().execute(() -> {
                        /* In case the photo is not null and it does not have empty values,
                         * delete all photos and add the photo to the database */
                        if (photo != null && !photo.getPhotoDate().isEmpty()) {
                            appDatabase.astroDao().deleteAllPhotos();
                            appDatabase.astroDao().addPhoto(photo);
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

        context = photoDateTextView.getContext();

        if (photoScrollView != null) {
            photoScrollView.requestFocus();
        }

        /* Get the current time, put in the SimpleDataFormat and format it to the localDate */
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        localDate = simpleDateFormat.format(date);

        /* Check if there in a savedInstanceState */
        if (savedInstanceState == null) {
            /* In case there is no savedInstanceState, execute a PhotoAsyncTask */
            new PhotoAsyncTask().execute();
        } else {
            /* In case there is a savedInstanceState, get the scroll positions, get the saved
             * photo and populate the view with its values */
            scrollX = savedInstanceState.getInt(SCROLL_POSITION_X);
            scrollY = savedInstanceState.getInt(SCROLL_POSITION_Y);
            getActivity().overridePendingTransition(0, 0);
            photo = savedInstanceState.getParcelable(photoKey);
            if (photo != null) {
                populatePhoto(photo);
            }
        }
        return rootView;
    }

    /**
     * PhotoAsyncTask class that creates the URL for loading the photo, makes the HTTP request and
     * parses the JSON String in order to create a new Photo object.
     * Returns a list of photos.
     */
    @SuppressLint("StaticFieldLeak")
    private class PhotoAsyncTask extends AsyncTask<String, Void, Photo> {

        @Override
        protected Photo doInBackground(String... strings) {

            try {
                /* Create an URL and make a HTTP request */
                URL url = QueryUtils.createPhotoUrl(localDate);
                String photoJson = QueryUtils.makeHttpRequest(url);

                /* Create a new photoObject */
                JSONObject photoObject = new JSONObject(photoJson);

                /* Extract the value for the required keys */
                photoTitle = photoObject.getString("title");
                photoDate = photoObject.getString("date");
                photoDescription = photoObject.getString("explanation");
                photoUrl = photoObject.getString("url");
                photoMediaType = photoObject.getString("media_type");

                /* Create a new Photo object and set the values to it */
                photo = new Photo(0, photoTitle, photoDate, photoDescription, photoUrl, photoMediaType);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the photo JSON results");
                jsonNotSuccessful = true;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the photo JSON response");
                jsonNotSuccessful = true;
            }
            return photo;
        }

        @Override
        protected void onPostExecute(Photo newPhoto) {
            if (newPhoto != null && !jsonNotSuccessful) {
                /* If there is a photo available, and the API call was successful,
                populate the photo in the view */
                populatePhoto(newPhoto);
            } else if (photoViewModel.getPhotos().getValue() != null && photoViewModel.getPhotos().getValue().size() != 0) {
                /* In case there are values stored in the PhotoViewModel, retrieve those values */
                LiveData<List<Photo>> photoDatabaseList = photoViewModel.getPhotos();
                List<Photo> photos = photoDatabaseList.getValue();
                photo = photos.get(0);
                if (photo != null) {
                    /* In case there is a photo in the database, retrieve its values and populate
                     * the views */
                    int photoId = photo.getPhotoId();
                    photoTitle = photo.getPhotoTitle();
                    photoDate = photo.getPhotoDate();
                    photoDescription = photo.getPhotoDescription();
                    photoUrl = photo.getPhotoUrl();
                    photoMediaType = photo.getPhotoMediaType();
                    photo.setPhotoId(photoId);
                    photo.setPhotoTitle(photoTitle);
                    photo.setPhotoDate(photoDate);
                    photo.setPhotoDescription(photoDescription);
                    photo.setPhotoUrl(photoUrl);
                    photo.setPhotoMediaType(photoMediaType);
                    populatePhoto(photo);

                    /* Create and show a Snackbar that informs the user that there is no Internet
                     * connectivity and the data is populated from the database */
                    Snackbar snackbar = Snackbar.make(photoScrollView, getString(R.string.snackbar_offline_mode), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            } else {
                /* In case there are also no values stored in the database, hide all the
                 * views except the empty views */
                loadingIndicator.setVisibility(View.GONE);
                photoTitleTextView.setVisibility(View.GONE);
                photoDescriptionTextView.setVisibility(View.GONE);
                photoDateTextView.setVisibility(View.GONE);
                photoImageView.setVisibility(View.INVISIBLE);
                emptyTextView.setVisibility(View.VISIBLE);
                emptyImageView.setVisibility(View.VISIBLE);
            }
            super.onPostExecute(photo);
        }
    }

    private void populatePhoto(Photo photo) {
        if (photoScrollView != null) {
            /* Scroll to the X and Y position of the photoScrollView*/
            photoScrollView.scrollTo(scrollX, scrollY);
        }

        /* Hide the empty views and loading indicator */
        emptyTextView.setVisibility(View.GONE);
        emptyImageView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.GONE);

        /* Show the photoImageView and all the TextViews */
        photoImageView.setVisibility(View.VISIBLE);
        photoDateTextView.setVisibility(View.VISIBLE);
        photoTitleTextView.setVisibility(View.VISIBLE);
        photoDescriptionTextView.setVisibility(View.VISIBLE);

        /* Set text of the photoTitleTextView, photoDateTextView and photoDescriptionTextView */
        if (photo.getPhotoTitle() != null && photo.getPhotoDate() != null && photo.getPhotoDescription() != null) {
            photoTitleTextView.setText(photo.getPhotoTitle());
            photoDateTextView.setText(photo.getPhotoDate());
            photoDescriptionTextView.setText(photo.getPhotoDescription());
        }
        if (Build.VERSION.SDK_INT >= 26) {
            photoDescriptionTextView.setJustificationMode(Layout.JUSTIFICATION_MODE_INTER_WORD);
        }

        if (photoMediaType != null && photoMediaType.equals("video")) {
            /* If the photoMediaType exists and equals a video, get the URL,
             * parse it show the playVideoButton*/
            String videoUrl = photo.getPhotoUrl();
            if (videoUrl != null) {
                videoUri = Uri.parse(videoUrl);
                playVideoButton.setVisibility(View.VISIBLE);
                photoImageView.setVisibility(View.GONE);

                /* Set an OnClickListener to the playVideoButton */
                playVideoButton.setOnClickListener(v -> {
                    /* OnClick, create and start an intent that opens the URL of the video */
                    Intent openVideoIntent = new Intent(Intent.ACTION_VIEW);
                    openVideoIntent.setData(videoUri);
                    startActivity(openVideoIntent);
                });
            }

        } else if (photo.getPhotoMediaType().equals("image")) {
            /* In case the media type equals an image, hide the playVideoButton*/
            playVideoButton.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);

            /* Get the photoUrl and load it into the photoImageView */
            Uri photoUri = Uri.parse(photo.getPhotoUrl());
            if (photoUri != null) {
                Picasso picasso = new Picasso.Builder(context).build();
                picasso.load(photoUri).into(photoImageView);

                /* Set the content description of the photoImageView to inform the user about the photo's title */
                photoImageView.setContentDescription(getString(R.string.photo_of_content_description) + " " + photoTitle);
            }
        }
        /* Update the app widget */
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Intent widgetIntent = new Intent(context, AstroAppWidget.class);
        widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        /* Send the broadcast to update all the app widget id's */
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context.getPackageName(), AstroAppWidget.class.getName()));
        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(widgetIntent);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        /* Save the photo and scroll positions in the savedInstanceState */
        outState.putParcelable(photoKey, photo);
        if (photoScrollView != null) {
            scrollX = photoScrollView.getScrollX();
            scrollY = photoScrollView.getScrollY();
        }
        outState.putInt(SCROLL_POSITION_X, scrollX);
        outState.putInt(SCROLL_POSITION_Y, scrollY);
        super.onSaveInstanceState(outState);
    }
}