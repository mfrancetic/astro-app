package com.udacity.astroapp.di

import com.udacity.astroapp.data.AppDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {
    single { AppDatabase.getInstance(androidContext()) }

    single { get<AppDatabase>().astroDao() }
}
