package com.udacity.astroapp.data;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Global executor pools for the whole application.
 */
public class AppExecutors {

    /* Singleton instantiation */
    private static final Object LOCK = new Object();
    private static AppExecutors executors;
    private final Executor diskIO;
    private final Executor mainThread;
    private final Executor networkIO;

    private AppExecutors(Executor diskIO, Executor mainThread, Executor networkIO) {
        this.diskIO = diskIO;
        this.mainThread = mainThread;
        this.networkIO = networkIO;
    }

    public static AppExecutors getExecutors() {
        if (executors == null) {
            synchronized (LOCK) {
                executors = new AppExecutors(Executors.newSingleThreadExecutor(),
                        Executors.newFixedThreadPool(3),
                        new MainThreadExecutor());
            }
        }
        return executors;
    }

    public Executor diskIO() {
        return diskIO;
    }

    @SuppressWarnings("unused")
    public Executor mainThread() {
        return mainThread;
    }

    @SuppressWarnings("unused")
    public Executor networkIO() {
        return networkIO;
    }

    private static class MainThreadExecutor implements Executor {
        private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}