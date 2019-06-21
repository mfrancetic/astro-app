package com.udacity.astroapp.fragments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.udacity.astroapp.R;
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


public class AsteroidFragment extends Fragment {

    private static final String LOG_TAG = AsteroidFragment.class.getSimpleName();

    private AsteroidAdapter asteroidAdapter;

    private List<Asteroid> asteroidList;

    private Asteroid asteroid;

    private RecyclerView asteroidRecyclerView;

    private Date date;

    private Context context;

    private String localDate;

    private TextView emptyTextView;

    private ProgressBar loadingIndicator;

    private AppDatabase appDatabase;

    private AsteroidViewModelFactory asteroidViewModelFactory;

    private AsteroidViewModel asteroidViewModel;

    private boolean isTablet;

    private boolean isLandscape;

    private int orientation;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null) {
            getActivity().setTitle(R.string.menu_asteroids);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_asteroid, container, false);

        isTablet = getResources().getBoolean(R.bool.isTablet);

        orientation = getResources().getConfiguration().orientation;

//        asteroidList = new ArrayList<>();
        asteroidRecyclerView = rootView.findViewById(R.id.asteroid_recycler_view);

        asteroidAdapter = new AsteroidAdapter(asteroidList);

        context = asteroidRecyclerView.getContext();

        if (isTablet && orientation == Configuration.ORIENTATION_LANDSCAPE) {

            asteroidRecyclerView.addItemDecoration(new DividerItemDecoration(context,
                    DividerItemDecoration.HORIZONTAL));
            asteroidRecyclerView.addItemDecoration(new DividerItemDecoration(context,
                    DividerItemDecoration.VERTICAL));
            asteroidRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

//            Drawable dividerDrawable = ContextCompat.getDrawable(context, R.drawable.divider);
//            asteroidRecyclerView.addItemDecoration(new DividerItemDecoration(dividerDrawable));
            //            asteroidRecyclerView.setItemAnimator(new DefaultItemAnimator());
//            DividerItemDecoration verticalDivider = new DividerItemDecoration(context, DividerItemDecoration.VERTICAL);
//            verticalDivider.setDrawable(ContextCompat.getDrawable(context, R.drawable.divider));
//            asteroidRecyclerView.addItemDecoration(verticalDivider);
        } else {
            asteroidRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }

        asteroidRecyclerView.setAdapter(asteroidAdapter);


        asteroidRecyclerView.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));


        emptyTextView = rootView.findViewById(R.id.asteroid_empty_text_view);
        loadingIndicator = rootView.findViewById(R.id.asteroid_loading_indicator);
        emptyTextView.setVisibility(View.GONE);
        loadingIndicator.setVisibility(View.VISIBLE);

        appDatabase = AppDatabase.getInstance(getContext());

        asteroidViewModelFactory = new AsteroidViewModelFactory(appDatabase);
        asteroidViewModel = ViewModelProviders.of(AsteroidFragment.this, asteroidViewModelFactory)
                .get(AsteroidViewModel.class);

        asteroidViewModel.getAsteroids().observe(AsteroidFragment.this, new Observer<List<Asteroid>>() {
            @Override
            public void onChanged(@Nullable final List<Asteroid> asteroids) {
                asteroidViewModel.getAsteroids().removeObserver(this);
                if (asteroids != null) {
                    AppExecutors.getExecutors().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            int numberOfAsteroids = appDatabase.astroDao().getAsteroidCount();
                            if (asteroidList != null) {
                                appDatabase.astroDao().deleteAllAsteroids();

                                numberOfAsteroids = appDatabase.astroDao().getAsteroidCount();

//                                appDatabase.astroDao().addAsteroid(asteroid);
                                appDatabase.astroDao().addAllAsteroids(asteroidList);
                            }
                        }
                    });
                }
            }
        });

        date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        localDate = simpleDateFormat.format(date);

        new AsteroidAsyncTask().execute();
        return rootView;
    }

    private class AsteroidAsyncTask extends AsyncTask<String, Void, List<Asteroid>> {


        @Override
        protected List<Asteroid> doInBackground(String... strings) {

//            List<Asteroid> asteroidList = new ArrayList<>();

            try {
                URL url = QueryUtils.createAsteroidUrl(localDate, localDate);

                String asteroidJson = QueryUtils.makeHttpRequest(url);

                JSONObject baseAsteroidResponse = new JSONObject(asteroidJson);

                JSONObject asteroidBaseObject = baseAsteroidResponse.getJSONObject("near_earth_objects");

                JSONArray asteroidArray = asteroidBaseObject.getJSONArray(localDate);

                for (int i = 0; i < asteroidArray.length(); i++) {

                    JSONObject asteroidObject = asteroidArray.getJSONObject(i);

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

                    for (int j = 0; j < approachDateArray.length(); j++) {

                        JSONObject approachDataObject = approachDateArray.getJSONObject(j);

                        asteroidApproachDate = approachDataObject.getString("close_approach_date");

                        JSONObject velocityObject = approachDataObject.getJSONObject("relative_velocity");

                        asteroidVelocity = velocityObject.getString("kilometers_per_second");
                    }

                    asteroid = new Asteroid(id, asteroidName, asteroidDiameterMin, asteroidDiameterMax,
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
                        asteroidList = new ArrayList<>();
                    }
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
        protected void onPostExecute(List<Asteroid> asteroids) {
            if (asteroids != null) {
                populateAsteroids(asteroids);
            } else if (asteroidViewModel.getAsteroids() != null && asteroidViewModel.getAsteroids().getValue().size() != 0) {
                LiveData<List<Asteroid>> asteroidDatabaseList = asteroidViewModel.getAsteroids();
                asteroids = asteroidDatabaseList.getValue();
                populateAsteroids(asteroids);
            } else {
                asteroidRecyclerView.setVisibility(View.GONE);
                emptyTextView.setVisibility(View.VISIBLE);
                loadingIndicator.setVisibility(View.GONE);
            }
        }
    }

    private void populateAsteroids(List<Asteroid> asteroids) {
        loadingIndicator.setVisibility(View.GONE);
        emptyTextView.setVisibility(View.GONE);
        asteroidAdapter.setAsteroids(asteroids);
        asteroidRecyclerView.setVisibility(View.VISIBLE);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        orientation = getResources().getConfiguration().orientation;
        if (isTablet && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            asteroidRecyclerView.addItemDecoration(new DividerItemDecoration(context,
                    DividerItemDecoration.HORIZONTAL));
            asteroidRecyclerView.addItemDecoration(new DividerItemDecoration(context,
                    DividerItemDecoration.VERTICAL));
            asteroidRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        } else {
            asteroidRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        super.onConfigurationChanged(newConfig);
    }
}