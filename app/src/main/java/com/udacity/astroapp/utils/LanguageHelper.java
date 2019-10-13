package com.udacity.astroapp.utils;

import android.content.Context;

import com.udacity.astroapp.R;

import java.util.Locale;

public class LanguageHelper {

    public static String getSystemLanguage(Context context) {

        String language = Locale.getDefault().getLanguage();

        if (language.equals(context.getResources().getString(R.string.croatian_language_key))) {
            language = context.getResources().getString(R.string.croatian_language_key);
        } else {
            language = context.getResources().getString(R.string.english_language_key);
        }
        return language;
    }
}