package com.udacity.astroapp.di

import com.udacity.astroapp.utils.QueryUtils
import org.koin.dsl.module

val utilModule = module {
    
    single {
        QueryUtils()
    }
}