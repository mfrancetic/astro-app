package com.udacity.astroapp.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.udacity.astroapp.R;
import com.udacity.astroapp.adapters.ObservatoryAdapter;
import com.udacity.astroapp.models.Observatory;
import com.udacity.astroapp.utils.Secret;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.security.Permission;
import java.security.Permissions;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class ObservatoryListFragment extends Fragment implements LocationListener {

    private RecyclerView observatoryRecyclerView;

    private ObservatoryAdapter observatoryAdapter;

    private List<Observatory> observatories;

    private String google_api_key = Secret.google_play_services_api_key;


    private ArrayList permissionsToRequest;
    private ArrayList permissionsRejected = new ArrayList();
    private ArrayList permissions = new ArrayList();

    private final static int ALL_PERMISSIONS_RESULT = 101;

    GoogleApiClient googleApiClient;

    private Context context;

    private boolean locationPermissionGranted;

    private LocationManager locationManager;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;

    private Location location;

    private double currentLongitude;

    private double currentLatitude;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() != null) {
            getActivity().setTitle(R.string.menu_observatories);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_observatory_list, container, false);

        observatories = new ArrayList<>();

        observatoryRecyclerView = rootView.findViewById(R.id.observatory_list_recycler_view);
        observatoryAdapter = new ObservatoryAdapter(observatories);
        observatoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        observatoryRecyclerView.setAdapter(observatoryAdapter);

        context = observatoryRecyclerView.getContext();


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


        return rootView;
    }

    public void getUsersLocation() {

//        googleApiClient = new GoogleApiClient.Builder(context)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .addApi(LocationServices.API).build();
//
//

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
        currentLongitude = location.getLongitude();
        currentLatitude = location.getLatitude();
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

}
