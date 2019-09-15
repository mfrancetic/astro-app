package com.udacity.astroapp.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceManager;

import com.udacity.astroapp.R;

public class SettingsFragment extends PreferenceFragmentCompat implements
        Preference.OnPreferenceChangeListener {

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String stringValue = newValue.toString();
        if (preference instanceof android.support.v7.preference.ListPreference) {
            android.support.v7.preference.ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                CharSequence[] labels = listPreference.getEntries();
                preference.setSummary(labels[prefIndex]);
            }
        } else {
            preference.setSummary(stringValue);
        }
        return true;
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String rootKey) {
//        addPreferencesFromResource(R.xml.settings_preferences);
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);

        android.support.v7.preference.Preference languagePreference = findPreference(getString(R.string.settings_language_key));
        bindPreferenceSummaryToValue(languagePreference);

    }

    private void bindPreferenceSummaryToValue(android.support.v7.preference.Preference preference) {
        if (preference != null) {
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString = preferences.getString(preference.getKey(), "");
            if (preferenceString != null) {
                onPreferenceChange(preference, preferenceString);
            }
        }
    }


}