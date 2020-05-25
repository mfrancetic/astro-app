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
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.udacity.astroapp.R;
import com.udacity.astroapp.activities.MainActivity;
import com.udacity.astroapp.data.AppDatabase;
import com.udacity.astroapp.data.AppExecutors;
import com.udacity.astroapp.data.MarsPhotoViewModel;
import com.udacity.astroapp.data.MarsPhotoViewModelFactory;
import com.udacity.astroapp.models.Camera;
import com.udacity.astroapp.models.MarsPhoto;
import com.udacity.astroapp.models.MarsPhotoObject;
import com.udacity.astroapp.models.Rover;
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

    @BindView(R.id.mars_photo_scroll_view)
    ScrollView scrollView;

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
    private List<MarsPhoto> marsPhotos = new ArrayList<>();
    private MarsPhoto currentMarsPhoto;

    private String localDate;

    private Context context;

    private DatePickerDialog datePickerDialog;

    private int currentYear;
    private int currentMonth;
    private int currentDayOfMonth;

    private int currentMarsPhotoIndex;

    private AppDatabase database;
    private MarsPhotoViewModelFactory viewModelFactory;
    private MarsPhotoViewModel viewModel;
    private int scrollX;
    private int scrollY;
    private Observer<List<MarsPhoto>> observer;

    private boolean apiIsSuccessful = true;

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
        setupDatabase();
        setScrollListeners();
        setOnClickListeners();

        if (savedInstanceState == null) {
            getPhotoDataFromUrl();
        } else {
            getDataFromSavedInstanceState(savedInstanceState);
        }

        return rootView;
    }

    private void setScrollListeners() {
        if (scrollView != null) {
            scrollView.requestFocus();
            scrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    scrollX = scrollView.getScrollX();
                    scrollY = scrollView.getScrollY();
                }
            });
        }
    }

    private void getDataFromSavedInstanceState(Bundle savedInstanceState) {
        localDate = savedInstanceState.getString(Constants.MARS_CURRENT_DATE_KEY);
        setLocalDateToCalendar(localDate);
        scrollX = savedInstanceState.getInt(Constants.MARS_SCROLL_POSITION_X_KEY);
        scrollY = savedInstanceState.getInt(Constants.MARS_SCROLL_POSITION_Y_KEY);
        currentMarsPhoto = savedInstanceState.getParcelable(Constants.MARS_PHOTO_KEY);
        marsPhotos = savedInstanceState.getParcelableArrayList(Constants.MARS_PHOTOS_KEY);
        if (currentMarsPhoto != null) {
            displayPhoto(currentMarsPhoto);
        }
    }

    private void setupDatabase() {
        database = AppDatabase.getInstance(context);
        /* In case there is a MarsPhotoViewModelFactory, create a new instance */
        if (viewModelFactory == null) {
            viewModelFactory = new MarsPhotoViewModelFactory(database);
        }
        viewModel = ViewModelProviders.of(MarsPhotoFragment.this, viewModelFactory).get(MarsPhotoViewModel.class);
        observer = new Observer<List<MarsPhoto>>() {
            @Override
            public void onChanged(List<MarsPhoto> databaseMarsPhotos) {
                viewModel.getMarsPhotos().removeObserver(this);
                if (databaseMarsPhotos != null) {
                    if (databaseMarsPhotos.size() > 0 && marsPhotos.size() == 0) {
                        marsPhotos = databaseMarsPhotos;
                        displayPhoto(marsPhotos.get(0));
                        if (!apiIsSuccessful) {
                            Snackbar snackbar = Snackbar.make(scrollView, getString(R.string.snackbar_offline_mode), Snackbar.LENGTH_LONG);
                            snackbar.show();
                        }
                    }
                    AppExecutors.getExecutors().diskIO().execute(() -> {
                        if (marsPhotos != null && marsPhotos.size() > 0) {
                            database.astroDao().deleteAllMarsPhotos();
                            database.astroDao().addAllMarsPhotos(marsPhotos);
                        }
                    });
                }
            }
        };
        viewModel.getMarsPhotos().observe(getViewLifecycleOwner(), observer);
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

        previousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMarsPhoto = marsPhotos.get(currentMarsPhotoIndex - 1);
                displayPhoto(currentMarsPhoto);
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentMarsPhoto = marsPhotos.get(currentMarsPhotoIndex + 1);
                displayPhoto(currentMarsPhoto);
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
                    if (response.body().getPhotos().size() > 0) {
                        viewModel.getMarsPhotos().observe(getViewLifecycleOwner(), observer);
                        marsPhotos = new ArrayList<>();
                        marsPhotos.addAll(response.body().getPhotos());
                        currentMarsPhoto = marsPhotos.get(0);
                        displayPhoto(currentMarsPhoto);
                        apiIsSuccessful = true;
                    } else {
                        localDate = DateTimeUtils.getPreviousDate(localDate);
                        getPhotoDataFromUrl();
                    }
                }
            }

            @Override
            public void onFailure(Call<MarsPhotoObject> call, Throwable t) {
                apiIsSuccessful = false;
                viewModel.getMarsPhotos().observe(getViewLifecycleOwner(), observer);
                t.printStackTrace();
                LiveData<List<MarsPhoto>> marsPhotosDatabase = viewModel.getMarsPhotos();
                List<MarsPhoto> marsPhotosDatabaseList = marsPhotosDatabase.getValue();
                if (marsPhotosDatabaseList != null && marsPhotosDatabaseList.size() > 0) {
                    marsPhotos = marsPhotosDatabaseList;
                    displayPhoto(marsPhotos.get(0));
                    Snackbar snackbar = Snackbar.make(scrollView, getString(R.string.snackbar_offline_mode), Snackbar.LENGTH_LONG);
                    snackbar.show();
                } else {
                    setEmptyView();
                }
            }
        });
    }

    private void setEmptyView() {
        if (!MainActivity.isNetworkAvailable(context)) {
            emptyTextView.setText(R.string.no_internet_connection);
        } else {
            emptyTextView.setText(R.string.no_photo_found);
        }
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

    private void displayPhoto(MarsPhoto photo) {
        dateTextView.setText(photo.getEarthDate());

        Camera camera = photo.getCamera();
        if (camera != null) {
            String cameraName = getString(R.string.camera) + photo.getCamera().getCameraFullName();
            cameraTextView.setText(cameraName);
        }

        Rover rover = photo.getRover();
        if (rover != null) {
            String roverName = getString(R.string.rover) + rover.getRoverName();
            roverNameTextView.setText(roverName);

            String launchDate = getString(R.string.launch_date) + rover.getLaunchDate();
            String landingDate = getString(R.string.landing_date) + rover.getLandingDate();
            launchDateTextView.setText(launchDate);
            landingDateTextView.setText(landingDate);
        }

        Uri photoUri = Uri.parse(photo.getImageUrl());
        PhotoUtils.displayPhotoFromUrl(context, photoUri, photoView, loadingIndicator);

        photoView.setVisibility(View.VISIBLE);
        dateTextView.setVisibility(View.VISIBLE);
        launchDateTextView.setVisibility(View.VISIBLE);
        landingDateTextView.setVisibility(View.VISIBLE);
        sourceTextView.setVisibility(View.VISIBLE);
        roverNameTextView.setVisibility(View.VISIBLE);
        cameraTextView.setVisibility(View.VISIBLE);
        emptyTextView.setVisibility(View.GONE);
        emptyImageView.setVisibility(View.GONE);
        fab.show();
        landingDateTextView.setVisibility(View.VISIBLE);
        currentMarsPhotoIndex = marsPhotos.indexOf(currentMarsPhoto);
        if (currentMarsPhotoIndex < 1) {
            previousButton.setVisibility(View.GONE);
        } else {
            previousButton.setVisibility(View.VISIBLE);
        }

        if (currentMarsPhotoIndex == marsPhotos.size() - 1) {
            nextButton.setVisibility(View.GONE);
        } else {
            nextButton.setVisibility(View.VISIBLE);
        }
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

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putParcelable(Constants.MARS_PHOTO_KEY, currentMarsPhoto);
        outState.putParcelableArrayList(Constants.MARS_PHOTOS_KEY, (ArrayList<MarsPhoto>) marsPhotos);
        if (scrollView != null) {
            scrollX = scrollView.getScrollX();
            scrollY = scrollView.getScrollY();
        }
        outState.putString(Constants.MARS_CURRENT_DATE_KEY, localDate);
        outState.putInt(Constants.MARS_SCROLL_POSITION_X_KEY, scrollX);
        outState.putInt(Constants.MARS_SCROLL_POSITION_Y_KEY, scrollY);
    }
}