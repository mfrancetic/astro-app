package com.udacity.astroapp.fragments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.arch.persistence.room.Query;
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
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.udacity.astroapp.R;
import com.udacity.astroapp.activities.MainActivity;
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

    private AppDatabase appDatabase;

    private Context context;

    private ObservatoryDetailViewModel observatoryDetailViewModel;

    private ObservatoryDetailViewModelFactory observatoryDetailViewModelFactory;

    private static final String LOG_TAG = ObservatoryFragment.class.getSimpleName();

    private static final String SCROLL_POSITION_X = "scrollX";

    private static final String SCROLL_POSITION_Y = "scrollY";

    private boolean jsonNotSuccessful;

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

        View rootView = inflater.inflate(R.layout.fragment_observatory, container, false);

        jsonNotSuccessful = false;

        observatoryOpeningHoursDay = "";

        if (getActivity() != null) {
            getActivity().setTitle(observatoryName);
        }

        if (observatory != null) {
            observatoryAddress = observatory.getObservatoryAddress();
            observatoryLatitude = observatory.getObservatoryLatitude();
            observatoryLongitude = observatory.getObservatoryLongitude();
            observatoryUrl = observatory.getObservatoryUrl();
            observatoryOpenNow = observatory.getObservatoryOpenNow();
            observatoryId = observatory.getObservatoryId();
            observatoryOpeningHoursDay = observatory.getObservatoryOpeningHours();
            observatoryPhoneNumber = observatory.getObservatoryPhoneNumber();
        }

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


//        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//        MapFragment mapFragment = (MapFragment) getActivity().getSupportFragmentManager().findFragmentByTag("mapFragment");
//
        FragmentManager fragmentManager = getChildFragmentManager();

        SupportMapFragment mapFragment = (SupportMapFragment) fragmentManager
                .findFragmentByTag("mapFragment");

        if (mapFragment == null) {
            mapFragment = new SupportMapFragment();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.add(R.id.map_fragment_container, mapFragment, "mapFragment");
            fragmentTransaction.commit();
            fragmentManager.executePendingTransactions();
        }

        mapFragment.getMapAsync(this);

        observatoryEmptyTextView.setVisibility(View.GONE);
        observatoryEmptyImageView.setVisibility(View.GONE);

        context = observatoryEmptyTextView.getContext();

        appDatabase = AppDatabase.getInstance(context);

        observatoryDetailViewModelFactory = new ObservatoryDetailViewModelFactory(appDatabase, observatoryId);

        observatoryDetailViewModel = ViewModelProviders.of(ObservatoryFragment.this, observatoryDetailViewModelFactory)
                .get(ObservatoryDetailViewModel.class);

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
                                appDatabase.astroDao().deleteObservatory(observatoryId);
                                appDatabase.astroDao().addObservatory(observatory);
                            }
                        }
                    });
                }
            }
        });
        if (savedInstanceState == null) {
            new ObservatoryDetailsAsyncTask().execute();
        } else {
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
            observatoryScrollView.scrollTo(scrollX, scrollY);
        }

        observatoryEmptyImageView.setVisibility(View.GONE);
        observatoryLoadingIndicator.setVisibility(View.GONE);
        if (observatory != null) {
            observatoryName = observatory.getObservatoryName();
            observatoryId = observatory.getObservatoryId();
            if (getActivity() != null) {
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

            observatoryNameTextView.setText(observatoryName);
            observatoryAddressTextView.setText(observatoryAddress);

            if (observatoryOpenNow) {
                observatoryOpenNowTextView.setText(R.string.observatory_open);
                observatoryOpenNowTextView.setVisibility(View.VISIBLE);
            } else {
                observatoryOpenNowTextView.setVisibility(View.GONE);
            }

            if (observatoryPhoneNumber != null) {
                observatoryPhoneNumberTextView.setText(observatoryPhoneNumber);
                observatoryPhoneNumberTextView.setVisibility(View.VISIBLE);
            } else {
                observatoryPhoneNumberTextView.setVisibility(View.GONE);
            }

            if (observatoryOpeningHoursDay != null) {
                observatoryOpeningHoursTextView.setText(observatoryOpeningHoursDay);
                observatoryOpeningHoursTextView.setVisibility(View.VISIBLE);
            } else {
                observatoryOpeningHoursTextView.setVisibility(View.GONE);
            }

            if (observatoryUrl == null || observatoryUrl.isEmpty()) {
                visitObservatoryHomepageButton.setVisibility(View.GONE);
            } else {
                visitObservatoryHomepageButton.setVisibility(View.VISIBLE);
                visitObservatoryHomepageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
        LatLng observatoryLatLng = new LatLng(observatoryLatitude, observatoryLongitude);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(15);

        googleMap.addMarker(new MarkerOptions().position(observatoryLatLng)
                .title(getString(R.string.marker_of_the_observatory_content_description) + " " +
                        observatoryName));
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(observatoryLatLng));
        googleMap.animateCamera(zoom);
    }

    private class ObservatoryDetailsAsyncTask extends AsyncTask<String, Void, Observatory> {

        @Override
        protected Observatory doInBackground(String... strings) {

            try {
                URL url = QueryUtils.createObservatoryDetailsUrl(observatoryId);
                String observatoryDetailsJson = QueryUtils.makeHttpRequest(url);

                JSONObject baseObservatoryJsonResponse = new JSONObject(observatoryDetailsJson);

                JSONObject observatoryObject = baseObservatoryJsonResponse.getJSONObject("result");

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
                observatoryEmptyTextView.setVisibility(View.GONE);
                observatoryLoadingIndicator.setVisibility(View.GONE);
                populateObservatory(newObservatory);
                if (jsonNotSuccessful) {
                        Snackbar snackbar = Snackbar.make(observatoryScrollView, getString(R.string.snackbar_offline_mode), Snackbar.LENGTH_LONG);
                        snackbar.show();
                }
            } else if (observatoryDetailViewModel.getObservatory() != null && observatoryDetailViewModel.getObservatory()
                    .getValue() != null) {
                LiveData<Observatory> observatoryDatabase = observatoryDetailViewModel.getObservatory();
                observatory = observatoryDatabase.getValue();
                if (observatory != null) {
//                    if (getActivity() != null) {
//                        View scrollView = getActivity().findViewById(R.id.observatory_scroll_view);
                        Snackbar snackbar = Snackbar.make(observatoryScrollView, getString(R.string.snackbar_offline_mode), Snackbar.LENGTH_LONG);
                        snackbar.show();
//                    }
                    populateObservatory(observatory);

                } else {
                    observatoryLoadingIndicator.setVisibility(View.GONE);
                    observatoryEmptyTextView.setVisibility(View.VISIBLE);
                    observatoryEmptyImageView.setVisibility(View.VISIBLE);
                }
            } else {
                observatoryLoadingIndicator.setVisibility(View.GONE);
                observatoryEmptyTextView.setVisibility(View.VISIBLE);
                observatoryEmptyImageView.setVisibility(View.VISIBLE);
            }
            super.onPostExecute(observatory);
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
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
