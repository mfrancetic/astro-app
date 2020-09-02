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
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.navigation.NavigationView;
import com.udacity.astroapp.R;
import com.udacity.astroapp.adapters.ObservatoryAdapter;
import com.udacity.astroapp.fragments.AsteroidFragment;
import com.udacity.astroapp.fragments.EarthPhotoFragment;
import com.udacity.astroapp.fragments.MarsPhotoFragment;
import com.udacity.astroapp.fragments.ObservatoryFragment;
import com.udacity.astroapp.fragments.ObservatoryListFragment.OnObservatoryClickListener;
import com.udacity.astroapp.fragments.PhotoFragment;

import java.util.Objects;
import java.util.Timer;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnObservatoryClickListener {

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

    private int themeId;

    private int androidVersion;

    private static final String PREFS_NAME = "prefs";

    private static final String PREF_THEME = "theme";

    private static final String PREF_CHECKED_THEME = "checkedTheme";

    private SharedPreferences sharedPreferences;

    private int checkedTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            themeId = sharedPreferences.getInt(PREF_THEME, 0);
            checkedTheme = sharedPreferences.getInt(PREF_CHECKED_THEME, 0);
        } else {
            checkedTheme = savedInstanceState.getInt(PREF_CHECKED_THEME);
            themeId = savedInstanceState.getInt(PREF_THEME);
        }
        updateTheme(themeId, checkedTheme);

        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);
        /* Find toolbar and set the support action bar to the toolbar */
        setSupportActionBar(toolbar);

        androidVersion = Build.VERSION.SDK_INT;

        /* Check if the device is a phone or a tablet */
        tabletSize = getResources().getBoolean(R.bool.isTablet);
//        checkLocationPermission();

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
//        } else if (id == R.id.nav_observatories) {
//            currentFragment = new ObservatoryListFragment();
//            displayFragment(currentFragment);
        } else if(id == R.id.nav_earth_photo) {
            currentFragment = new EarthPhotoFragment();
            displayFragment(currentFragment);
        } else if (id == R.id.nav_mars_photo) {
            currentFragment = new MarsPhotoFragment();
            displayFragment(currentFragment);
        } else if (id == R.id.nav_theme) {
            changeThemeDialog();
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
        }
        /* In phone mode, set an animation for entering and exiting the fragment */
        if (!tabletSize) {
            fragmentTransaction.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left,
                    R.anim.enter_from_left, R.anim.exit_to_right);
        }
        fragmentTransaction
                .replace(R.id.fragment_container, fragment)
                .addToBackStack("fragment")
                .commit();

        currentFragment = fragment;
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
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
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
        outState.putInt(PREF_THEME, themeId);
        outState.putInt(PREF_CHECKED_THEME, checkedTheme);
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
//        if (getSupportFragmentManager().getBackStackEntryCount() > 1 && currentFragment != null &&
//                currentFragment.toString().contains(getResources().getString(R.string.observatory_fragment_name))) {
//            getSupportFragmentManager().popBackStack();
//            currentFragment = getSupportFragmentManager().findFragmentById(fragmentId);
//        } else {
            currentFragment = getSupportFragmentManager().findFragmentById(fragmentId);
            finish();
//        }
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
            timer.schedule(timerTask, 1800);
            return true;
        } else if (id == R.id.menu_calendar) {
            return false;
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
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

    public void refreshFragment() {
        isBeingRefreshed = true;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (currentFragment != null) {
            fragmentTransaction.detach(this.currentFragment).attach(this.currentFragment).commit();
        }
        isBeingRefreshed = false;
    }


    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = Objects.requireNonNull(connectivityManager).getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private class TimerTask extends java.util.TimerTask {
        @Override
        public void run() {
            refreshFragment();
        }
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        /* Fix bug with unmarshalling unknown type code exception while resuming */
        try {
            super.onRestoreInstanceState(savedInstanceState);
        } catch (Exception e) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
    }

    private void changeThemeDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        String[] themes;
        if (androidVersion >= Build.VERSION_CODES.Q) {
            themes = getResources().getStringArray(R.array.themes_array_v29);
        } else {
            themes = getResources().getStringArray(R.array.themes_array_default);
        }
        builder
                .setTitle(R.string.menu_theme)
                .setSingleChoiceItems(themes, checkedTheme, (dialog, which) -> {
                    if (which == 0) {
                        checkedTheme = which;
                        themeId = AppCompatDelegate.MODE_NIGHT_NO;
                    } else if (which == 1) {
                        checkedTheme = which;
                        themeId = AppCompatDelegate.MODE_NIGHT_YES;
                    } else {
                        checkedTheme = which;
                        if (androidVersion >= Build.VERSION_CODES.Q) {
                            themeId = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                        } else {
                            themeId = AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
                        }
                    }
                });
        builder.setPositiveButton(getString(R.string.ok), (dialogInterface, i) -> updateTheme(themeId, checkedTheme));
        builder.setNegativeButton(getString(R.string.cancel), null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void updateTheme(int themeId, int checkedTheme) {
        SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, MODE_PRIVATE).edit();
        editor.putInt(PREF_THEME, themeId);
        editor.putInt(PREF_CHECKED_THEME, checkedTheme);
        editor.apply();
        AppCompatDelegate.setDefaultNightMode(themeId);
    }
}