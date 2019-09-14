package com.udacity.astroapp.utils;

import android.content.res.Configuration;
import android.content.res.Resources;

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
}