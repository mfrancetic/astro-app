package com.udacity.astroapp.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.text.LineBreaker;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView;
import com.udacity.astroapp.R;
import com.udacity.astroapp.activities.MainActivity;
import com.udacity.astroapp.data.AppDatabase;
import com.udacity.astroapp.data.AppExecutors;
import com.udacity.astroapp.data.AstroAppWidget;
import com.udacity.astroapp.data.GlideApp;
import com.udacity.astroapp.data.PhotoViewModel;
import com.udacity.astroapp.data.PhotoViewModelFactory;
import com.udacity.astroapp.databinding.FragmentPhotoBinding;
import com.udacity.astroapp.models.Photo;
import com.udacity.astroapp.utils.PhotoUtils;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhotoFragment extends Fragment {

    /* Tag for log messages */
    private static final String LOG_TAG = PhotoFragment.class.getSimpleName();

    private FragmentPhotoBinding binding;

    /* Views of the PhotoFragment */
    private ImageView photoImageView;

    private TextView photoTitleTextView;

    private TextView photoDateTextView;

    private TextView photoDescriptionTextView;

    private ScrollView photoScrollView;

    private TextView emptyTextView;

    private ProgressBar loadingIndicator;

    private ImageView emptyImageView;

    private FloatingActionButton floatingActionButton;

    private TextView photoVideoSourceTextView;

    private ImageButton photoPreviousButton;

    private ImageButton photoNextButton;

    private ImageButton playVideoButton;

    private Context context;

    private Uri photoUri;

    private String videoId;

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

    private final static String currentDayKey = "currentDay";

    private static final int MILLIS_IN_DAY = 1000 * 60 * 60 * 24;

    private Date selectedDate;

    private DatePickerDialog datePickerDialog;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null) {
            /* Set the title of the activity */
            getActivity().setTitle(R.string.menu_photo);
        }
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        jsonNotSuccessful = false;

        /* Get the current time, put in the SimpleDataFormat and UTC time zone and format it to the localDate */
        formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        timeZone = TimeZone.getTimeZone("America/Chicago");
        date = new Date();
        formatter.setTimeZone(timeZone);

        calendar = Calendar.getInstance();
        calendar.setTimeZone(timeZone);

        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);
        currentDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        if (getActivity() != null) {
            /* Set the title of the activity*/
            getActivity().setTitle(R.string.menu_photo);
        }

        /* Inflate the fragment_photo.xml layout */
        binding = FragmentPhotoBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        findViews();

        context = photoDateTextView.getContext();

        appDatabase = AppDatabase.getInstance(getContext());

        /* In case there is a photoViewModelFactory, create a new instance */
        if (photoViewModelFactory == null) {
            photoViewModelFactory = new PhotoViewModelFactory(appDatabase);
        }

        photoViewModel = ViewModelProviders.of(PhotoFragment.this, photoViewModelFactory).get(PhotoViewModel.class);

        setLoadingView();

        /* Observe the photos in the PhotoFragment */
        photoViewModel.getPhotos().observe(getViewLifecycleOwner(), new Observer<List<Photo>>() {
            @Override
            public void onChanged(@Nullable final List<Photo> photos) {
                photoViewModel.getPhotos().removeObserver(this);
                if (photos != null) {
                    AppExecutors.getExecutors().diskIO().execute(() -> {
                        /* In case the photo is not null and it does not have empty values,
                         * delete all photos and add the photo to the database */
                        if (photo != null && photo.getPhotoUrl() != null && !photo.getPhotoUrl().isEmpty()) {
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
            photoScrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
                scrollX = photoScrollView.getScrollX();
                scrollY = photoScrollView.getScrollY();
                /* Hide the floatingActionButton when scrolling down, and show it when scrolling up*/
                if (!isUnplayableVideo(photoUrl)) {
                    if (scrollY > 0 || scrollY < 0 && floatingActionButton.isShown()) {
                        floatingActionButton.hide();
                    } else {
                        floatingActionButton.show();
                    }
                }
            });
        }

        /* Check if there in a savedInstanceState */
        if (savedInstanceState == null) {
            localDate = formatter.format(date);
            /* In case there is no savedInstanceState, execute a PhotoAsyncTask */
            new PhotoAsyncTask().execute();
        } else {
            /* In case there is a savedInstanceState, get the scroll positions, get the saved
             * photo and populate the rootView with its values */
            localDate = savedInstanceState.getString(currentDayKey);
            isDialogShown = savedInstanceState.getBoolean(IS_DIALOG_SHOWN_KEY);
            scrollX = savedInstanceState.getInt(SCROLL_POSITION_X);
            scrollY = savedInstanceState.getInt(SCROLL_POSITION_Y);
            requireActivity().overridePendingTransition(0, 0);
            photo = savedInstanceState.getParcelable(photoKey);
            if (photo != null) {
                populatePhoto(photo);
            } else {
                showEmptyView();
            }
            if (isDialogShown) {
                showPhotoDialog();
            }
        }
        return rootView;
    }

    private void findViews() {
        photoImageView = binding.photoView;
        photoTitleTextView = binding.photoTitleTextView;
        photoDateTextView = binding.photoDateTextView;
        photoDescriptionTextView = binding.photoDescriptionTextView;
        photoScrollView = binding.photoScrollView;
        emptyTextView = binding.photoEmptyTextView;
        emptyImageView = binding.photoEmptyImageView;
        loadingIndicator = binding.photoLoadingIndicator;
        photoVideoSourceTextView = binding.photoVideoSourceTextView;
        floatingActionButton = binding.fab;
        photoPreviousButton = binding.photoPreviousButton;
        photoNextButton = binding.photoNextButton;
        playVideoButton = binding.playVideoButton;
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

                jsonNotSuccessful = false;
                /* Create a new Photo object and set the values to it */
                photo = new Photo(0, photoTitle, photoDate, photoDescription, photoUrl, photoMediaType);
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the photo JSON results");
                jsonNotSuccessful = true;
                photo = new Photo(localDate);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the photo JSON response");
                jsonNotSuccessful = true;
                photo = new Photo(localDate);
            }
            return photo;
        }

        @Override
        protected void onPostExecute(Photo newPhoto) {
            if (newPhoto != null && newPhoto.getPhotoUrl() != null && !jsonNotSuccessful) {
                /* If there is a photo available, and the API call was successful,
                populate the photo in the view */
                populatePhoto(newPhoto);
            } else if (photoViewModel.getPhotos().getValue() != null && !photoViewModel.getPhotos().getValue().isEmpty()) {
                /* In case there are values stored in the PhotoViewModel, retrieve those values */
                LiveData<List<Photo>> photoDatabaseList = photoViewModel.getPhotos();
                List<Photo> photos = photoDatabaseList.getValue();
                photo = photos.get(0);
                if (photo != null && photo.getPhotoUrl() != null && !photo.getPhotoUrl().isEmpty()) {
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
                } else {
                    emptyTextView.setText(R.string.no_photo_found);
                }
                /* In case there are also no values stored in the database, hide all the
                 * views except the empty views */
                showEmptyView();
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

        floatingActionButton.show();

        selectedDate = getSelectedDate(photo.getPhotoDate());
        date = selectedDate;

        createDatePickerDialog();

        SimpleDateFormat simpleFormatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        String datePickerDate = simpleFormatter.format(selectedDate);
        String monthString = datePickerDate.substring(5, 7);
        int month = Integer.parseInt(monthString);
        datePickerDialog.getDatePicker().updateDate(Integer.parseInt(datePickerDate.substring(0, 4)),
                month - 1, Integer.parseInt(datePickerDate.substring(8, 10)));

        /* Set text of the photoTitleTextView, photoDateTextView and photoDescriptionTextView */
        if (photo.getPhotoTitle() != null) {
            photoTitleTextView.setText(photo.getPhotoTitle());
        }

        if (photo.getPhotoDate() != null) {
            photoDateTextView.setText(photo.getPhotoDate());
        }

        if (photo.getPhotoDescription() != null) {
            photoDescriptionTextView.setText(photo.getPhotoDescription());
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            photoDescriptionTextView.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);
        }

        if (photoMediaType != null && photoMediaType.equals("video")) {
            /* If the photoMediaType exists and equals a video, get the URL,
             * parse it show the playVideoButton*/
            String videoUrl = photo.getPhotoUrl();
            if (videoUrl != null) {
                if (!isUnplayableVideo(photoUrl)) {
                    photoImageView.setVisibility(View.GONE);
                    floatingActionButton.show();
                    videoId = extractYoutubeId(videoUrl);
                    playVideoButton.setVisibility(View.VISIBLE);
                    playVideoButton.setOnClickListener(view -> {
                        if (getActivity() != null) {
                            showVideoDialog();
                        }
                    });
                } else {
                    // If there is another type of video, show a placeholder image and hide the
                    // share button
                    playVideoButton.setVisibility(View.GONE);
                    photoImageView.setVisibility(View.VISIBLE);
                    photoImageView.setImageResource(R.mipmap.ic_launcher);
                    photoImageView.setClickable(false);
                    floatingActionButton.hide();
                }
            }
        } else if (photo.getPhotoMediaType() != null && photo.getPhotoMediaType().equals("image")) {
            playVideoButton.setVisibility(View.GONE);
            photoImageView.setVisibility(View.VISIBLE);
            photoImageView.setClickable(true);

            /* Get the photoUrl and load it into the photoImageView */
            photoUri = Uri.parse(photo.getPhotoUrl());
            if (photoUri != null) {
                PhotoUtils.displayPhotoFromUrl(context, photoUri, photoImageView, loadingIndicator);

                /* Set the content description of the photoImageView to inform the user about the photo's title */
                photoImageView.setContentDescription(getString(R.string.photo_of_content_description) + " " + photoTitle);

                photoImageView.setOnClickListener(v -> showPhotoDialog());
            }
        }

        floatingActionButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            if (photo.getPhotoMediaType().equals("image")) {
                floatingActionButton.setContentDescription(getString(R.string.share_photo_content_description));
                Glide.
                        with(context)
                        .asBitmap()
                        .load(photoUrl)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                intent.setType("image/*");
                                intent.putExtra(Intent.EXTRA_STREAM, getLocalBitmapUri(resource, context));
                                startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_photo_content_description)));
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            } else if (photo.getPhotoMediaType().equals("video")) {
                if (!isUnplayableVideo(photoUrl)) {
                    floatingActionButton.show();
                    floatingActionButton.setContentDescription(getString(R.string.share_video_content_description));
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT, photoUrl);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    startActivity(Intent.createChooser(intent, getResources().getString(R.string.share_video_content_description)));
                } else {
                    floatingActionButton.hide();
                }
            }
        });

        setPreviousAndNextButtons();

        /* Update the app widget */
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        Intent widgetIntent = new Intent(context, AstroAppWidget.class);
        widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        /* Send the broadcast to update all the app widget id's */
        int[] ids = appWidgetManager.getAppWidgetIds(new ComponentName(context.getPackageName(), AstroAppWidget.class.getName()));
        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        context.sendBroadcast(widgetIntent);
    }

    private void setPreviousAndNextButtons() {
        selectedDate = getSelectedDate(photo.getPhotoDate());
        Date nextDate = getNextDate(selectedDate);
        Date todaysDate = getTodaysDate();

        photoPreviousButton.setVisibility(View.VISIBLE);
        photoPreviousButton.setOnClickListener(v -> {
            date = getPreviousDate(selectedDate);
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            localDate = formatter.format(date);
            new PhotoAsyncTask().execute();
        });
        if (nextDate.after(todaysDate)) {
            photoNextButton.setVisibility(View.GONE);
        } else {
            photoNextButton.setVisibility(View.VISIBLE);
            photoNextButton.setOnClickListener(v -> {
                date = getNextDate(selectedDate);
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                localDate = formatter.format(date);
                new PhotoAsyncTask().execute();
            });
        }
    }

    private void showEmptyView() {
        loadingIndicator.setVisibility(View.GONE);
        photoTitleTextView.setVisibility(View.GONE);
        photoDescriptionTextView.setVisibility(View.GONE);
        photoDateTextView.setVisibility(View.GONE);
        photoPreviousButton.setVisibility(View.GONE);
        photoNextButton.setVisibility(View.GONE);
        photoVideoSourceTextView.setVisibility(View.INVISIBLE);
        photoImageView.setVisibility(View.INVISIBLE);
        emptyTextView.setVisibility(View.VISIBLE);
        emptyImageView.setVisibility(View.VISIBLE);
        floatingActionButton.hide();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        /* Save the photo and scroll positions in the savedInstanceState */
        outState.putParcelable(photoKey, photo);
        if (photoScrollView != null) {
            scrollX = photoScrollView.getScrollX();
            scrollY = photoScrollView.getScrollY();
        }
        outState.putString(currentDayKey, localDate);
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

        GlideApp.
                with(context)
                .load(photoUri)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(fullScreenImageView);

        fullScreenImageView.setOnClickListener(v -> {
            isDialogShown = false;
            dialog.dismiss();
        });

        dialog.setOnDismissListener(dialog -> isDialogShown = false);
    }

    void showVideoDialog() {
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(true);
        dialog.setContentView(R.layout.fullscreen_video);
        dialog.show();

        isDialogShown = true;
        if (getActivity() != null) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }

        YouTubePlayerView fullScreenVideoPlayerView = dialog.findViewById(R.id.youtube_player_view);
        getLifecycle().addObserver(fullScreenVideoPlayerView);

        fullScreenVideoPlayerView.addYouTubePlayerListener(new AbstractYouTubePlayerListener() {
            @Override
            public void onReady(@NonNull YouTubePlayer youTubePlayer) {
                youTubePlayer.loadVideo(videoId, 0);
            }

            @Override
            public void onError(@NonNull YouTubePlayer youTubePlayer, @NonNull PlayerConstants.PlayerError error) {
                Toast.makeText(getActivity(), getString(R.string.video_not_playable), Toast.LENGTH_SHORT).show();
                Log.e(LOG_TAG, "Failed to open a video with URL " + videoId + error.name());
                super.onError(youTubePlayer, error);
                isDialogShown = false;
                dialog.dismiss();
            }
        });

        fullScreenVideoPlayerView.setOnClickListener(v -> {
            isDialogShown = false;
            dialog.dismiss();
        });

        dialog.setOnDismissListener(dialog -> {
            isDialogShown = false;
            fullScreenVideoPlayerView.release();
            if (getActivity() != null) {
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            }
        });
    }

    public void setLoadingView() {
        loadingIndicator.setVisibility(View.GONE);
        playVideoButton.setVisibility(View.GONE);
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
            setLoadingView();
        }
        super.onPause();
    }

    private Date getPreviousDate(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, -1);
        return calendar.getTime();
    }

    private Date getNextDate(Date date) {
//        return date.getTime() + MILLIS_IN_DAY;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(Calendar.DATE, +1);
        return calendar.getTime();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_calendar).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            return false;
        } else if (id == R.id.menu_calendar) {
            if (datePickerDialog == null) {
                createDatePickerDialog();
            }
            datePickerDialog.show();
            return true;
        }
        return false;
    }

    private void createDatePickerDialog() {
        long minDateLong = 0;

        try {
            String minDateString = "1995-06-16";
            Date minDate = formatter.parse(minDateString);
            if (minDate != null) {
                minDateLong = minDate.getTime();
            }
        } catch (Exception e) {
            Log.e(LOG_TAG, "Problem parsing the minDate");
        }

        datePickerDialog = new DatePickerDialog(context,
//                R.style.AstroDialogTheme,
//                R.style.Theme_AppCompat_DayNight_Dialog_Alert,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    calendar.setTimeZone(timeZone);
                    date = calendar.getTime();
                    localDate = formatter.format(date);
                    new PhotoAsyncTask().execute();

                    currentYear = year;
                    currentMonth = month;
                    currentDayOfMonth = dayOfMonth;
                }, currentYear, currentMonth, currentDayOfMonth);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.getDatePicker().setMinDate(minDateLong);
//        datePickerDialog.show();
    }

    private String extractYoutubeId(String videoUrl) {
        String videoId = null;
        Pattern pattern = Pattern.compile("^https?://.*(?:youtu.be/|v/|u/\\w/|embed/|watch?v=)([^#&?]*).*$",
                Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(videoUrl);
        if (matcher.matches()) {
            videoId = matcher.group(1);
        }
        return videoId;
    }

    private boolean isUnplayableVideo(String photoUrl) {
        if (photoMediaType != null && photoUrl != null) {
            return photoMediaType.contains("video") && !photoUrl.contains("youtube");
        } else {
            return false;
        }
    }

    private Date getSelectedDate(String selectedDate) {
        String yearString = selectedDate.substring(0, 4);
        String monthString = selectedDate.substring(5, 7);
        String dayString = selectedDate.substring(8, 10);
        int month = Integer.parseInt(monthString);
        month = month - 1;
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, Integer.parseInt(yearString));
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(dayString));
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private Date getTodaysDate() {
        timeZone = TimeZone.getTimeZone("America/Chicago");
        Date todaysDate = new Date();
        formatter.setTimeZone(timeZone);
        calendar = Calendar.getInstance();
        calendar.setTimeZone(timeZone);
        return todaysDate;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}