package com.udacity.astroapp.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.udacity.astroapp.R;
import com.udacity.astroapp.activities.MainActivity;
import com.udacity.astroapp.adapters.ObservatoryAdapter;
import com.udacity.astroapp.data.AppDatabase;
import com.udacity.astroapp.data.AppExecutors;
import com.udacity.astroapp.data.ObservatoryViewModel;
import com.udacity.astroapp.data.ObservatoryViewModelFactory;
import com.udacity.astroapp.models.Observatory;
import com.udacity.astroapp.utils.QueryUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.support.v4.content.ContextCompat.checkSelfPermission;

public class ObservatoryListFragment extends Fragment implements LocationListener {

    /* Tag for log messages */
    private static final String LOG_TAG = ObservatoryListFragment.class.getSimpleName();

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;


    /* Views of the PhotoFragment */
    @BindView(R.id.observatory_list_recycler_view)
    RecyclerView observatoryRecyclerView;

    @BindView(R.id.observatory_list_scroll_view)
    NestedScrollView scrollView;

    @BindView(R.id.observatory_list_empty_text_view)
    TextView observatoryListEmptyTextView;

    @BindView(R.id.observatory_list_loading_indicator)
    ProgressBar observatoryListLoadingIndicator;

    @BindView(R.id.observatory_list_empty_image_view)
    ImageView observatoryListEmptyImageView;

    @BindView(R.id.activate_location_button)
    Button activateLocationButton;

    @BindView(R.id.grant_location_permission_button)
    Button grantLocationPermissionButton;

    private ObservatoryAdapter observatoryAdapter;
    private static final String observatoryListKey = "observatoryList";
    private List<Observatory> observatoryList;

    /* Scroll position X and Y keys */
    private static final String SCROLL_POSITION_X = "scrollPositionX";
    private static final String SCROLL_POSITION_Y = "scrollPositionY";

    /* Scroll positions X and Y values */
    private int scrollX;
    private int scrollY;

    private Context context;

    /* Integer for device orientation */
    private int orientation;

    /* Boolean that indicates if a device is a phone or tablet */
    private boolean isTablet;

    //    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    private Location location;
    private String currentLocation;

    public static OnObservatoryClickListener onObservatoryClickListener;

    /* Instances of the AppDatabase and ViewModel */
    private AppDatabase appDatabase;
    private ObservatoryViewModel observatoryViewModel;

    public static boolean locationActivatedNeedsToBeRefreshed;

    public boolean locationPermissionGranted;

    public interface OnObservatoryClickListener {
        void onObservatorySelected(int position);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        if (getActivity() != null) {
            /* Set the title of the activity */
            getActivity().setTitle(R.string.menu_observatories);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (getActivity() != null) {
            /* Set the title of the activity */
            getActivity().setTitle(R.string.menu_observatories);
        }
        View rootView = inflater.inflate(R.layout.fragment_observatory_list, container, false);
        ButterKnife.bind(this, rootView);

        /* Get the boolean that indicates if a device is a tablet */
        isTablet = getResources().getBoolean(R.bool.isTablet);

        /* Get the orientation of the device */
        orientation = getResources().getConfiguration().orientation;

//        locationActivatedNeedsToBeRefreshed = false;

        /* Hide the empty views and show the loadingIndicator */
        observatoryListEmptyTextView.setVisibility(View.GONE);
        observatoryListEmptyImageView.setVisibility(View.GONE);
        activateLocationButton.setVisibility(View.GONE);
        grantLocationPermissionButton.setVisibility(View.GONE);
        observatoryListLoadingIndicator.setVisibility(View.VISIBLE);

        context = observatoryListLoadingIndicator.getContext();
        appDatabase = AppDatabase.getInstance(context);

        ObservatoryViewModelFactory observatoryViewModelFactory = new ObservatoryViewModelFactory(appDatabase);
        observatoryViewModel = ViewModelProviders.of(ObservatoryListFragment.this, observatoryViewModelFactory)
                .get(ObservatoryViewModel.class);

        /* Observe the observatories in the ObservatoryListFragment */
        observatoryViewModel.getObservatories().observe(ObservatoryListFragment.this, new Observer<List<Observatory>>() {
            @Override
            public void onChanged(@Nullable final List<Observatory> observatories) {
                observatoryViewModel.getObservatories().removeObserver(this);
                if (observatories != null) {
                    AppExecutors.getExecutors().diskIO().execute(() -> {
                        /* In case the observatoryList is not null and it is not empty,
                         * delete all observatories and add the observatoryList to the database */
                        if (observatoryList != null && !observatoryList.isEmpty()) {
                            appDatabase.astroDao().deleteAllObservatories();
                            appDatabase.astroDao().addAllObservatories(observatoryList);
                        }
                    });
                }
            }
        });

        /* Create a new ObservatoryAdapter */
        observatoryAdapter = new ObservatoryAdapter(observatoryList, onObservatoryClickListener);

        /* Set an OnClickListener to the observatoryRecyclerView */
        observatoryRecyclerView.setOnClickListener(v -> onObservatoryClickListener.onObservatorySelected(observatoryRecyclerView.getId()));

        /* Set the dividers and the adapter */
        setDividers(orientation);
        observatoryRecyclerView.setAdapter(observatoryAdapter);

        checkLocationPermission();

        /* Check if there in a savedInstanceState */
        if (savedInstanceState == null) {
            /* In case there is no savedInstanceState, execute an ObservatoryAsycTask */
            new ObservatoryAsyncTask().execute();
        } else {
            /* In case there is a savedInstanceState, get the scroll positions, get the saved
             * observatoryList and populate the view with its values */
            observatoryList = savedInstanceState.getParcelableArrayList(observatoryListKey);
            scrollX = savedInstanceState.getInt(SCROLL_POSITION_X);
            scrollY = savedInstanceState.getInt(SCROLL_POSITION_Y);
            populateObservatories(observatoryList);
        }
        return rootView;
    }

