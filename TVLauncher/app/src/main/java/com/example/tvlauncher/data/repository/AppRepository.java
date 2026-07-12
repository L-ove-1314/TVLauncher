package com.example.tvlauncher.data.repository;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.example.tvlauncher.data.model.AppInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 应用数据仓库：封装 PackageManager 查询，负责获取所有已安装的可启动应用
 * 不持有 Activity Context，使用 ApplicationContext 避免内存泄漏
 */
public class AppRepository {

    private static AppRepository instance;
    private final Context appContext;

    private AppRepository(Context context) {
        // 使用 ApplicationContext，生命周期跟随应用
        this.appContext = context.getApplicationContext();
    }

    public static AppRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AppRepository(context);
        }
        return instance;
    }

    /**
     * 同步获取已安装的可启动应用列表（过滤自身、按名称排序）
     * 注意：此方法包含 I/O 操作，应在后台线程调用
     */
    public List<AppInfo> getInstalledApps() {
        PackageManager pm = appContext.getPackageManager();
        Intent mainIntent = new Intent(Intent.ACTION_MAIN);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(mainIntent, 0);
        List<AppInfo> apps = new ArrayList<>();
        String selfPackage = appContext.getPackageName();

        for (ResolveInfo info : resolveInfos) {
            String packageName = info.activityInfo.packageName;
            if (selfPackage.equals(packageName)) continue;

            apps.add(new AppInfo(
                    packageName,
                    info.loadLabel(pm).toString(),
                    info.loadIcon(pm)
            ));
        }

        Collections.sort(apps, (a, b) -> a.label.compareToIgnoreCase(b.label));
        return apps;
    }
}