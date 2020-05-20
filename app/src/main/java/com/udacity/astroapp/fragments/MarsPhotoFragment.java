package com.udacity.astroapp.fragments;

import android.app.DatePickerDialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.udacity.astroapp.R;
import com.udacity.astroapp.models.MarsPhotoObject;
import com.udacity.astroapp.utils.Constants;
import com.udacity.astroapp.utils.DateTimeUtils;
import com.udacity.astroapp.utils.MarsPhotoService;
import com.udacity.astroapp.utils.PhotoUtils;
import com.udacity.astroapp.utils.RetrofitClientInstance;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MarsPhotoFragment extends Fragment {

    @BindView(R.id.mars_photo_loading_indicator)
    ProgressBar loadingIndicator;

    @BindView(R.id.mars_photo_empty_image_view)
    ImageView emptyImageView;

    @BindView(R.id.mars_photo_empty_text_view)
    TextView emptyTextView;

    @BindView(R.id.mars_photo_date_text_view)
    TextView dateTextView;

    @BindView(R.id.mars_photo_launch_date_text_view)
    TextView launchDateTextView;

    @BindView(R.id.mars_photo_landing_date_text_view)
    TextView landingDateTextView;

    @BindView(R.id.mars_photo_camera_text_view)
    TextView cameraTextView;

    @BindView(R.id.mars_photo_source_text_view)
    TextView sourceTextView;

    @BindView(R.id.mars_photo_rover_name)
    TextView roverNameTextView;

    @BindView(R.id.mars_photo_view)
    ImageView photoView;

    @BindView(R.id.mars_photo_previous_button)
    ImageButton previousButton;

    @BindView(R.id.mars_photo_next_button)
    ImageButton nextButton;

    @BindView(R.id.fab_mars_photo)
    FloatingActionButton fab;

    private MarsPhotoService marsPhotoService;
    private List<MarsPhotoObject.MarsPhoto> marsPhotos = new ArrayList<>();
    private MarsPhotoObject.MarsPhoto currentMarsPhoto;

    private String localDate;

    private Context context;

    private DatePickerDialog datePickerDialog;

    private int currentYear;
    private int currentMonth;
    private int currentDayOfMonth;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            getActivity().setTitle(getString(R.string.menu_mars_photo));
        }
        setRetainInstance(true);
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_mars_photo, container, false);
        context = rootView.getContext();
        ButterKnife.bind(this, rootView);

        localDate = DateTimeUtils.getCurrentLocalDate();
        setLocalDateToCalendar(localDate);

        createDatePickerDialog();
        setLoadingView();
        setOnClickListeners();
        getPhotoDataFromUrl();

        return rootView;
    }

    private void setOnClickListeners() {
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PhotoUtils.sharePhoto(context, currentMarsPhoto.getImageUrl());
            }
        });

        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri photoUri = Uri.parse(currentMarsPhoto.getImageUrl());
                PhotoUtils.displayPhotoDialog(context, photoUri);
            }
        });
    }

    private void setLocalDateToCalendar(String localDate) {
        currentYear = DateTimeUtils.getYear(localDate);
        currentMonth = DateTimeUtils.getMonth(localDate);
        currentDayOfMonth = DateTimeUtils.getDay(localDate);
    }

    private void getPhotoDataFromUrl() {
        marsPhotoService = RetrofitClientInstance.getRetrofitInstance().create(MarsPhotoService.class);
        Call<MarsPhotoObject> marsPhotoCalls = marsPhotoService.getMarsPhotoObject(localDate, Constants.NASA_API_KEY,
                Constants.PAGE_NUMBER);

        marsPhotoCalls.enqueue(new Callback<MarsPhotoObject>() {
            @Override
            public void onResponse(Call<MarsPhotoObject> call, Response<MarsPhotoObject> response) {
                if (response.body() != null) {
                    marsPhotos = response.body().getPhotos();
                    if (marsPhotos.size() > 0) {
                        currentMarsPhoto = marsPhotos.get(0);
                        displayPhoto(currentMarsPhoto);
                    } else {
                        localDate = DateTimeUtils.getPreviousDate(localDate);
                        getPhotoDataFromUrl();
                    }
                }
            }

            @Override
            public void onFailure(Call<MarsPhotoObject> call, Throwable t) {
                t.printStackTrace();
                setEmptyView();
            }
        });
    }

    private void setEmptyView() {
        loadingIndicator.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.VISIBLE);
        emptyImageView.setVisibility(View.VISIBLE);
        previousButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        photoView.setVisibility(View.INVISIBLE);
        dateTextView.setVisibility(View.GONE);
        cameraTextView.setVisibility(View.GONE);
        launchDateTextView.setVisibility(View.GONE);
        roverNameTextView.setVisibility(View.GONE);
        landingDateTextView.setVisibility(View.GONE);
        sourceTextView.setVisibility(View.GONE);
        fab.hide();
    }

    private void displayPhoto(MarsPhotoObject.MarsPhoto photo) {
        dateTextView.setText(photo.getEarthDate());
        cameraTextView.setText(photo.getCamera().getCameraFullName());
        roverNameTextView.setText(photo.getRover().getRoverName());

        String launchDate = getString(R.string.launch_date) + photo.getRover().getLaunchDate();
        String landingDate = getString(R.string.landing_date) + photo.getRover().getLandingDate();
        launchDateTextView.setText(launchDate);
        landingDateTextView.setText(landingDate);

        Uri photoUri = Uri.parse(photo.getImageUrl());
        PhotoUtils.displayPhotoFromUrl(context, photoUri, photoView, loadingIndicator);

        photoView.setVisibility(View.VISIBLE);
        dateTextView.setVisibility(View.VISIBLE);
        launchDateTextView.setVisibility(View.VISIBLE);
        landingDateTextView.setVisibility(View.VISIBLE);
        sourceTextView.setVisibility(View.VISIBLE);
        roverNameTextView.setVisibility(View.VISIBLE);
        cameraTextView.setVisibility(View.VISIBLE);
        fab.show();
        landingDateTextView.setVisibility(View.VISIBLE);
        loadingIndicator.setVisibility(View.GONE);
    }

    private void setLoadingView() {
        loadingIndicator.setVisibility(View.VISIBLE);
        emptyImageView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);
        previousButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        photoView.setVisibility(View.INVISIBLE);
        dateTextView.setVisibility(View.GONE);
        cameraTextView.setVisibility(View.GONE);
        launchDateTextView.setVisibility(View.GONE);
        roverNameTextView.setVisibility(View.GONE);
        landingDateTextView.setVisibility(View.GONE);
        sourceTextView.setVisibility(View.GONE);
        fab.hide();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        menu.findItem(R.id.menu_calendar).setVisible(true);
        super.onPrepareOptionsMenu(menu);
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
                (view, year, month, dayOfMonth) -> {
                    Calendar calendar = DateTimeUtils.getCalendar();
                    calendar.set(year, month, dayOfMonth);
                    calendar.setTimeZone(TimeZone.getDefault());
                    Date date = calendar.getTime();
                    localDate = DateTimeUtils.getFormattedDate(date);
                    setLoadingView();
                    getPhotoDataFromUrl();

                    currentYear = year;
                    currentMonth = month;
                    currentDayOfMonth = dayOfMonth;
                }, currentYear, currentMonth, currentDayOfMonth);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
    }
}