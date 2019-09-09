package com.udacity.astroapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.udacity.astroapp.R;
import com.udacity.astroapp.adapters.ObservatoryAdapter;
import com.udacity.astroapp.fragments.AsteroidFragment;
import com.udacity.astroapp.fragments.ObservatoryFragment;
import com.udacity.astroapp.fragments.ObservatoryListFragment;
import com.udacity.astroapp.fragments.ObservatoryListFragment.OnObservatoryClickListener;
import com.udacity.astroapp.fragments.PhotoFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnObservatoryClickListener {


    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    private boolean tabletSize;
    private final static String fragmentIdKey = "fragmentId";
    private int fragmentId;
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private ActionBarDrawerToggle toggle;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        /* Find toolbar and set the support action bar to the toolbar */
        setSupportActionBar(toolbar);

        /* Check if the device is a phone or a tablet */
        tabletSize = getResources().getBoolean(R.bool.isTablet);
        checkLocationPermission();


        /* Check if the activity has an ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions.
         * If it doesn't request it*/
//        checkLocationPermission();
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
//                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
//                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
//        } else {
//            /* In case the activity does have a permission, get the last known location and pass the location to the
//           onLocationChangedMethod in the ObservatoryListFragment */
//            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//            ObservatoryListFragment observatoryListFragment = new ObservatoryListFragment();
//            observatoryListFragment.onLocationChanged(location);
//        }

        /* Set NavigationView and focusable and request focus */
        navigationView.requestFocus();
        navigationView.setFocusable(true);

        if (savedInstanceState != null) {
            currentFragment = getSupportFragmentManager().findFragmentById(fragmentId);
        }

        /* */
        if (!tabletSize) {
            if (toggle == null) {
                toggle = new ActionBarDrawerToggle(
                        this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
                drawer.addDrawerListener(toggle);
            }
            toggle.syncState();
            drawer.closeDrawer(GravityCompat.START);
        }

        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                navigationView.setFocusable(true);
                navigationView.requestFocus();
            } else {
                navigationView.setFocusable(false);
                navigationView.clearFocus();
            }
        });

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_photo);
            navigationView.requestFocus();
            Fragment fragment = new PhotoFragment();
            currentFragment = fragment;
            displayFragment(fragment);
        } else {
            currentFragment = getSupportFragmentManager().findFragmentById(fragmentId);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        navigationView.requestFocus();
        /* Get the MenuItem id and create and display the appropriate fragment */
        int id = item.getItemId();
        if (id == R.id.nav_photo) {
            currentFragment = new PhotoFragment();
            displayFragment(currentFragment);
        } else if (id == R.id.nav_asteroids) {
            currentFragment = new AsteroidFragment();
            displayFragment(currentFragment);
        } else if (id == R.id.nav_observatories) {
            currentFragment = new ObservatoryListFragment();
            displayFragment(currentFragment);
        }
        fragmentId = id;
        /* In phone mode, close the drawer and clear focus */
        if (!tabletSize) {
            drawer.closeDrawer(GravityCompat.START);
            navigationView.clearFocus();
        }
        return true;
    }

    /**
     * Displays a fragment using the SupportFragmentManager and adds an animation to it
     */
    private void displayFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        currentFragment = fragmentManager.findFragmentById(fragmentId);

        /* In case there is no current fragment, replace it */
        if (currentFragment == null) {
            currentFragment = new PhotoFragment();
            /* In phone mode, set an animation for entering and exiting the fragment */
            if (!tabletSize) {
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                        R.anim.enter_from_left, R.anim.exit_to_right);
            }
            fragmentTransaction.replace(R.id.fragment_container, fragment)
                    .addToBackStack("fragment")
                    .commit();

            currentFragment = fragment;
        }
    }

    /**
     * Creates a new ObservatoryFragment and sets the value of the selected observatory.
     * Displays fragment
     */
    public void onObservatorySelected(int position) {
        ObservatoryFragment observatoryFragment = new ObservatoryFragment();
        observatoryFragment.setObservatory(ObservatoryAdapter.observatories.get(position));
        fragmentId = observatoryFragment.getId();
        displayFragment(observatoryFragment);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        /* In phone mode, sync the state of the toggle */
        if (!tabletSize) {
            toggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        /* In phone mode, override the pending transition */
        if (!tabletSize) {
            overridePendingTransition(0, 0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        /* Add the fragment id to the savedInstanceState */
        outState.putInt(fragmentIdKey, fragmentId);
        super.onSaveInstanceState(outState);
    }


    @Override
    public void onBackPressed() {
        /* In phone mode, close the drawer */
        if (!tabletSize) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
        }

        /* If heading back from the ObservatoryFragment, go back to the ObservatoryListFragment.
         * If not, close the application. */
        if (getSupportFragmentManager().getBackStackEntryCount() > 1 && currentFragment != null &&
                currentFragment.toString().contains(getResources().getString(R.string.observatory_fragment_name))) {
            getSupportFragmentManager().popBackStack();
            currentFragment = getSupportFragmentManager().findFragmentById(fragmentId);
        } else {
            currentFragment = getSupportFragmentManager().findFragmentById(fragmentId);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_refresh) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            if (currentFragment != null) {
                fragmentTransaction.detach(this.currentFragment).attach(this.currentFragment).commit();
            }
        }
        return true;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                    Toast.makeText(getApplicationContext(), getString(R.string.location_permission_granted), Toast.LENGTH_SHORT).show();
                    if (currentFragment != null && currentFragment.toString().contains(getResources().getString(R.string.observatory_list_fragment_name))) {
                        refreshFragment();
//                        FragmentManager fragmentManager = getSupportFragmentManager();
//                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                        if (currentFragment != null) {
//                            fragmentTransaction.detach(this.currentFragment).attach(this.currentFragment).commit();
//                        }
                    }
//                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    // }
                    else {
                        // permission denied, boo! Disable the
                        // functionality that depends on this permission.
                        Toast.makeText(getApplicationContext(), getString(R.string.location_permission_declined), Toast.LENGTH_SHORT).show();
                    }
//                    return;
                }

            }
        }
    }

    public void checkLocationPermission() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {


//            if (getAct           PERMISSIONS_REQUEST_A; CCESS_FINE_LOCATION);ivity() != null) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    //
//    @Override
//    protected void onPostResume() {
//        super.onPostResume();
//    }
//
    @Override
    protected void onResume() {
        super.onResume();
        // if needsToBeRefreshed --> refresh the ObservatoryListFragment
        if (ObservatoryListFragment.locationActivatedNeedsToBeRefreshed) {
            refreshFragment();
            ObservatoryListFragment.locationActivatedNeedsToBeRefreshed = false;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    public void refreshFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (currentFragment != null) {
//        if (currentFragment != null && currentFragment.toString().contains(getResources().getString(R.string.observatory_fragment_name))) {
            fragmentTransaction.detach(this.currentFragment).attach(this.currentFragment).commit();
        }
    }
}