package com.example.tvlauncher.data.model;

import android.graphics.drawable.Drawable;

/**
 * 应用信息实体类
 */
public class AppInfo {
    public String packageName;
    public String label;
    public Drawable icon;

    public AppInfo(String packageName, String label, Drawable icon) {
        this.packageName = packageName;
        this.label = label;
        this.icon = icon;
    }
}