package com.udacity.astroapp.fragments;

import android.annotation.SuppressLint;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.udacity.astroapp.R;
import com.udacity.astroapp.activities.MainActivity;
import com.udacity.astroapp.adapters.AsteroidAdapter;
import com.udacity.astroapp.data.AppDatabase;
import com.udacity.astroapp.data.AppExecutors;
import com.udacity.astroapp.data.AsteroidViewModel;
import com.udacity.astroapp.data.AsteroidViewModelFactory;
import com.udacity.astroapp.models.Asteroid;
import com.udacity.astroapp.utils.QueryUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AsteroidFragment extends Fragment {

    /* Tag for log messages */
    private static final String LOG_TAG = AsteroidFragment.class.getSimpleName();

    /* Instances of the AsteroidAdapter, List<Asteroid> and asteroid key */
    private AsteroidAdapter asteroidAdapter;
    private List<Asteroid> asteroidList;
    private static final String asteroidListKey = "asteroidList";

    private Context context;

    /* Views of the PhotoFragment */
    @BindView(R.id.asteroid_scroll_view)
    NestedScrollView scrollView;

    @BindView(R.id.asteroid_recycler_view)
    RecyclerView asteroidRecyclerView;

    @BindView(R.id.asteroid_empty_text_view)
    TextView emptyTextView;

    @BindView(R.id.asteroid_loading_indicator)
    ProgressBar loadingIndicator;

    @BindView(R.id.asteroid_empty_image_view)
    ImageView emptyImageView;

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

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null) {
            /* Set the title of the activity */
            getActivity().setTitle(R.string.menu_asteroids);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            /* Set the title of the activity */
            getActivity().setTitle(R.string.menu_asteroids);
        }

        /* Inflate the fragment_asteroid.xml layout */
        View rootView = inflater.inflate(R.layout.fragment_asteroid, container, false);
        ButterKnife.bind(this, rootView);

        /* Get the boolean that indicates if a device is a tablet */
        isTablet = getResources().getBoolean(R.bool.isTablet);

        /* Get the orientation of the device */
        orientation = getResources().getConfiguration().orientation;

        /* Hide the empty views and show the loadingIndicator */
        emptyTextView.setVisibility(View.GONE);
        emptyImageView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);

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
        asteroidViewModel.getAsteroids().observe(AsteroidFragment.this, new Observer<List<Asteroid>>() {
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
            } else if (asteroidViewModel.getAsteroids().getValue() != null && asteroidViewModel.getAsteroids().getValue().size() != 0) {
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
    public void onConfigurationChanged(Configuration newConfig) {
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
}