    /**
     * ObservatoryAsyncTask class that creates the URL for loading the observatoryList, makes the HTTP request and
     * parses the JSON String in order to create a new List<Observatory> object.
     * Returns a list of observatories.
     */
    @SuppressLint("StaticFieldLeak")
    private class ObservatoryAsyncTask extends AsyncTask<String, Void, List<Observatory>> {

        @Override
        protected List<Observatory> doInBackground(String... strings) {
            try {
                /* Create an URL and make a HTTP request */
                URL url = QueryUtils.createObservatoryURL(currentLocation);
                String observatoryJson = QueryUtils.makeHttpRequest(url);

                JSONObject baseObservatoryResponse = new JSONObject(observatoryJson);
                JSONArray observatoryArray = baseObservatoryResponse.getJSONArray("results");

                /* For each observatory in the observatoryArray, create an Observatory object */
                for (int i = 0; i < observatoryArray.length(); i++) {
                    JSONObject observatoryObject = observatoryArray.getJSONObject(i);

                    /* Extract the value for the required keys */
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

                    /* Create a new Observatory object and set the values to it */
                    /* Instances of the Observatory, ObservatoryAdapter and List<Observatory> and its key */
                    Observatory observatory = new Observatory(observatoryId, observatoryName, observatoryAddress,
                            null, observatoryOpeningHours, null,
                            observatoryLatitude, observatoryLongitude, observatoryUrl);

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
            } catch (
                    IOException e) {
                Log.e(LOG_TAG, "Problem retrieving the observatory JSON results");
            } catch (
                    JSONException e) {
                Log.e(LOG_TAG, "Problem parsing the observatory JSON response");
            }
            return observatoryList;
        }

        @Override
        protected void onPostExecute(List<Observatory> newObservatories) {
            if (newObservatories != null) {
                /* If there is a list of observatories available, populate the view with its values */
                populateObservatories(newObservatories);
            } else if (observatoryViewModel.getObservatories().getValue() != null && observatoryViewModel.getObservatories()
                    .getValue().size() != 0) {
                /* In case there are values stored in the ObservatoryViewModel, retrieve those values */
                LiveData<List<Observatory>> observatoryDatabaseList = observatoryViewModel.getObservatories();
                observatoryList = observatoryDatabaseList.getValue();
                populateObservatories(observatoryList);

                /* Create and show a Snackbar that informs the user that there is no Internet
                 * connectivity and the data is populated from the database */
                Snackbar snackbar = Snackbar.make(observatoryRecyclerView, getString(R.string.snackbar_offline_mode), Snackbar.LENGTH_LONG);
                snackbar.show();
            } else {
                /* In case there are also no values stored in the database, hide all the
                 * views except the empty views */
                if (!locationPermissionGranted) {
                    observatoryListEmptyTextView.setText(R.string.location_permission_declined);
                    grantLocationPermissionButton.setVisibility(View.VISIBLE);
                    grantLocationPermissionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                            locationActivatedNeedsToBeRefreshed = true;
                        }
                    });
                }

