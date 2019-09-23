package com.udacity.astroapp.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toolbar;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.udacity.astroapp.R;
import com.udacity.astroapp.activities.MainActivity;
import com.udacity.astroapp.data.AppDatabase;
import com.udacity.astroapp.data.AppExecutors;
import com.udacity.astroapp.data.AstroAppWidget;
import com.udacity.astroapp.data.PhotoViewModel;
import com.udacity.astroapp.data.PhotoViewModelFactory;
import com.udacity.astroapp.models.Photo;
import com.udacity.astroapp.utils.QueryUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class PhotoFragment extends Fragment {

    /* Tag for log messages */
    private static final String LOG_TAG = PhotoFragment.class.getSimpleName();

    /* Views of the PhotoFragment */
    @BindView(R.id.photo_coordinator_layout)
    CoordinatorLayout photoCoordinatorLayout;

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

    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;

    @BindView(R.id.photo_video_source_text_view)
    TextView photoVideoSourceTextView;

    @BindView(R.id.photo_previous_button)
    ImageButton photoPreviousButton;

    @BindView(R.id.photo_next_button)
    ImageButton photoNextButton;

    @BindView(R.id.photo_calendar_button)
    ImageButton photoCalendarButton;

    private Context context;

    private Uri photoUri;

    /* Boolean that indicates the API call was not successful */
    private boolean jsonNotSuccessful;

    private boolean isDialogShown;

    private final static String IS_DIALOG_SHOWN_KEY = "isDialogShown";

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

    private Dialog dialog;

    private TimeZone timeZone;

    private Date date;

    private SimpleDateFormat formatter;

    private int currentYear;

    private int currentMonth;

    private int currentDayOfMonth;

    private Calendar calendar;

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

        calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);
        currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        if (getActivity() != null) {
            /* Set the title of the activity */
            getActivity().setTitle(R.string.menu_photo);
        }

        /* Inflate the fragment_photo.xml layout */
        View rootView = inflater.inflate(R.layout.fragment_photo, container, false);
        ButterKnife.bind(this, rootView);


        context = photoDateTextView.getContext();

        setPhotoLoadingIndicator();

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


        if (photoScrollView != null) {
            photoScrollView.requestFocus();
            photoScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    scrollX = photoScrollView.getScrollX();
                    scrollY = photoScrollView.getScrollY();
                    /* Hide the floatingActionButton when scrolling down, and show it when scrolling up*/
                    if (scrollY > 0 || scrollY < 0 && floatingActionButton.isShown()) {
                        floatingActionButton.hide();
                    } else {
                        floatingActionButton.show();
                    }
                }
            });
        }

        /* Get the current time, put in the SimpleDataFormat and UTC time zone and format it to the localDate */
        formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        timeZone = TimeZone.getTimeZone("America/Chicago");
        date = new Date();
        formatter.setTimeZone(timeZone);
        localDate = formatter.format(date);

        /* Check if there in a savedInstanceState */
        if (savedInstanceState == null) {
            /* In case there is no savedInstanceState, execute a PhotoAsyncTask */
            new PhotoAsyncTask().execute();
        } else {
            /* In case there is a savedInstanceState, get the scroll positions, get the saved
             * photo and populate the view with its values */
            isDialogShown = savedInstanceState.getBoolean(IS_DIALOG_SHOWN_KEY);
            scrollX = savedInstanceState.getInt(SCROLL_POSITION_X);
            scrollY = savedInstanceState.getInt(SCROLL_POSITION_Y);
            getActivity().overridePendingTransition(0, 0);
            photo = savedInstanceState.getParcelable(photoKey);
            if (photo != null) {
                populatePhoto(photo);
            }
            if (isDialogShown) {
                showPhotoDialog();
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
                if (!MainActivity.isNetworkAvailable(context)) {
                    emptyTextView.setText(R.string.no_internet_connection);
                }
                /* In case there are also no values stored in the database, hide all the
                 * views except the empty views */
                loadingIndicator.setVisibility(View.GONE);
                photoTitleTextView.setVisibility(View.GONE);
                photoDescriptionTextView.setVisibility(View.GONE);
                photoDateTextView.setVisibility(View.GONE);
                photoPreviousButton.setVisibility(View.GONE);
                photoNextButton.setVisibility(View.GONE);
                floatingActionButton.hide();
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
        photoVideoSourceTextView.setVisibility(View.VISIBLE);
        photoDescriptionTextView.setVisibility(View.VISIBLE);
        photoCalendarButton.setVisibility(View.VISIBLE);

        photoCalendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                int minimumYear = 1995;
                int minimumMonth = 6;
                int minimumDayOfMonth = 16;

                DatePickerDialog datePickerDialog = new DatePickerDialog(context, new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, final int year, int month, int dayOfMonth) {
                        calendar.set(year, month, dayOfMonth);
                        date = calendar.getTime();
                        localDate = formatter.format(date);
                        new PhotoAsyncTask().execute();

                        currentYear = year;
                        currentMonth = month;
                        currentDayOfMonth = dayOfMonth;
                    }
                }, currentYear, currentMonth, currentDayOfMonth);
                datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
                // 16.06.1995
                datePickerDialog.show();
            }
        });

        floatingActionButton.show();

        photoPreviousButton.setVisibility(View.VISIBLE);
        photoPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                date = getPreviousDate(date);
                localDate = formatter.format(date);
                new PhotoAsyncTask().execute();
            }
        });
        Date currentDate = new Date();
        Date nextDate = getNextDate(date);
        if (nextDate.after(currentDate)) {
            photoNextButton.setVisibility(View.GONE);
        } else {
            photoNextButton.setVisibility(View.VISIBLE);
            photoNextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    date = getNextDate(date);
                    localDate = formatter.format(date);
                    new PhotoAsyncTask().execute();

                }
            });
        }


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
            photoUri = Uri.parse(photo.getPhotoUrl());
            if (photoUri != null) {
                Picasso picasso = new Picasso.Builder(context).build();
                picasso.load(photoUri).into(photoImageView);

                /* Set the content description of the photoImageView to inform the user about the photo's title */
                photoImageView.setContentDescription(getString(R.string.photo_of_content_description) + " " + photoTitle);

                photoImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showPhotoDialog();
                    }
                });
            }
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (photo.getPhotoMediaType().equals("image")) {
                    floatingActionButton.setContentDescription(getString(R.string.share_photo_content_description));
                    Picasso picasso = new Picasso.Builder(context).build();
                    picasso.load(photoUrl).into(new Target() {
                        @Override
                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                            intent.setType("image/*");
                            intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(bitmap, context));
                            startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_photo_content_description)));
                        }

                        @Override
                        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
                        }

                        @Override
                        public void onPrepareLoad(Drawable placeHolderDrawable) {
                        }
                    });
                } else if (photo.getPhotoMediaType().equals("video")) {
                    floatingActionButton.setContentDescription(getString(R.string.share_video_content_description));
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, photoUrl);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_video_content_description)));
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
        outState.putBoolean(IS_DIALOG_SHOWN_KEY, isDialogShown);
        super.onSaveInstanceState(outState);
    }

    /**
     * Get local bitmap uri, in order to share the photo
     */
    public Uri getLocalBitmapUri(Bitmap bitmap, Context context) {
        Uri bitmapUri = null;
        try {
            File file = new File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                    "share_image_" + System.currentTimeMillis() + ".png");
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fileOutputStream);
            fileOutputStream.close();
            bitmapUri = FileProvider.getUriForFile(context, context.getApplicationContext()
                    .getPackageName() + ".provider", file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bitmapUri;
    }

    void showPhotoDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.fullscreen);
        dialog.show();

        isDialogShown = true;

        ImageView fullScreenImageView = dialog.findViewById(R.id.photo_full_screen_view);
        Picasso picasso = new Picasso.Builder(context).build();
        picasso.load(photoUri).into(fullScreenImageView);
        fullScreenImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isDialogShown = false;
                dialog.dismiss();
            }
        });

        dialog.setOnDismissListener(dialog -> {
            isDialogShown = false;
        });
    }

    public void setPhotoLoadingIndicator() {
        loadingIndicator.setVisibility(View.VISIBLE);
        playVideoButton.setVisibility(View.GONE);
        photoCalendarButton.setVisibility(View.GONE);
        photoImageView.setVisibility(View.GONE);
        emptyImageView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);
        photoDateTextView.setVisibility(View.GONE);
        photoDescriptionTextView.setVisibility(View.GONE);
        photoTitleTextView.setVisibility(View.GONE);
        photoVideoSourceTextView.setVisibility(View.GONE);
        photoPreviousButton.setVisibility(View.GONE);
        photoNextButton.setVisibility(View.GONE);
    }

    @Override
    public void onPause() {
        if (MainActivity.isBeingRefreshed) {
            setPhotoLoadingIndicator();
        }
        super.onPause();
    }

    private Date getPreviousDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
//        Date newDate = new Date(date* 1000 -  24 * 60 * 60 * 1000);
//        return new Date (date* 24*60*60*1000);
    }

    private Date getNextDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, +1);
        return calendar.getTime();
    }

}