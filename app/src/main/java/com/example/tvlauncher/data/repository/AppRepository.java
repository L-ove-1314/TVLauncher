package com.example.tvlauncher.data.repository;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.example.tvlauncher.data.model.AppInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 应用数据仓库，封装 PackageManager 查询
 */
public class AppRepository {

    private static AppRepository instance;
    private final Context appContext;

    private AppRepository(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static AppRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AppRepository(context);
        }
        return instance;
    }

    // 获取已安装应用列表（需在后台线程调用）
    public List<AppInfo> getInstalledApps() {
        PackageManager pm = appContext.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        List<AppInfo> apps = new ArrayList<>();
        String selfPackage = appContext.getPackageName();

        for (ResolveInfo info : resolveInfos) {
            if (info == null || info.activityInfo == null) continue;

            String packageName = info.activityInfo.packageName;
            if (selfPackage.equals(packageName)) continue;

            apps.add(new AppInfo(
                    packageName,
                    info.loadLabel(pm).toString(),
                    info.loadIcon(pm)
            ));
        }

        apps.sort((a, b) -> a.label.compareToIgnoreCase(b.label));
        return apps;
    }
}