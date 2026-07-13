package com.example.tvlauncher.utils;

import android.os.Handler;
import android.os.Looper;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 自治时钟组件，跟随生命周期自动启停
 */
public class LifecycleClockManager implements DefaultLifecycleObserver {

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

    // 开始计时
    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                long now = System.currentTimeMillis();
                Date nowDate = new Date(now);
                tvTime.setText(timeFormat.format(nowDate));
                tvDate.setText(dateFormat.format(nowDate));
                handler.postDelayed(this, 1000);
            }
        };
        handler.post(clockRunnable);
    }

    // 停止计时
    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        if (clockRunnable != null) {
            handler.removeCallbacks(clockRunnable);
            clockRunnable = null;
        }
    }
}