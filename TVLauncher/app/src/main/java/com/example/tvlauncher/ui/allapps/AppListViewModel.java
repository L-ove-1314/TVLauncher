package com.example.tvlauncher.ui.allapps;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.tvlauncher.data.model.AppInfo;
import com.example.tvlauncher.data.repository.AppRepository;
import com.example.tvlauncher.executor.AppExecutors;

import java.util.List;

/**
 * 应用列表 ViewModel
 * 负责异步加载应用数据，通过 LiveData 暴露给 UI 层
 */
public class AppListViewModel extends AndroidViewModel {

    private final MutableLiveData<List<AppInfo>> appListLiveData = new MutableLiveData<>();
    private final AppRepository appRepository;

    public AppListViewModel(Application application) {
        super(application);
        appRepository = AppRepository.getInstance(application);
        loadApps();
    }

    public LiveData<List<AppInfo>> getAppList() {
        return appListLiveData;
    }

    private void loadApps() {
        AppExecutors.getInstance().executeDiskIO(() -> {
            List<AppInfo> apps = appRepository.getInstalledApps();
            // postValue 可在后台线程调用，切回主线程更新
            appListLiveData.postValue(apps);
        });
    }
}