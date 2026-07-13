package com.example.tvlauncher.ui.allapps;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tvlauncher.data.model.AppInfo;
import com.example.tvlauncher.data.repository.AppRepository;
import com.example.tvlauncher.executor.AppExecutors;

import java.util.List;

/**
 * 应用列表 ViewModel，异步加载已安装应用
 */
public class AppListViewModel extends AndroidViewModel {

    private final MutableLiveData<List<AppInfo>> appListLiveData = new MutableLiveData<>();
    private final AppRepository appRepository;

    public AppListViewModel(@NonNull Application application) {
        super(application);
        appRepository = AppRepository.getInstance(application);
        loadApps();
    }

    // 暴露给 UI 层的应用列表数据
    public LiveData<List<AppInfo>> getAppList() {
        return appListLiveData;
    }

    // 后台加载应用
    private void loadApps() {
        AppExecutors.getInstance().executeDiskIO(() ->
                appListLiveData.postValue(appRepository.getInstalledApps())
        );
    }

    // 主动刷新（供外部调用）
    @SuppressWarnings("unused")
    public void refreshApps() {
        loadApps();
    }
}