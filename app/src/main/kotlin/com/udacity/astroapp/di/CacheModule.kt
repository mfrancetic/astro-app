package com.udacity.astroapp.di

import com.udacity.astroapp.cache.CacheManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val cacheModule = module { single { CacheManager(androidContext()) } }
