package com.udacity.astroapp.fragments;

import android.Manifest;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.udacity.astroapp.R;
import com.udacity.astroapp.adapters.ObservatoryAdapter;
import com.udacity.astroapp.data.AppDatabase;
import com.udacity.astroapp.data.AppExecutors;
import com.udacity.astroapp.data.ObservatoryViewModel;
import com.udacity.astroapp.data.ObservatoryViewModelFactory;
import com.udacity.astroapp.models.Observatory;
import com.udacity.astroapp.utils.QueryUtils;
import com.udacity.astroapp.utils.Secret;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;


public class ObservatoryListFragment extends Fragment implements LocationListener {

    private RecyclerView observatoryRecyclerView;

    private ObservatoryAdapter observatoryAdapter;

    public List<Observatory> observatoryList;

    private String google_api_key = Secret.google_play_services_api_key;


    private ArrayList permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();

    private final static int ALL_PERMISSIONS_RESULT = 101;

    GoogleApiClient googleApiClient;

    private Context context;

    public Observatory observatory;

    private boolean locationPermissionGranted;

    private LocationManager locationManager;

    private String defaultLocationBerlin = "52.520008, 13.404954";

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;

    private static final String LOG_TAG = ObservatoryListFragment.class.getSimpleName();

    private Location location;

    private double currentLongitude;

    private double currentLatitude;

    private String currentLatitudeString;

    private String currentLongitudeString;

    public static OnObservatoryClickListener onObservatoryClickListener;

    private String currentLocation;

    private TextView observatoryListEmptyTextView;

    private ProgressBar observatoryListLoadingIndicator;

    private AppDatabase appDatabase;

    private ObservatoryViewModelFactory observatoryViewModelFactory;

    private ObservatoryViewModel observatoryViewModel;


    public interface OnObservatoryClickListener {
        void onObservatorySelected(int position);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null) {
            getActivity().setTitle(R.string.menu_observatories);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_observatory_list, container, false);

//        observatories = new ArrayList<>();

        observatoryListEmptyTextView = rootView.findViewById(R.id.observatory_list_empty_text_view);
        observatoryListLoadingIndicator = rootView.findViewById(R.id.observatory_list_loading_indicator);

        observatoryListEmptyTextView.setVisibility(View.GONE);

        context = observatoryListLoadingIndicator.getContext();

        appDatabase = AppDatabase.getInstance(context);

        observatoryViewModelFactory = new ObservatoryViewModelFactory(appDatabase);
        observatoryViewModel = ViewModelProviders.of(ObservatoryListFragment.this, observatoryViewModelFactory)
                .get(ObservatoryViewModel.class);

        observatoryViewModel.getObservatories().observe(ObservatoryListFragment.this, new Observer<List<Observatory>>() {
            @Override
            public void onChanged(@Nullable final List<Observatory> observatories) {
                observatoryViewModel.getObservatories().removeObserver(this);
                if (observatories != null) {
                    AppExecutors.getExecutors().diskIO().execute(new Runnable() {
                        @Override
                        public void run() {
                            int numberOfObservatories = appDatabase.astroDao().getObservatoryCount();
                            if (observatoryList != null) {
                                appDatabase.astroDao().deleteAllObservatories();

                                numberOfObservatories = appDatabase.astroDao().getObservatoryCount();

                                appDatabase.astroDao().addAllObservatories(observatoryList);
                            }
                        }
                    });
                }
            }
        });


        observatoryRecyclerView = rootView.findViewById(R.id.observatory_list_recycler_view);
        observatoryAdapter = new ObservatoryAdapter(observatoryList, onObservatoryClickListener);

        final FragmentActivity fragmentActivity = getActivity();

        observatoryRecyclerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onObservatoryClickListener.onObservatorySelected(observatoryRecyclerView.getId());
            }
        });

        observatoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        observatoryRecyclerView.setAdapter(observatoryAdapter);

        observatoryRecyclerView.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            locationPermissionGranted = true;
            location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
            onLocationChanged(location);
        }
        new ObservatoryAsyncTask().execute();
        return rootView;
    }


    private class ObservatoryAsyncTask extends AsyncTask<String, Void, List<Observatory>> {

        @Override
        protected List<Observatory> doInBackground(String... strings) {
            try {

                if (currentLocation == null) {
                     currentLocation = defaultLocationBerlin;
                }

                URL url = QueryUtils.createObservatoryURL(currentLocation);

                String observatoryJson = QueryUtils.makeHttpRequest(url);

                JSONObject baseObservatoryResponse = new JSONObject(observatoryJson);

                JSONArray observatoryArray = baseObservatoryResponse.getJSONArray("results");

                for (int i = 0; i < observatoryArray.length(); i++) {
                    JSONObject observatoryObject = observatoryArray.getJSONObject(i);

                    String observatoryId = observatoryObject.getString("place_id");
                    String observatoryName = observatoryObject.getString("name");
                    String observatoryAddress = observatoryObject.getString("formatted_address");

                    boolean observatoryOpeningHours = false;
                    if (observatoryObject.has("opening_hours")) {
                        JSONObject openingHoursObject = observatoryObject.getJSONObject("opening_hours");
                        if (openingHoursObject.has("open_now")) {
                            observatoryOpeningHours = openingHoursObject.getBoolean("open_now");
                        }
                    }

                JSONObject geometryObject = observatoryObject.getJSONObject("geometry");

                JSONObject locationObject = geometryObject.getJSONObject("location");

                double observatoryLatitude = locationObject.getDouble("lat");
                double observatoryLongitude = locationObject.getDouble("lng");

                String observatoryUrl = "";

                observatory = new Observatory(observatoryId, observatoryName, observatoryAddress, null,
                        observatoryOpeningHours,
                        null,
                        observatoryLatitude, observatoryLongitude,
                        observatoryUrl);

                observatory.setObservatoryId(observatoryId);
                observatory.setObservatoryName(observatoryName);
                observatory.setObservatoryAddress(observatoryAddress);
                observatory.setObservatoryOpenNow(observatoryOpeningHours);
                observatory.setObservatoryLatitude(observatoryLatitude);
                observatory.setObservatoryLongitude(observatoryLongitude);
                observatory.setObservatoryUrl(observatoryUrl);

                if (observatoryList == null) {
                    observatoryList = new ArrayList<>();
                }
                observatoryList.add(i, observatory);
            }
        } catch(
        IOException e)

        {
            Log.e(LOG_TAG, "Problem retrieving the observatory JSON results");
        } catch(
        JSONException e)

        {
            Log.e(LOG_TAG, "Problem parsing the observatory JSON response");
        }
            return observatoryList;
    }

    @Override
    protected void onPostExecute(List<Observatory> observatories) {
        if (observatories != null) {
            populateObservatories(observatories);
        } else if (observatoryViewModel.getObservatories() != null && observatoryViewModel.getObservatories()
                .getValue().size() != 0) {
            LiveData<List<Observatory>> observatoryDatabaseList = observatoryViewModel.getObservatories();
            observatories = observatoryDatabaseList.getValue();
            populateObservatories(observatories);
        } else {
            observatoryRecyclerView.setVisibility(View.GONE);
            observatoryListLoadingIndicator.setVisibility(View.GONE);
            observatoryListEmptyTextView.setVisibility(View.VISIBLE);
        }
    }

}

    private void populateObservatories(List<Observatory> observatories) {
        observatoryListLoadingIndicator.setVisibility(View.GONE);
        observatoryListEmptyTextView.setVisibility(View.GONE);
        observatoryAdapter.setObservatories(observatories);
        observatoryRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    onLocationChanged(location);
                    new ObservatoryAsyncTask().execute();
                } else {
                    String toastText = "Location access not granted. Please allow location access";
                    Toast.makeText(getActivity(), toastText, Toast.LENGTH_LONG).show();
                }
//                return;
            }
        }
//        updateLocationUI();
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            currentLongitude = location.getLongitude();
            currentLatitude = location.getLatitude();
            currentLongitudeString = String.valueOf(currentLongitude);
            currentLatitudeString = String.valueOf(currentLatitude);
            currentLocation = currentLatitudeString + ", " + currentLongitudeString;
        }

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        /* Check if the onRecipeStepClickListener exists; if not, throw a RuntimeException */
        try {
            onObservatoryClickListener = (OnObservatoryClickListener) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.toString() + "must implement OnDetailRecipeStepClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onObservatoryClickListener = null;
    }

}
