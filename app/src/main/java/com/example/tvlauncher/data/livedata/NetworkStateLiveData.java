package com.example.tvlauncher.data.livedata;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

/**
 * 网络状态 LiveData，生命周期感知
 */

public class NetworkStateLiveData extends LiveData<Boolean> {

    private final ConnectivityManager cm;
    private ConnectivityManager.NetworkCallback callback;
    private int availableNetworkCount = 0; // 计数已连接的有效网络，避免多网络切换时状态错误

    public NetworkStateLiveData(Application application) {
        cm = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onActive() {
        super.onActive();
        availableNetworkCount = 0;
        registerCallback();
        // 初始化当前状态：支持WiFi、以太网、蜂窝（TV少见但兼容）
        Network activeNetwork = cm.getActiveNetwork();
        if (activeNetwork != null) {
            NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
            boolean hasValidNetwork = caps != null &&
                    (caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                            || caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                            || caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
            postValue(hasValidNetwork);
        } else {
            postValue(false);
        }
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        unregisterCallback();
    }

    private void registerCallback() {
        // 同时监听WiFi和以太网
        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .addTransportType(NetworkCapabilities.TRANSPORT_ETHERNET)
                .build();

        callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                availableNetworkCount++;
                postValue(true);// WiFi 已连接
            }

            @Override
            public void onLost(@NonNull Network network) {
                availableNetworkCount--;
                if (availableNetworkCount <= 0) {
                    availableNetworkCount = 0;
                    postValue(false);// WiFi 已断开
                }
            }
        };
        cm.registerNetworkCallback(request, callback);
    }

    private void unregisterCallback() {
        if (callback != null) {
            cm.unregisterNetworkCallback(callback);
            callback = null;
        }
    }
}