                if (locationPermissionGranted && !isLocationEnabled(context)) {
                    observatoryListEmptyTextView.setText(getString(R.string.no_observatories_found_location_disabled));
                    activateLocationButton.setVisibility(View.VISIBLE);
                    activateLocationButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            activateLocation();
                            locationActivatedNeedsToBeRefreshed = true;
                        }
                    });
                }

                if (locationPermissionGranted && isLocationEnabled(context) && !MainActivity.isNetworkAvailable(context)) {
                    observatoryListEmptyTextView.setText(R.string.no_internet_connection);
                }
                observatoryRecyclerView.setVisibility(View.GONE);
                observatoryListLoadingIndicator.setVisibility(View.GONE);
                observatoryListEmptyTextView.setVisibility(View.VISIBLE);
                observatoryListEmptyImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    private void populateObservatories(List<Observatory> observatories) {
        /* Scroll to the X and Y position of the scrollView*/
        scrollView.scrollTo(scrollX, scrollY);
        /* Hide the empty views and loading indicator */
        observatoryListLoadingIndicator.setVisibility(View.GONE);
        observatoryListEmptyTextView.setVisibility(View.GONE);
        observatoryListEmptyImageView.setVisibility(View.GONE);
        /* Set the observatories to the observatoryAdapter and show the observatoryRecyclerView */
        observatoryAdapter.setObservatories(observatories);
        observatoryRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        // If request is cancelled, the result arrays are empty.
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onLocationChanged(location);
                new ObservatoryAsyncTask().execute();
            } else {
                Toast.makeText(getActivity(), getString(R.string.location_access_not_granted_toast),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            /* Get longitude and logitude and set it to the currentLocation */
            double currentLongitude = location.getLongitude();
            double currentLatitude = location.getLatitude();
            String currentLongitudeString = String.valueOf(currentLongitude);
            String currentLatitudeString = String.valueOf(currentLatitude);
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
        /* Check if the onObservatoryClickListener exists; if not, throw a RuntimeException */
        try {
            onObservatoryClickListener = (OnObservatoryClickListener) context;
        } catch (ClassCastException e) {
            throw new RuntimeException(context.toString() + "must implement OnObservatoryClickListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onObservatoryClickListener = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        orientation = getResources().getConfiguration().orientation;
        setDividers(orientation);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        /* Save the observatoryList and scroll positions in the savedInstanceState */
        outState.putParcelableArrayList(observatoryListKey, (ArrayList<? extends Parcelable>) observatoryList);
        scrollX = scrollView.getScrollX();
        scrollY = scrollView.getScrollY();
        outState.putInt(SCROLL_POSITION_X, scrollX);
        outState.putInt(SCROLL_POSITION_Y, scrollY);
        super.onSaveInstanceState(outState);
    }

    private void setDividers(int orientation) {
        if (isTablet && orientation == Configuration.ORIENTATION_LANDSCAPE) {
            /* In tablet landscape mode, add a horizontal divider and set the GridLayoutManager
             * to the observatoryRecyclerView */
            observatoryRecyclerView.addItemDecoration(new DividerItemDecoration(context,
                    DividerItemDecoration.HORIZONTAL));
            observatoryRecyclerView.setLayoutManager(new GridLayoutManager(context, 2));
        } else {
            /* In all other case, set the LinearLayoutManager to the observatoryRecyclerView */
            observatoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        /* In all cases, add a vertical item decoration to the observatoryRecyclerView */
        observatoryRecyclerView.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));
    }

    public static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    /* Check if the activity has an ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions.
     * If it doesn't request it*/
    public void checkLocationPermission() {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        if (checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(context, ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = false;
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                if (getActivity() != null) {
                    ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
                }
            } else {
                requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        } else {
            /* In case the activity does have a permission, get the last known location and pass the location to the
           onLocationChangedMethod */
            locationPermissionGranted = true;
            FusedLocationProviderClient fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                onLocationChanged(location);
                                new ObservatoryAsyncTask().execute();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(LOG_TAG, "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
//            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            onLocationChanged(location);
        }
    }

    @Override
    public boolean shouldShowRequestPermissionRationale(@NonNull String permission) {
        return super.shouldShowRequestPermissionRationale(permission);
    }

    public void activateLocation() {
        new AlertDialog.Builder(context)
                .setMessage(R.string.snackbar_location_disabled)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.open_location_settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                        locationActivatedNeedsToBeRefreshed = true;
                    }
                }).show();
    }
}