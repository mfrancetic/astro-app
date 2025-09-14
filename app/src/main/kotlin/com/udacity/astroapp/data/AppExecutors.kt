package com.udacity.astroapp.data

import android.os.Handler
import android.os.Looper
import java.util.concurrent.Executor
import java.util.concurrent.Executors

/**
 * Global executor pools for the whole application.
 */
class AppExecutors private constructor(
    val diskIO: Executor,
    val mainThread: Executor,
    val networkIO: Executor
) {

    companion object {
        @Volatile
        private var executors: AppExecutors? = null

        fun getExecutors(): AppExecutors {
            return executors ?: synchronized(this) {
                executors ?: AppExecutors(
                    diskIO = Executors.newSingleThreadExecutor(),
                    networkIO = Executors.newFixedThreadPool(3),
                    mainThread = MainThreadExecutor()
                ).also { executors = it }
            }
        }
    }

    private class MainThreadExecutor : Executor {
        private val mainThreadHandler = Handler(Looper.getMainLooper())

        override fun execute(command: Runnable) {
            mainThreadHandler.post(command)
        }
    }
}