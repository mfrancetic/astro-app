package com.udacity.astroapp.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.udacity.astroapp.R;
import com.udacity.astroapp.adapters.AsteroidAdapter;
import com.udacity.astroapp.models.Asteroid;
import com.udacity.astroapp.utils.QueryUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class AsteroidFragment extends Fragment {

    private static final String LOG_TAG = AsteroidFragment.class.getSimpleName();

    private String date;

    private AsteroidAdapter asteroidAdapter;

    private List<Asteroid> asteroidList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            getActivity().setTitle(R.string.menu_asteroids);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_asteroid, container, false);

        asteroidList = new ArrayList<>();

//       LocalDate localDate = LocalDate.now();

        new AsteroidAsyncTask().execute();



        return rootView;
    }

    private class AsteroidAsyncTask extends AsyncTask<String, Void, List<Asteroid>> {



        @Override
        protected List<Asteroid> doInBackground(String... strings) {

            List<Asteroid> asteroidList = new ArrayList<>();

            try {
                URL url = QueryUtils.createAsteroidUrl("2019-06-01", "2019-06-01");
                String asteroidJson = QueryUtils.makeHttpRequest(url);

                JSONObject baseAsteroidResponse = new JSONObject(asteroidJson);

//                JSONArray asteroidBaseArray = new JSONArray(asteroidJson);

                JSONObject asteroidBaseObject = baseAsteroidResponse.getJSONObject("near_earth_objects");

//                JSONArray asteroidBaseArray = baseAsteroidResponse.getJSONArray("near_earth_objects");

//                JSONArray asteroidArray = asteroidBaseArray.getJSONArray("2019-06-01");

//                JSONObject asteroidBaseObject = asteroidBaseArray.getJSONObject()

//                JSONArray asteroidArray = asteroidBaseArray.getJSONArray(2);

//                JSONObject dateObject = asteroidBaseObject.getJSONObject("2019-06-03");

                JSONArray asteroidArray = asteroidBaseObject.getJSONArray("2019-06-01");

                for (int i = 0; i < asteroidArray.length(); i++) {

                    JSONObject asteroidObject = asteroidArray.getJSONObject(i);

                    int id = asteroidObject.getInt("id");

                    String asteroidName = asteroidObject.getString("name");

                    boolean asteroidIsHazardous = asteroidObject.getBoolean("is_potentially_hazardous_asteroid");

                    String asteroidUrl = asteroidObject.getString("nasa_jpl_url");

                    JSONObject diameterObject = asteroidObject.getJSONObject("estimated_diameter");


//                    JSONObject diameterObject = diameterArray.getJSONObject(i);

//                    JSONArray diameterMetersArray = diameterArray.getJSONArray("");

                    JSONObject diameterKilometersObject = diameterObject.getJSONObject("kilometers");


                    double asteroidDiameterMin = diameterKilometersObject.getDouble("estimated_diameter_min");

                    double asteroidDiameterMax = diameterKilometersObject.getDouble("estimated_diameter_max");

//                    String asteroidDiameterMinString = String.valueOf(asteroidDiameterMin);
//
//                    String asteroidDiameterMaxString = String.valueOf(asteroidDiameterMax);

//                    String asteroidDiameter = asteroidDiameterMinString + " - " + asteroidDiameterMaxString + " m";

//                    JSONObject approachDateObject = asteroidObject.getJSONObject("close_approach_data");

                    JSONArray approachDateArray = asteroidObject.getJSONArray("close_approach_data");

                    String asteroidApproachDate = null;

                    String asteroidVelocity = null;

                    for (int j = 0; j < approachDateArray.length(); j++) {

                        JSONObject approachDataObject = approachDateArray.getJSONObject(j);

                        asteroidApproachDate = approachDataObject.getString("close_approach_date_full");

                        JSONObject velocityObject = approachDataObject.getJSONObject("relative_velocity");

                        asteroidVelocity = velocityObject.getString("kilometers_per_second");


//                        JSONArray velocityArray = approachDataObject.getJSONArray("relative_velocity");

//                        for (int k = 0; k < velocityArray.length(); k++) {
//                            JSONObject velocityObject = velocityArray.getJSONObject(i);

////                            asteroidVelocityString = String.valueOf(asteroidVelocity);
//                        }
                    }

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
            if (asteroids.size() == 0) {

            } else {
                populateAsteroids(asteroids);
            }
        }
    }

    private void populateAsteroids(List<Asteroid> asteroids) {

//        asteroidAdapter.setAsteroidList(asteroids);
    }

}
