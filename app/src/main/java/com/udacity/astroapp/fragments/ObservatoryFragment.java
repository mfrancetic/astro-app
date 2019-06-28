package com.udacity.astroapp.fragments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.udacity.astroapp.R;
import com.udacity.astroapp.data.AppDatabase;
import com.udacity.astroapp.data.AppExecutors;
import com.udacity.astroapp.data.ObservatoryDetailViewModel;
import com.udacity.astroapp.data.ObservatoryDetailViewModelFactory;
import com.udacity.astroapp.models.Observatory;
import com.udacity.astroapp.utils.QueryUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;

public class ObservatoryFragment extends Fragment implements OnMapReadyCallback {

    /* Tag for log messages */
    private static final String LOG_TAG = ObservatoryFragment.class.getSimpleName();

    /* Observatory object and its values*/
    private Observatory observatory;
    private static final String observatoryKey = "observatory";
    private String observatoryId;
    private String observatoryName;
    private String observatoryAddress;
    private double observatoryLatitude;
    private double observatoryLongitude;
    private String observatoryUrl;
    private String observatoryPhoneNumber;
    private String observatoryOpeningHoursDay;
    private boolean observatoryOpenNow;

    /* Views of the PhotoFragment */
    private TextView observatoryNameTextView;
    private TextView observatoryAddressTextView;
    private TextView observatoryOpenNowTextView;
    private ScrollView observatoryScrollView;
    private Button visitObservatoryHomepageButton;
    private TextView observatoryOpeningHoursTextView;
    private TextView observatoryPhoneNumberTextView;
    private TextView observatoryEmptyTextView;
    private ImageView observatoryEmptyImageView;
    private ProgressBar observatoryLoadingIndicator;

    /* Instances of the AppDatabase and ObservatoryDetailViewModel */
    private AppDatabase appDatabase;
    private ObservatoryDetailViewModel observatoryDetailViewModel;

    /* Boolean that indicates the API call was not successful */
    private boolean jsonNotSuccessful;

    /* Scroll position X and Y keys */
    private static final String SCROLL_POSITION_X = "scrollX";
    private static final String SCROLL_POSITION_Y = "scrollY";

    /* Scroll position X and Y values */
    private int scrollX;
    private int scrollY;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        /* Inflate the fragment_observatory.xml view */
        View rootView = inflater.inflate(R.layout.fragment_observatory, container, false);

        jsonNotSuccessful = false;
        observatoryOpeningHoursDay = "";
        String mapFragmentTag = "mapFragment";

        if (getActivity() != null) {
            /* Set the title of the activity */
            getActivity().setTitle(observatoryName);
        }

        if (observatory != null) {
            /* If observatory is not null, retrieve its values */
            observatoryAddress = observatory.getObservatoryAddress();
            observatoryLatitude = observatory.getObservatoryLatitude();
            observatoryLongitude = observatory.getObservatoryLongitude();
            observatoryUrl = observatory.getObservatoryUrl();
            observatoryOpenNow = observatory.getObservatoryOpenNow();
            observatoryId = observatory.getObservatoryId();
            observatoryOpeningHoursDay = observatory.getObservatoryOpeningHours();
            observatoryPhoneNumber = observatory.getObservatoryPhoneNumber();
        }

        /* Find the views */
        observatoryScrollView = rootView.findViewById(R.id.observatory_scroll_view);
        observatoryNameTextView = rootView.findViewById(R.id.observatory_name);
        observatoryAddressTextView = rootView.findViewById(R.id.observatory_address);
        observatoryOpenNowTextView = rootView.findViewById(R.id.observatory_open_now);
        visitObservatoryHomepageButton = rootView.findViewById(R.id.observatory_visit_homepage_button);
        observatoryOpeningHoursTextView = rootView.findViewById(R.id.observatory_opening_hours);
        observatoryPhoneNumberTextView = rootView.findViewById(R.id.observatory_phone_number);
        observatoryLoadingIndicator = rootView.findViewById(R.id.observatory_loading_indicator);
        observatoryEmptyTextView = rootView.findViewById(R.id.observatory_empty_text_view);
        observatoryEmptyImageView = rootView.findViewById(R.id.observatory_empty_image_view);

        /* Get the childFragmentManager and find fragment by its tag */
        FragmentManager fragmentManager = getChildFragmentManager();
        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager
                .findFragmentByTag(mapFragmentTag);

