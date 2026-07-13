package com.example.tvlauncher;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tvlauncher.ui.allapps.AppAdapter;
import com.example.tvlauncher.ui.allapps.AppListViewModel;

public class AllAppsActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AppAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_apps);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
        );

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        recyclerView = findViewById(R.id.recycler_all_apps);
        GridLayoutManager layoutManager = new GridLayoutManager(this, 5);
        recyclerView.setLayoutManager(layoutManager);

        // 焦点自动居中（一行搞定）
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