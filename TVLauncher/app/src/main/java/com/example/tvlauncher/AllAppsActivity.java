package com.example.tvlauncher;

import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tvlauncher.base.BaseActivity;
import com.example.tvlauncher.ui.allapps.AppAdapter;
import com.example.tvlauncher.ui.allapps.AppListViewModel;

public class AllAppsActivity extends BaseActivity {

    // 控件
    private RecyclerView recyclerView;
    private AppAdapter adapter;

    // 生命周期
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_apps);
        setFullScreen();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recycler_all_apps);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 5);
        recyclerView.setLayoutManager(layoutManager);

        // 焦点自动居中
        LinearSnapHelper snapHelper = new LinearSnapHelper();
        snapHelper.attachToRecyclerView(recyclerView);

        adapter = new AppAdapter();
        recyclerView.setAdapter(adapter);

        AppListViewModel viewModel = new ViewModelProvider(this).get(AppListViewModel.class);
        viewModel.getAppList().observe(this, appList -> {
            adapter.submitList(appList);

            if (!appList.isEmpty()) {
                recyclerView.post(() -> {
                    View firstChild = recyclerView.getChildAt(0);
                    if (firstChild != null) firstChild.requestFocus();
                });
            }
        });
    }
}