        if (mapFragment == null) {
            /* If the mapFragment is null, create a new SupportMapFragment and execute the
            * adding of the fragment*/
            mapFragment = new SupportMapFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.map_fragment_container, mapFragment, mapFragmentTag);
            fragmentTransaction.commit();
            fragmentManager.executePendingTransactions();
        }

        mapFragment.getMapAsync(this);

        /* Hide the empty views and show the loading indicator */
        observatoryEmptyTextView.setVisibility(View.GONE);
        observatoryEmptyImageView.setVisibility(View.GONE);
        observatoryLoadingIndicator.setVisibility(View.VISIBLE);

        Context context = observatoryEmptyTextView.getContext();
        appDatabase = AppDatabase.getInstance(context);

        ObservatoryDetailViewModelFactory observatoryDetailViewModelFactory = new ObservatoryDetailViewModelFactory(appDatabase, observatoryId);
        observatoryDetailViewModel = ViewModelProviders.of(ObservatoryFragment.this, observatoryDetailViewModelFactory)
                .get(ObservatoryDetailViewModel.class);

        /* Observe the observatory in the ObservatoryFragment */
        observatoryDetailViewModel.getObservatory().observe(ObservatoryFragment.this, new Observer<Observatory>() {
            @Override
            public void onChanged(@Nullable final Observatory observatoryDatabase) {
                observatoryDetailViewModel.getObservatory().removeObserver(this);

                if (observatoryDatabase != null) {
                    AppExecutors.getExecutors().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            appDatabase.astroDao().loadObservatoryById(observatoryId);
                            if (observatory != null) {
                                /* In case the observatory is not null, delete the observatory and
                                add the new one to the database */
                                appDatabase.astroDao().deleteObservatory(observatoryId);
                                appDatabase.astroDao().addObservatory(observatory);
                            }
                        }
                    });
                }
            }
        });

        /* Check if there in a savedInstanceState */
        if (savedInstanceState == null) {
            /* In case there is no savedInstanceState, execute an ObservatoryDetailsAsyncTask */
            new ObservatoryDetailsAsyncTask().execute();
        } else {
            /* In case there is a savedInstanceState, get the scroll positions, get the saved
             * observatory and populate the view with its values */
            scrollX = savedInstanceState.getInt(SCROLL_POSITION_X);
            scrollY = savedInstanceState.getInt(SCROLL_POSITION_Y);
            observatory = savedInstanceState.getParcelable(observatoryKey);
            populateObservatory(observatory);
        }
        return rootView;
    }

    public void setObservatory(Observatory observatory) {
        this.observatory = observatory;
    }

    public Observatory getObservatory() {
        return observatory;
    }

    private void populateObservatory(Observatory observatory) {
        if (observatoryScrollView != null) {
            /* Scroll to the X and Y position of the observatoryScrollView*/
            observatoryScrollView.scrollTo(scrollX, scrollY);
        }

        /* Hide the empty views and loading indicator */
        observatoryEmptyImageView.setVisibility(View.GONE);
        observatoryEmptyTextView.setVisibility(View.VISIBLE);
        observatoryLoadingIndicator.setVisibility(View.GONE);

        if (observatory != null) {
            /* If the observatory is not null, retrieve its values */
            observatoryName = observatory.getObservatoryName();
            observatoryId = observatory.getObservatoryId();
            if (getActivity() != null) {
                /* Set the title of the activity */
                getActivity().setTitle(observatoryName);
            }
            observatoryAddress = observatory.getObservatoryAddress();
            observatoryLatitude = observatory.getObservatoryLatitude();
            observatoryLongitude = observatory.getObservatoryLongitude();
            observatoryUrl = observatory.getObservatoryUrl();
            observatoryOpenNow = observatory.getObservatoryOpenNow();
            observatoryId = observatory.getObservatoryId();
            observatoryOpeningHoursDay = observatory.getObservatoryOpeningHours();
            observatoryPhoneNumber = observatory.getObservatoryPhoneNumber();

            /* Set the observatory's values to the appropriate text views */
            observatoryNameTextView.setText(observatoryName);
            observatoryAddressTextView.setText(observatoryAddress);

            if (observatoryOpenNow) {
                observatoryOpenNowTextView.setText(R.string.observatory_open);
                observatoryOpenNowTextView.setVisibility(View.VISIBLE);
            } else {
                /* If the boolean observatoryOpenNow is false, hide the observatoryOpenNowTextView */
                observatoryOpenNowTextView.setVisibility(View.GONE);
            }

            if (observatoryPhoneNumber != null) {
                observatoryPhoneNumberTextView.setText(observatoryPhoneNumber);
                observatoryPhoneNumberTextView.setVisibility(View.VISIBLE);
            } else {
                /* If there is no phone number, hide the observatoryPhoneNumberTextView */
                observatoryPhoneNumberTextView.setVisibility(View.GONE);
            }

            if (observatoryOpeningHoursDay != null) {
                observatoryOpeningHoursTextView.setText(observatoryOpeningHoursDay);
                observatoryOpeningHoursTextView.setVisibility(View.VISIBLE);
            } else {
                /* If there are no opening hours, hide the observatoryOpeningHoursTextView */
                observatoryOpeningHoursTextView.setVisibility(View.GONE);
            }

            if (observatoryUrl == null || observatoryUrl.isEmpty()) {
                /* If there is no observatoryUrl or it is empty, hide the visitObservatoryHomepageButton */
                visitObservatoryHomepageButton.setVisibility(View.GONE);
            } else {
                /* If there is an observatoryUrl, show the visitObservatoryHomepageButton and set
                * an onClickListner to it */
                visitObservatoryHomepageButton.setVisibility(View.VISIBLE);
                visitObservatoryHomepageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /* OnClick, create and start an intent to open the homepage of the observatory */
                        Intent openObservatoryHomepageIntent = new Intent(Intent.ACTION_VIEW);
                        Uri observatoryHomepageUri = Uri.parse(observatoryUrl);
                        openObservatoryHomepageIntent.setData(observatoryHomepageUri);
                        startActivity(openObservatoryHomepageIntent);
                    }
                });
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        /* Create a new LatLng value of the observatory */
        LatLng observatoryLatLng = new LatLng(observatoryLatitude, observatoryLongitude);

        /* Zoom to the defined zoomValue */
        int zoomValue = 15;
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(zoomValue);

        /* Add a marker to the map, that indicates the name of the observatory */
        googleMap.addMarker(new MarkerOptions().position(observatoryLatLng)
                .title(getString(R.string.marker_of_the_observatory_content_description) + " " +
                        observatoryName));

        /* Move and animate the camera */
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(observatoryLatLng));
        googleMap.animateCamera(zoom);
    }

    /**
     * ObservatoryDetailsAsyncTask class that creates the URL for loading the observatory, makes the HTTP request and
     * parses the JSON String in order to create a new Observatory object.
     * Returns an observatory.
     */
    private class ObservatoryDetailsAsyncTask extends AsyncTask<String, Void, Observatory> {

        @Override
        protected Observatory doInBackground(String... strings) {

            try {
                /* Create an URL and make a HTTP request */
                URL url = QueryUtils.createObservatoryDetailsUrl(observatoryId);
                String observatoryDetailsJson = QueryUtils.makeHttpRequest(url);

                JSONObject baseObservatoryJsonResponse = new JSONObject(observatoryDetailsJson);
                JSONObject observatoryObject = baseObservatoryJsonResponse.getJSONObject("result");

                /* Extract the value for the required keys */
                if (observatoryObject.has("website")) {
                    observatoryUrl = observatoryObject.getString("website");
                    observatory.setObservatoryUrl(observatoryUrl);
                }

                if (observatoryObject.has("international_phone_number")) {
                    observatoryPhoneNumber = observatoryObject.getString("international_phone_number");
                    observatory.setObservatoryPhoneNumber(observatoryPhoneNumber);
                }
                observatoryOpeningHoursDay = "";

                if (observatoryObject.has("opening_hours")) {
                    JSONObject openingHoursJsonObject = observatoryObject.getJSONObject("opening_hours");
                    JSONArray openingHoursArray = openingHoursJsonObject.getJSONArray("weekday_text");
                    for (int i = 0; i < openingHoursArray.length(); i++) {
                        observatoryOpeningHoursDay = observatoryOpeningHoursDay + "\n" + openingHoursArray.getString(i);
                        observatory.setObservatoryOpeningHours(observatoryOpeningHoursDay);
                    }
                }
            } catch (IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the observatory details JSON results");
                jsonNotSuccessful = true;
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the observatory details JSON response");
                jsonNotSuccessful = true;
            }
            return observatory;
        }

        @Override
        protected void onPostExecute(Observatory newObservatory) {
            if (newObservatory != null) {
                /* If there is an observatory available, populate the view with its values */
                observatoryEmptyTextView.setVisibility(View.GONE);
                observatoryLoadingIndicator.setVisibility(View.GONE);
                populateObservatory(newObservatory);
                if (jsonNotSuccessful) {
                    /* Create and show a Snackbar that informs the user that there is no Internet
                     * connectivity and the data is populated from the database */
                    Snackbar snackbar = Snackbar.make(observatoryScrollView, getString(R.string.snackbar_offline_mode), Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
            } else if (observatoryDetailViewModel.getObservatory() != null && observatoryDetailViewModel.getObservatory()
                    .getValue() != null) {
                /* In case there are values stored in the ObservatoryDetailViewModel, retrieve those values */
                LiveData<Observatory> observatoryDatabase = observatoryDetailViewModel.getObservatory();
                observatory = observatoryDatabase.getValue();
                if (observatory != null) {
                    /* Create and show a Snackbar that informs the user that there is no Internet
                     * connectivity and the data is populated from the database */
                    Snackbar snackbar = Snackbar.make(observatoryScrollView, getString(R.string.snackbar_offline_mode), Snackbar.LENGTH_LONG);
                    snackbar.show();
                    populateObservatory(observatory);
                }
            } else {
                /* In case there are also no values stored in the database, hide all the
                 * views except the empty views */
                observatoryLoadingIndicator.setVisibility(View.GONE);
                observatoryEmptyTextView.setVisibility(View.VISIBLE);
                observatoryEmptyImageView.setVisibility(View.VISIBLE);
            }
            super.onPostExecute(observatory);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        /* Save the observatory and scroll positions in the savedInstanceState */
        outState.putParcelable(observatoryKey, observatory);
        if (observatoryScrollView != null) {
            scrollX = observatoryScrollView.getScrollX();
            scrollY = observatoryScrollView.getScrollY();
        }
        outState.putInt(SCROLL_POSITION_X, scrollX);
        outState.putInt(SCROLL_POSITION_Y, scrollY);
        super.onSaveInstanceState(outState);
    }
}