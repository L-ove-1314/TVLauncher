package com.example.tvlauncher.executor;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 全局线程池管理器
 * diskIO：用于磁盘读写、PackageManager 查询等 I/O 密集型任务
 * mainThread：用于切回主线程更新 UI
 */
public class AppExecutors {

    private static volatile AppExecutors instance;

    private final ExecutorService diskIO;
    private final Handler mainHandler;

    private AppExecutors() {
        this.diskIO = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static AppExecutors getInstance() {
        if (instance == null) {
            instance = new AppExecutors();
        }
        return instance;
    }

    public void executeDiskIO(Runnable runnable) {
        diskIO.execute(runnable);
    }

    public void executeMainThread(Runnable runnable) {
        mainHandler.post(runnable);
    }
}