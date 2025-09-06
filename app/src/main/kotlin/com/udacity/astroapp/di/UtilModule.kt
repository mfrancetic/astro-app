package com.udacity.astroapp.di

import com.udacity.astroapp.data.preferences.ThemePreferences
import com.udacity.astroapp.utils.QueryUtils
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val utilModule = module {
    
    single {
        QueryUtils()
    }
    
    single {
        ThemePreferences(androidContext())
    }
}