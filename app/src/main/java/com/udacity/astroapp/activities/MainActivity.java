package com.udacity.astroapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.transition.ChangeBounds;
import android.transition.Scene;
import android.transition.Slide;
import android.transition.TransitionManager;
import android.view.Gravity;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.animation.DecelerateInterpolator;

import com.udacity.astroapp.R;
import com.udacity.astroapp.adapters.ObservatoryAdapter;
import com.udacity.astroapp.fragments.AsteroidFragment;
import com.udacity.astroapp.fragments.ObservatoryFragment;
import com.udacity.astroapp.fragments.ObservatoryListFragment;
import com.udacity.astroapp.fragments.ObservatoryListFragment.OnObservatoryClickListener;
import com.udacity.astroapp.fragments.PhotoFragment;
import com.udacity.astroapp.adapters.ObservatoryAdapter;
import com.udacity.astroapp.fragments.ObservatoryListFragment;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnObservatoryClickListener {

    private LocationManager locationManager;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 101;

    private Location location;

    private boolean isDrawerLocked;

    private ObservatoryAdapter observatoryAdapter;

    private boolean locationPermissionGranted;

    private boolean tabletSize;

    private final static String fragmentIdKey = "fragmentId";

    private int fragmentId;

    private NavigationView navigationView;

    private ObservatoryListFragment observatoryListFragment;

    private DrawerLayout drawer;

    private ActionBarDrawerToggle toggle;

    private Bundle onSaveInstanceState;

    private Fragment currentFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);

        tabletSize = getResources().getBoolean(R.bool.isTablet);

        setSupportActionBar(toolbar);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        } else {
            locationPermissionGranted = true;
            location = locationManager.getLastKnownLocation(locationManager.NETWORK_PROVIDER);
            ObservatoryListFragment observatoryListFragment = new ObservatoryListFragment();
            observatoryListFragment.onLocationChanged(location);
        }

        navigationView = findViewById(R.id.nav_view);
        navigationView.requestFocus();
        navigationView.setFocusable(true);
        drawer = findViewById(R.id.drawer_layout);

        if (savedInstanceState != null) {
            currentFragment = getSupportFragmentManager().findFragmentById(fragmentId);
        }

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
        navigationView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    navigationView.setFocusable(true);
                    navigationView.requestFocus();
                } else {
                    navigationView.setFocusable(false);
                    navigationView.clearFocus();
                }
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
        if (!tabletSize) {
//            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
        finish();
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        navigationView.requestFocus();
        int newId = item.getItemId();
//        if (newId != fragmentId) {
            if (newId == R.id.nav_photo) {
                currentFragment = new PhotoFragment();
                displayFragment(currentFragment);
            } else if (newId == R.id.nav_asteroids) {
                currentFragment = new AsteroidFragment();
                displayFragment(currentFragment);
            } else if (newId == R.id.nav_observatories) {
                currentFragment = new ObservatoryListFragment();
                displayFragment(currentFragment);
            }
            fragmentId = newId;
//        }
        if (!tabletSize) {
            drawer.closeDrawer(GravityCompat.START);
            navigationView.clearFocus();
        }

        return true;
    }

    private void displayFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        currentFragment = fragmentManager.findFragmentById(fragmentId);

        if (currentFragment == null) {
            if (!tabletSize) {
                fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left,
                        R.animator.enter_from_left, R.animator.exit_to_right);
            }
            fragmentTransaction.replace(R.id.fragment_container, fragment)
                    .commit();
        }
    }

    public void onObservatorySelected(int position) {
        ObservatoryFragment observatoryFragment = new ObservatoryFragment();
        observatoryFragment.setObservatory(ObservatoryAdapter.observatories.get(position));
        displayFragment(observatoryFragment);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (!tabletSize) {
            toggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!tabletSize) {
//            toggle.onConfigurationChanged(newConfig);
            overridePendingTransition(0,0);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(fragmentIdKey, fragmentId);
        super.onSaveInstanceState(outState);
    }
}