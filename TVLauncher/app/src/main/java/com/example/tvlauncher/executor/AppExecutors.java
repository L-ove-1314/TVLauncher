package com.example.tvlauncher.executor;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 全局线程池管理器
 */
public class AppExecutors {

    private static volatile AppExecutors instance;

    private final ExecutorService diskIO;
    private final Handler mainHandler;

    private AppExecutors() {
        this.diskIO = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    // 双重检查锁获取单例
    public static AppExecutors getInstance() {
        if (instance == null) {
            synchronized (AppExecutors.class) {
                if (instance == null) {
                    instance = new AppExecutors();
                }
            }
        }
        return instance;
    }

    // 后台 I/O 任务
    public void executeDiskIO(Runnable runnable) {
        diskIO.execute(runnable);
    }

    // 主线程任务
    @SuppressWarnings("unused")
    public void executeMainThread(Runnable runnable) {
        mainHandler.post(runnable);
    }
}