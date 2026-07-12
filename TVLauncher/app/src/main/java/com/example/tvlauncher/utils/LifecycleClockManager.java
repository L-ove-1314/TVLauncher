package com.example.tvlauncher.utils;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 自治时钟组件：绑定 Activity/Fragment 生命周期
 * 在 onStart 时开始计时，在 onStop 时停止
 * 复用 SimpleDateFormat，避免每秒创建新对象
 */
public class LifecycleClockManager implements LifecycleObserver {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final SimpleDateFormat timeFormat;
    private final SimpleDateFormat dateFormat;
    private final TextView tvTime;
    private final TextView tvDate;
    private Runnable clockRunnable;

    public LifecycleClockManager(TextView tvTime, TextView tvDate) {
        this.tvTime = tvTime;
        this.tvDate = tvDate;
        this.timeFormat = new SimpleDateFormat("h:mm a", Locale.US);
        this.dateFormat = new SimpleDateFormat("EEEE, MMMM d", Locale.US);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    void start() {
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                tvTime.setText(timeFormat.format(new Date(now)));
                tvDate.setText(dateFormat.format(new Date(now)));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(clockRunnable);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    void stop() {
        if (clockRunnable != null) {
            handler.removeCallbacks(clockRunnable);
        }
    }
}