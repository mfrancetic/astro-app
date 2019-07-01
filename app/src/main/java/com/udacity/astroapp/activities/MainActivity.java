package com.udacity.astroapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
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
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.udacity.astroapp.R;
import com.udacity.astroapp.adapters.ObservatoryAdapter;
import com.udacity.astroapp.fragments.AsteroidFragment;
import com.udacity.astroapp.fragments.ObservatoryFragment;
import com.udacity.astroapp.fragments.ObservatoryListFragment;
import com.udacity.astroapp.fragments.ObservatoryListFragment.OnObservatoryClickListener;
import com.udacity.astroapp.fragments.PhotoFragment;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnObservatoryClickListener {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;
    private boolean tabletSize;
    private final static String fragmentIdKey = "fragmentId";
    private int fragmentId;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private ActionBarDrawerToggle toggle;
    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /* Find toolbar and set the support action bar to the toolbar */
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);

        /* Check if the device is a phone or a tablet */
        tabletSize = getResources().getBoolean(R.bool.isTablet);

        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        /* Check if the activity has an ACCESS_FINE_LOCATION and ACCESS_COARSE_LOCATION permissions.
         * If it doesn't request it*/
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            /* In case the activity does have a permission, get the last known location and pass the location to the
           onLocationChangedMethod in the ObservatoryListFragment */
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            ObservatoryListFragment observatoryListFragment = new ObservatoryListFragment();
            observatoryListFragment.onLocationChanged(location);
        }

        /* Set NavigationView and focusable and request focus */
        navigationView = findViewById(R.id.nav_view);
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
            displayFragment(fragment);
        } else {
            currentFragment = getSupportFragmentManager().findFragmentById(fragmentId);
        }
    }

    @Override
    public void onBackPressed() {
        /* In phone mode, close the drawer */
        if (!tabletSize) {
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
        /* Finish the activity */
        finish();
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
            /* In phone mode, set an animation for entering and exiting the fragment */
            if (!tabletSize) {
                fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                        R.anim.enter_from_left, R.anim.exit_to_right);
            }
            fragmentTransaction.replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    /**
     * Creates a new ObservatoryFragment and sets the value of the selected observatory.
     * Displays fragment
     */
    public void onObservatorySelected(int position) {
        ObservatoryFragment observatoryFragment = new ObservatoryFragment();
        observatoryFragment.setObservatory(ObservatoryAdapter.observatories.get(position));
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
}