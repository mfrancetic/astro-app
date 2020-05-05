package com.udacity.astroapp.fragments;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.udacity.astroapp.R;
import com.udacity.astroapp.activities.MainActivity;
import com.udacity.astroapp.data.AppDatabase;
import com.udacity.astroapp.data.AppExecutors;
import com.udacity.astroapp.data.EarthPhotoViewModel;
import com.udacity.astroapp.data.EarthPhotoViewModelFactory;
import com.udacity.astroapp.models.EarthPhoto;
import com.udacity.astroapp.utils.DateTimeUtils;
import com.udacity.astroapp.utils.PhotoUtils;
import com.udacity.astroapp.utils.QueryUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EarthPhotoFragment extends Fragment {

    private static final String LOG_TAG = EarthPhotoFragment.class.getSimpleName();

    @BindView(R.id.earth_photo_coordinator_layout)
    CoordinatorLayout earthPhotoCoordinatorLayout;

    @BindView(R.id.earth_scroll_view)
    ScrollView earthScrollView;

    @BindView(R.id.earth_photo_view)
    ImageView earthPhotoView;

    @BindView(R.id.earth_photo_caption_text_view)
    TextView earthPhotoCaptionTextView;

    @BindView(R.id.earth_photo_date_time_text_view)
    TextView earthPhotoDateTimeTextView;

    @BindView(R.id.earth_photo_source_text_view)
    TextView earthPhotoSourceTextView;

    @BindView(R.id.earth_photo_fab)
    FloatingActionButton earthPhotoFab;

    @BindView(R.id.earth_photo_empty_image_view)
    ImageView earthPhotoEmptyImageView;

    @BindView(R.id.earth_photo_empty_text_view)
    TextView earthPhotoEmptyTextView;

    @BindView(R.id.earth_photo_loading_indicator)
    ProgressBar earthPhotoLoadingIndicator;

    private Context context;

    private String earthPhotoIdentifier;
    private String earthPhotoCaption;
    private String earthPhotoDateTime;
    private String earthPhotoImageUrl;

    private EarthPhoto earthPhoto;
    private List<EarthPhoto> earthPhotos;

    private boolean jsonNotSuccessful;
    private AppDatabase appDatabase;
    private EarthPhotoViewModelFactory earthPhotoViewModelFactory;
    private EarthPhotoViewModel earthPhotoViewModel;

    private String localDate;
    private DatePickerDialog datePickerDialog;

    private int currentYear;
    private int currentMonth;
    private int currentDayOfMonth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null) {
            /* Set the title of the activity */
            getActivity().setTitle(R.string.menu_earth_photo);
        }
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_earth_photo, container, false);
        context = rootView.getContext();
        ButterKnife.bind(this, rootView);

        localDate = DateTimeUtils.getCurrentLocalDate();
        setLocalDateToCalendar(localDate);

        setLoadingView();
        createDatePickerDialog();
        setupDatabase();
        new EarthPhotoAsyncTask().execute();

        return rootView;
    }

    private void setLocalDateToCalendar(String localDate) {
        currentYear = DateTimeUtils.getYear(localDate);
        currentMonth = DateTimeUtils.getMonth(localDate);
        currentDayOfMonth = DateTimeUtils.getDay(localDate);
    }

    private void setupDatabase() {
        appDatabase = AppDatabase.getInstance(context);
        /* In case there is an earthPhotoViewModelFactory, create a new instance */
        if (earthPhotoViewModelFactory == null) {
            earthPhotoViewModelFactory = new EarthPhotoViewModelFactory(appDatabase);
        }
        earthPhotoViewModel = ViewModelProviders.of(EarthPhotoFragment.this, earthPhotoViewModelFactory).get(EarthPhotoViewModel.class);

        earthPhotoViewModel.getEarthPhotos().observe(getViewLifecycleOwner(), new Observer<List<EarthPhoto>>() {
            @Override
            public void onChanged(List<EarthPhoto> databaseEarthPhotos) {
                earthPhotoViewModel.getEarthPhotos().removeObserver(this);
                if (earthPhotos != null && earthPhotos.size() > 0) {
                    AppExecutors.getExecutors().diskIO().execute(() -> {
                        appDatabase.astroDao().deleteAllEarthPhotos();
                        appDatabase.astroDao().addAllEarthPhotos(earthPhotos);
                    });
                }
            }
        });
    }

    private void populatePhoto(EarthPhoto photo) {
        String photoDate = photo.getEarthPhotoDateTime().substring(0, 10);
        URL earthPhotoUrl = QueryUtils.createEarthPhotoImageUrl(photoDate, photo.getEarthPhotoUrl());
        Uri earthPhotoUri = Uri.parse(earthPhotoUrl.toString());
        PhotoUtils.displayPhotoFromUrl(context, earthPhotoUri, earthPhotoView);

        earthPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoUtils.displayPhotoDialog(context, earthPhotoUri);
            }
        });

        earthPhotoFab.show();
        earthPhotoFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoUtils.sharePhoto(context, String.valueOf(earthPhotoUri));
            }
        });

        PhotoUtils.addScrollingFunctionalityToFab(earthScrollView, earthPhotoFab);

        earthPhotoView.setVisibility(View.VISIBLE);
        earthPhotoLoadingIndicator.setVisibility(View.GONE);
        earthPhotoSourceTextView.setVisibility(View.VISIBLE);
        earthPhotoCaptionTextView.setText(photo.getEarthPhotoCaption());
        earthPhotoCaptionTextView.setVisibility(View.VISIBLE);
        earthPhotoDateTimeTextView.setVisibility(View.VISIBLE);
        earthPhotoDateTimeTextView.setText(photo.getEarthPhotoDateTime());
    }

    private void setLoadingView() {
        earthPhotoLoadingIndicator.setVisibility(View.VISIBLE);
        earthPhotoEmptyImageView.setVisibility(View.GONE);
        earthPhotoEmptyTextView.setVisibility(View.GONE);
        earthPhotoView.setVisibility(View.GONE);
        earthPhotoFab.hide();
        earthPhotoSourceTextView.setVisibility(View.GONE);
    }

    @SuppressLint("StaticFieldLeak")
    private class EarthPhotoAsyncTask extends AsyncTask<String, Void, List<EarthPhoto>> {

        @Override
        protected List<EarthPhoto> doInBackground(String... strings) {
            try {
                URL url = QueryUtils.createEarthPhotoUrl(localDate);
                String earthPhotoJson = QueryUtils.makeHttpRequest(url);

                JSONArray earthPhotoArray = new JSONArray(earthPhotoJson);
                while (earthPhotoArray.length() < 1) {
                    localDate = DateTimeUtils.getPreviousDate(localDate);
                    if (localDate != null) {
                        url = QueryUtils.createEarthPhotoUrl(localDate);
                        earthPhotoJson = QueryUtils.makeHttpRequest(url);
                        earthPhotoArray = new JSONArray(earthPhotoJson);
                    }
                }

                for (int i = 0; i < earthPhotoArray.length(); i++) {
                    JSONObject earthPhotoObject = earthPhotoArray.getJSONObject(i);
                    earthPhotoIdentifier = earthPhotoObject.getString("identifier");
                    earthPhotoCaption = earthPhotoObject.getString("caption");
                    earthPhotoImageUrl = earthPhotoObject.getString("image");
                    earthPhotoDateTime = earthPhotoObject.getString("date");

                    earthPhoto = new EarthPhoto(earthPhotoIdentifier, earthPhotoCaption, earthPhotoImageUrl, earthPhotoDateTime);
                    if (earthPhotos == null) {
                        earthPhotos = new ArrayList<>();
                    }
                    earthPhotos.add(earthPhoto);
                }
                jsonNotSuccessful = false;
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the photo JSON results");
                jsonNotSuccessful = true;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the photo JSON response");
                jsonNotSuccessful = true;
            }
            return earthPhotos;
        }

        @Override
        protected void onPostExecute(List<EarthPhoto> newPhotos) {
            if (newPhotos != null && !jsonNotSuccessful) {
                // display the latest Earth photo
                populatePhoto(newPhotos.get(newPhotos.size() - 1));
            } else if (earthPhotoViewModel.getEarthPhotos().getValue() != null && earthPhotoViewModel.getEarthPhotos().getValue().size() != 0) {
                LiveData<List<EarthPhoto>> earthPhotosDatabase = earthPhotoViewModel.getEarthPhotos();
                List<EarthPhoto> earthPhotosList = earthPhotosDatabase.getValue();
                if (earthPhotosList.size() > 0) {
                    earthPhotos = earthPhotosList;
                    populatePhoto(earthPhotos.get(earthPhotos.size() - 1));
                }
                Snackbar snackbar = Snackbar.make(earthScrollView, getString(R.string.snackbar_offline_mode), Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                displayEmptyView();
            }
            super.onPostExecute(newPhotos);
        }
    }

    private void displayEmptyView() {
        if (!MainActivity.isNetworkAvailable(context)) {
            earthPhotoEmptyTextView.setText(R.string.no_internet_connection);
        } else {
            earthPhotoEmptyTextView.setText(R.string.no_photo_found);
        }
        earthPhotoCaptionTextView.setVisibility(View.GONE);
        earthPhotoDateTimeTextView.setVisibility(View.GONE);
        earthPhotoView.setVisibility(View.GONE);
        earthPhotoSourceTextView.setVisibility(View.GONE);
        earthPhotoLoadingIndicator.setVisibility(View.GONE);

        earthPhotoEmptyImageView.setVisibility(View.VISIBLE);
        earthPhotoEmptyTextView.setVisibility(View.VISIBLE);
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
            datePickerDialog.show();
            return true;
        }
        return false;
    }

    private void createDatePickerDialog() {
        datePickerDialog = new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, final int year, int month, int dayOfMonth) {
                        Calendar calendar = DateTimeUtils.getCalendar();
                        calendar.set(year, month, dayOfMonth);
                        calendar.setTimeZone(TimeZone.getDefault());
                        Date date = calendar.getTime();
                        localDate = DateTimeUtils.getFormattedDate(date);
                        new EarthPhotoAsyncTask().execute();

                        currentYear = year;
                        currentMonth = month;
                        currentDayOfMonth = dayOfMonth;
                    }
                }, currentYear, currentMonth, currentDayOfMonth);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.menu_calendar).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }
}