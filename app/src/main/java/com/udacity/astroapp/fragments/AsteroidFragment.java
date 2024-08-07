package com.udacity.astroapp.fragments;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;

import androidx.core.widget.NestedScrollView;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.udacity.astroapp.R;
import com.udacity.astroapp.activities.MainActivity;
import com.udacity.astroapp.adapters.AsteroidAdapter;
import com.udacity.astroapp.data.AppDatabase;
import com.udacity.astroapp.data.AppExecutors;
import com.udacity.astroapp.data.AsteroidViewModel;
import com.udacity.astroapp.data.AsteroidViewModelFactory;
import com.udacity.astroapp.databinding.FragmentAsteroidBinding;
import com.udacity.astroapp.models.Asteroid;
import com.udacity.astroapp.utils.QueryUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class AsteroidFragment extends Fragment {

    /* Tag for log messages */
    private static final String LOG_TAG = AsteroidFragment.class.getSimpleName();

    /* Instances of the AsteroidAdapter, List<Asteroid> and asteroid key */
    private AsteroidAdapter asteroidAdapter;
    private List<Asteroid> asteroidList;
    private static final String asteroidListKey = "asteroidList";

    private FragmentAsteroidBinding binding;

    private Context context;

    /* Views of the PhotoFragment */
    private NestedScrollView scrollView;

    private RecyclerView asteroidRecyclerView;

    private TextView emptyTextView;

    private ProgressBar loadingIndicator;

    private ImageView emptyImageView;

    /* Instances of the AppDatabase and ViewModel */
    private AppDatabase appDatabase;
    private AsteroidViewModelFactory asteroidViewModelFactory;
    private AsteroidViewModel asteroidViewModel;

    private String localDate;

    /* Boolean that indicates if a device is a phone or tablet */
    private boolean isTablet;

    /* Integer that indicates the orientation of the device */
    private int orientation;

    /* Scroll position X and Y keys */
    private static final String SCROLL_POSITION_X = "scrollPositionX";
    private static final String SCROLL_POSITION_Y = "scrollPositionY";

    /* Scroll positions X and Y values */
    private int scrollX;
    private int scrollY;

    private TimeZone timeZone;

    private Date date;

    private SimpleDateFormat formatter;

    private int currentYear;

    private int currentMonth;

    private int currentDayOfMonth;

    private Calendar calendar;

    private final static String currentDayKey = "currentDay";


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null) {
            /* Set the title of the activity */
            getActivity().setTitle(R.string.menu_asteroids);
        }
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            /* Set the title of the activity */
            getActivity().setTitle(R.string.menu_asteroids);
        }

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

        /* Inflate the fragment_asteroid.xml layout */
        binding = FragmentAsteroidBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        findViews();

        /* Get the boolean that indicates if a device is a tablet */
        isTablet = getResources().getBoolean(R.bool.isTablet);

        /* Get the orientation of the device */
        orientation = getResources().getConfiguration().orientation;

        setAsteroidLoadingIndicator();

        if (savedInstanceState != null) {
            /* In case there is a savedInstanceState, retrieve the asteroidList */
            asteroidList = savedInstanceState.getParcelableArrayList(asteroidListKey);
        }

        /* Create a new AsteroidAdapter */
        asteroidAdapter = new AsteroidAdapter(asteroidList);

        context = asteroidRecyclerView.getContext();
        setDividers(orientation);

        /* Set the adapter to the asteroidRecyclerView */
        asteroidRecyclerView.setAdapter(asteroidAdapter);

        /* Add a vertical divider to the asteroidRecyclerView*/
        asteroidRecyclerView.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));

        appDatabase = AppDatabase.getInstance(getContext());
        if (asteroidViewModelFactory == null) {
            asteroidViewModelFactory = new AsteroidViewModelFactory(appDatabase);
        }
        asteroidViewModel = ViewModelProviders.of(AsteroidFragment.this, asteroidViewModelFactory)
                .get(AsteroidViewModel.class);

        /* Observe the asteroids in the AsteroidFragment */
        asteroidViewModel.getAsteroids().observe(getViewLifecycleOwner(), new Observer<List<Asteroid>>() {
            @Override
            public void onChanged(@Nullable final List<Asteroid> asteroids) {
                asteroidViewModel.getAsteroids().removeObserver(this);
                if (asteroids != null) {
                    AppExecutors.getExecutors().diskIO().execute(() -> {
                        /* In case the asteroidList is not null and it is not empty,
                         * delete all asteroids and add the asteroidList to the database */
                        if (asteroidList != null && !asteroidList.isEmpty()) {
                            appDatabase.astroDao().deleteAllAsteroids();
                            appDatabase.astroDao().addAllAsteroids(asteroidList);
                        }
                    });
                }
            }
        });

        /* Get the current time, put in the SimpleDataFormat and format it to the localDate */
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        localDate = simpleDateFormat.format(date);

        /* Check if there in a savedInstanceState */
        if (savedInstanceState == null) {
            /* In case there is no savedInstanceState, execute an AsteroidAsyncTask */
            localDate = formatter.format(date);
            new AsteroidAsyncTask().execute();
        } else {
            /* In case there is a savedInstanceState, get the scroll positions, get the saved
             * asteroidList and populate the view with its values */
            asteroidList = savedInstanceState.getParcelableArrayList(asteroidListKey);
            scrollX = savedInstanceState.getInt(SCROLL_POSITION_X);
            scrollY = savedInstanceState.getInt(SCROLL_POSITION_Y);
            populateAsteroids(asteroidList);
        }
        return rootView;
    }

    private void findViews() {
        scrollView = binding.asteroidScrollView;
        asteroidRecyclerView = binding.asteroidRecyclerView;
        emptyTextView = binding.asteroidEmptyTextView;
        emptyImageView = binding.asteroidEmptyImageView;
        loadingIndicator = binding.asteroidLoadingIndicator;
    }

    /**
     * AsteroidAsyncTask class that creates the URL for loading the asteroidList, makes the HTTP request and
     * parses the JSON String in order to create a new List<Asteroid> object.
     * Returns a list of asteroids.
     */
    @SuppressLint("StaticFieldLeak")
    private class AsteroidAsyncTask extends AsyncTask<String, Void, List<Asteroid>> {

        @Override
        protected List<Asteroid> doInBackground(String... strings) {

            try {
                /* Create an URL and make a HTTP request */
                URL url = QueryUtils.createAsteroidUrl(localDate, localDate);
                String asteroidJson = QueryUtils.makeHttpRequest(url);

                JSONObject baseAsteroidResponse = new JSONObject(asteroidJson);
                JSONObject asteroidBaseObject = baseAsteroidResponse.getJSONObject("near_earth_objects");
                JSONArray asteroidArray = asteroidBaseObject.getJSONArray(localDate);

                /* For each asteroid in the asteroidArray, create an Asteroid object */
                for (int i = 0; i < asteroidArray.length(); i++) {
                    JSONObject asteroidObject = asteroidArray.getJSONObject(i);

                    /* Extract the value for the required keys */
                    int id = asteroidObject.getInt("id");
                    String asteroidName = asteroidObject.getString("name");
                    boolean asteroidIsHazardous = asteroidObject.getBoolean("is_potentially_hazardous_asteroid");
                    String asteroidUrl = asteroidObject.getString("nasa_jpl_url");
                    JSONObject diameterObject = asteroidObject.getJSONObject("estimated_diameter");
                    JSONObject diameterKilometersObject = diameterObject.getJSONObject("kilometers");
                    double asteroidDiameterMin = diameterKilometersObject.getDouble("estimated_diameter_min");
                    double asteroidDiameterMax = diameterKilometersObject.getDouble("estimated_diameter_max");
                    JSONArray approachDateArray = asteroidObject.getJSONArray("close_approach_data");

                    String asteroidApproachDate = null;
                    String asteroidVelocity = null;

                    /* For each item in the approachDateArray, retrieve the velocity and approach date */
                    for (int j = 0; j < approachDateArray.length(); j++) {
                        JSONObject approachDataObject = approachDateArray.getJSONObject(j);
                        asteroidApproachDate = approachDataObject.getString("close_approach_date");
                        JSONObject velocityObject = approachDataObject.getJSONObject("relative_velocity");
                        asteroidVelocity = velocityObject.getString("kilometers_per_second");
                    }

                    /* Create a new Asteroid object and set the values to it */
                    Asteroid asteroid = new Asteroid(id, asteroidName, asteroidDiameterMin, asteroidDiameterMax,
                            asteroidApproachDate,
                            asteroidVelocity, asteroidIsHazardous, asteroidUrl);

                    asteroid.setAsteroidId(id);
                    asteroid.setAsteroidName(asteroidName);
                    asteroid.setAsteroidDiameterMin(asteroidDiameterMin);
                    asteroid.setAsteroidDiameterMax(asteroidDiameterMax);
                    asteroid.setAsteroidVelocity(asteroidVelocity);
                    asteroid.setAsteroidApproachDate(asteroidApproachDate);
                    asteroid.setAsteroidIsHazardous(asteroidIsHazardous);
                    asteroid.setAsteroidUrl(asteroidUrl);

                    if (asteroidList == null) {
                        /* If there is no asteroidList, create a new one */
                        asteroidList = new ArrayList<>();
                    }
                    /* Add the asteroid to the asteroidList */
                    asteroidList.add(i, asteroid);
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the asteroid JSON results");
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the asteroid JSON response");
            }
            return asteroidList;
        }

        @Override
        protected void onPostExecute(List<Asteroid> newAsteroids) {
            if (newAsteroids != null) {
                /* If there is a list of asteroids available, populate the view with its values */
                populateAsteroids(newAsteroids);
            } else if (asteroidViewModel.getAsteroids().getValue() != null && !asteroidViewModel.getAsteroids().getValue().isEmpty()) {
                /* In case there are values stored in the AsteroidViewModel, retrieve those values */
                LiveData<List<Asteroid>> asteroidDatabaseList = asteroidViewModel.getAsteroids();
                asteroidList = asteroidDatabaseList.getValue();
                populateAsteroids(asteroidList);

                /* Create and show a Snackbar that informs the user that there is no Internet
                 * connectivity and the data is populated from the database */
                Snackbar snackbar = Snackbar.make(asteroidRecyclerView, getString(R.string.snackbar_offline_mode), Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                /* In case there are also no values stored in the database, hide all the
                 * views except the empty views */
                if (!MainActivity.isNetworkAvailable(context)) {
                    emptyTextView.setText(R.string.no_internet_connection);
                }
                asteroidRecyclerView.setVisibility(View.INVISIBLE);
                emptyTextView.setVisibility(View.VISIBLE);
                emptyImageView.setVisibility(View.VISIBLE);
                loadingIndicator.setVisibility(View.GONE);
            }
        }
    }

    private void populateAsteroids(List<Asteroid> asteroids) {
        /* Scroll to the X and Y position of the scrollView*/
        scrollView.scrollTo(scrollX, scrollY);
        /* Hide the empty views and loading indicator */
        loadingIndicator.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);
        emptyImageView.setVisibility(View.GONE);
        /* Set the asteroids to the asteroidAdapter and show the asteroidRecyclerView */
        asteroidAdapter.setAsteroids(asteroids);
        asteroidRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        orientation = getResources().getConfiguration().orientation;
        setDividers(orientation);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        /* Save the asteroidList and scroll positions in the savedInstanceState */
        outState.putParcelableArrayList(asteroidListKey, (ArrayList<? extends Parcelable>) asteroidList);
        scrollX = scrollView.getScrollX();
        scrollY = scrollView.getScrollY();
        outState.putString(currentDayKey, localDate);
        outState.putInt(SCROLL_POSITION_X, scrollX);
        outState.putInt(SCROLL_POSITION_Y, scrollY);
        super.onSaveInstanceState(outState);
    }

    private void setDividers(int orientation) {
        if (isTablet && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            /* In tablet landscape mode, add a horizontal divider and set the GridLayoutManager
             * to the asteroidRecyclerView */
            asteroidRecyclerView.addItemDecoration(new DividerItemDecoration(context,
                    DividerItemDecoration.HORIZONTAL));
            asteroidRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        } else {
            /* In all other case, set the LinearLayoutManager to the asteroidRecyclerView */
            asteroidRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        /* In all cases, add a vertical item decoration to the asteroidRecyclerView */
        asteroidRecyclerView.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));
    }

    public void setAsteroidLoadingIndicator() {
        /* Hide the empty views and show the loadingIndicator */
        asteroidRecyclerView.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);
        emptyImageView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);
    }

    @Override
    public void onPause() {
        if (MainActivity.isBeingRefreshed) {
            setAsteroidLoadingIndicator();
        }
        super.onPause();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_calendar).setVisible(true);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            return false;
        } else if (item.getItemId() == R.id.menu_calendar) {
            createDatePickerDialog();
            return true;
        }
        return false;
    }

    private void createDatePickerDialog() {
        long minDateLong = 0;

        try {
            String minDateString = "1995-06-16";
            Date minDate = formatter.parse(minDateString);
            minDateLong = Objects.requireNonNull(minDate).getTime();
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Problem parsing the minDate");
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(context,
//                R.style.AstroDialogTheme,
//                R.style.Theme_AppCompat_DayNight_Dialog_Alert,
                (view, year, month, dayOfMonth) -> {
                    calendar.set(year, month, dayOfMonth);
                    calendar.setTimeZone(timeZone);
                    date = calendar.getTime();
                    localDate = formatter.format(date);
                    new AsteroidAsyncTask().execute();

                    currentYear = year;
                    currentMonth = month;
                    currentDayOfMonth = dayOfMonth;
                }, currentYear, currentMonth, currentDayOfMonth);
        datePickerDialog.getDatePicker().setMinDate(minDateLong);
        datePickerDialog.show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}