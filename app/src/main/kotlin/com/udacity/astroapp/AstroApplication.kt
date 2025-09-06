package com.udacity.astroapp

import android.app.Application
import com.udacity.astroapp.di.databaseModule
import com.udacity.astroapp.di.networkModule
import com.udacity.astroapp.di.repositoryModule
import com.udacity.astroapp.di.utilModule
import com.udacity.astroapp.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class AstroApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger(Level.DEBUG)
            androidContext(this@AstroApplication)
            modules(
                databaseModule,
                networkModule,
                utilModule,
                repositoryModule,
                viewModelModule
            )
        }
    }
}