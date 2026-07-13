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

    public NetworkStateLiveData(Application application) {
        cm = (ConnectivityManager) application.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    protected void onActive() {
        super.onActive();
        registerCallback();
        // 初始化当前状态
        Network activeNetwork = cm.getActiveNetwork();
        if (activeNetwork != null) {
            NetworkCapabilities caps = cm.getNetworkCapabilities(activeNetwork);
            boolean isWifi = caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
            postValue(isWifi);
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
        NetworkRequest request = new NetworkRequest.Builder()
                .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
                .build();

        callback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(@NonNull Network network) {
                postValue(true);  // WiFi 已连接
            }

            @Override
            public void onLost(@NonNull Network network) {
                postValue(false); // WiFi 已断开
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