package com.udacity.astroapp.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.udacity.astroapp.R;
import com.udacity.astroapp.adapters.ObservatoryAdapter;
import com.udacity.astroapp.fragments.AsteroidFragment;
import com.udacity.astroapp.fragments.ObservatoryFragment;
import com.udacity.astroapp.fragments.ObservatoryListFragment;
import com.udacity.astroapp.fragments.ObservatoryListFragment.OnObservatoryClickListener;
import com.udacity.astroapp.fragments.PhotoFragment;
import com.udacity.astroapp.utils.LanguageHelper;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnObservatoryClickListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

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

    public static boolean isBeingRefreshed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        setupSharedPreferences();

        /* Find toolbar and set the support action bar to the toolbar */
        setSupportActionBar(toolbar);

        /* Check if the device is a phone or a tablet */
        tabletSize = getResources().getBoolean(R.bool.isTablet);
        checkLocationPermission();

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
        int id = item.getItemId();
        if (id == R.id.menu_refresh) {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask();
            timer.schedule(timerTask, 2000);
//            FragmentManager fragmentManager = getSupportFragmentManager();
//            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//            if (currentFragment != null) {
//                fragmentTransaction.detach(this.currentFragment).attach(this.currentFragment).commit();
//            }
            return true;
        } else if (id == R.id.menu_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getApplicationContext(), getString(R.string.location_permission_granted), Toast.LENGTH_SHORT).show();
                    if (currentFragment != null && currentFragment.toString().contains(getResources().getString(R.string.observatory_list_fragment_name))) {
                        refreshFragment();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.location_permission_declined), Toast.LENGTH_SHORT).show();
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
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ObservatoryListFragment.locationActivatedNeedsToBeRefreshed) {
            Timer timer = new Timer();
            TimerTask timerTask = new TimerTask();
            timer.schedule(timerTask, 4000);
            ObservatoryListFragment.locationActivatedNeedsToBeRefreshed = false;
        }
    }

    public void refreshFragment() {
        isBeingRefreshed = true;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (currentFragment != null) {
//            if (currentFragment.toString().contains("PhotoFragment")) {
//                PhotoFragment.setPhotoLoadingIndicator();
//            }
            fragmentTransaction.detach(this.currentFragment).attach(this.currentFragment).commit();
        }
        isBeingRefreshed = false;
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.settings_language_key))) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String language = sharedPreferences.getString(
                    getString(R.string.settings_language_key),
                    getString(R.string.settings_language_default));
//            String language = PreferenceManager.getDefaultSharedPreferencesName(getBaseContext());
            LanguageHelper.changeLocale(this.getResources(), language);
//            refreshFragment();
            Intent intent = new Intent(this, MainActivity.class);
            finish();
            startActivity(intent);
//            finish();
        }
    }

    private class TimerTask extends java.util.TimerTask {
        @Override
        public void run() {
            refreshFragment();
        }
    }

    private void setupSharedPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        android.support.v7.preference.PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }
}