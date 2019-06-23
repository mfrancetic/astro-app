package com.udacity.astroapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
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

    private NavigationView navigationView;

    private ObservatoryListFragment observatoryListFragment;

    private DrawerLayout drawer;

    private ActionBarDrawerToggle toggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);

        tabletSize = getResources().getBoolean(R.bool.isTablet);

        setSupportActionBar(toolbar);
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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


//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

         navigationView = findViewById(R.id.nav_view);
        navigationView.requestFocus();
        navigationView.setFocusable(true);
        drawer = findViewById(R.id.drawer_layout);

        if (!tabletSize) {
            toggle = new ActionBarDrawerToggle(
                    this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
            drawer.addDrawerListener(toggle);
            toggle.syncState();
            drawer.closeDrawer(GravityCompat.START);
        }

//        if (findViewById(R.id.drawer_layout) != null) {

//        }
//
//        if (tabletSize) {
//            drawer.getDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
//            drawer.setScrimColor(getResources().getColor(R.color.colorDrawerNoShadow));
//            isDrawerLocked = true;
//        }
//        drawer.openDrawer(GravityCompat.START);

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
//        navigationView.getOnFocusChangeListener().onFocusChange();

        if (savedInstanceState == null) {
            navigationView.setCheckedItem(R.id.nav_photo);
            navigationView.requestFocus();
            Fragment fragment = new PhotoFragment();
            displayFragment(fragment);
        }
    }

    @Override
    public void onBackPressed() {
        if (tabletSize) {
//            DrawerLayout drawer = findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        navigationView.requestFocus();
        int id = item.getItemId();
        Fragment fragment = null;
        if (id == R.id.nav_photo) {
            fragment = new PhotoFragment();
            displayFragment(fragment);
        } else if (id == R.id.nav_asteroids) {
            fragment = new AsteroidFragment();
            displayFragment(fragment);
        } else if (id == R.id.nav_observatories) {
            fragment = new ObservatoryListFragment();
            displayFragment(fragment);
        }

//        DrawerLayout drawer = findViewById(R.id.drawer_layout);

        if (!tabletSize) {
            drawer.closeDrawer(GravityCompat.START);
            navigationView.clearFocus();
        }

        return true;
    }

    private void displayFragment(Fragment fragment) {

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (!tabletSize) {
            fragmentTransaction.setCustomAnimations(R.animator.enter_from_right, R.animator.exit_to_left,
                    R.animator.enter_from_left, R.animator.exit_to_right);
        }
        fragmentTransaction.replace(R.id.fragment_container, fragment)
                .commit();
    }

    public void onObservatorySelected(int position) {
        ObservatoryFragment observatoryFragment = new ObservatoryFragment();
//        observatoryFragment.setObservatory(ObservatoryAdapter.observatory);
        observatoryFragment.setObservatory(ObservatoryAdapter.observatories.get(position));
        displayFragment(observatoryFragment);
    }


}
