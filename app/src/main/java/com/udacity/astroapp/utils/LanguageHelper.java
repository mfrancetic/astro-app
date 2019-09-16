package com.udacity.astroapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.preference.PreferenceManager;

import com.udacity.astroapp.R;
import com.udacity.astroapp.activities.MainActivity;

import java.util.Locale;

public class LanguageHelper {

    public static void changeLocale(Resources resources, String locale) {
        Configuration configuration;

        configuration = new Configuration(resources.getConfiguration());

        if ("hr".equals(locale)) {
            configuration.setLocale(new Locale("hr"));
        } else {
            configuration.setLocale(Locale.ENGLISH);
        }
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

    public static String getLocale(Context context) {
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        String language = sharedPreferences.getString(
                context.getString(R.string.settings_language_key),
                context.getString(R.string.settings_language_default));
        return language;
    }
}