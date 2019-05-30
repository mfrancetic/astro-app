package com.udacity.astroapp.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import com.udacity.astroapp.R;
import com.udacity.astroapp.fragments.AsteroidFragment;

public class AsteroidActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asteroid);

        AsteroidFragment asteroidFragment = new AsteroidFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.asteroid_fragment_container, asteroidFragment)
                .commit();
    }
}
