package com.example.tvlauncher.ui.allapps;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.tvlauncher.R;
import com.example.tvlauncher.data.model.AppInfo;

import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    private List<AppInfo> apps;
    private RecyclerView recyclerView;

    public AppAdapter(List<AppInfo> apps, RecyclerView recyclerView) {
        this.apps = apps;
        this.recyclerView = recyclerView;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppInfo app = apps.get(position);
        holder.icon.setImageDrawable(app.icon);
        holder.label.setText(app.label);

        // 点击启动应用
        holder.itemView.setOnClickListener(v -> {
            try {
                Intent launchIntent = v.getContext().getPackageManager()
                        .getLaunchIntentForPackage(app.packageName);
                if (launchIntent != null) v.getContext().startActivity(launchIntent);
            } catch (Exception ignored) {}
        });

        // 焦点自动居中滚动
        holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && recyclerView != null) {
                recyclerView.smoothScrollToPosition(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return apps != null ? apps.size() : 0;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView label;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_app_icon);
            label = itemView.findViewById(R.id.tv_app_name);
            itemView.setFocusable(true);
            itemView.setClickable(true);
            itemView.setFocusableInTouchMode(true);
        }
    }
}