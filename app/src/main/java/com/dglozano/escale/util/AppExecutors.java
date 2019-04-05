package com.dglozano.escale.util;

import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;

import com.dglozano.escale.di.annotation.ApplicationScope;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

@ApplicationScope
public class AppExecutors {

    private Executor diskIO;
    private Executor networkIO;
    private Executor mainThread;

    @Inject
    public AppExecutors() {
        this.diskIO = Executors.newSingleThreadExecutor();
        this.networkIO = Executors.newFixedThreadPool(3);
        this.mainThread = new MainThreadExecutor();
    }

    public Executor getDiskIO() {
        return diskIO;
    }

    public Executor getNetworkIO() {
        return networkIO;
    }

    public Executor getMainThread() {
        return mainThread;
    }

    public class MainThreadExecutor implements Executor {

        private final Handler handler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable runnable) {
            handler.post(runnable);
        }
    }
}
