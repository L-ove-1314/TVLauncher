package com.example.tvlauncher.ui.allapps;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tvlauncher.R;
import com.example.tvlauncher.data.model.AppInfo;

/**
 * 应用列表适配器，使用 DiffUtil 增量刷新
 */
public class AppAdapter extends ListAdapter<AppInfo, AppAdapter.ViewHolder> {

    public AppAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AppInfo app = getItem(position);
        if (app == null) return;

        holder.icon.setImageDrawable(app.icon);
        holder.label.setText(app.label);

        holder.itemView.setOnClickListener(v -> {
            try {
                Intent launchIntent = v.getContext().getPackageManager()
                        .getLaunchIntentForPackage(app.packageName);
                if (launchIntent != null) v.getContext().startActivity(launchIntent);
            } catch (Exception ignored) {}
        });
    }

    // 差异比较回调
    private static final DiffUtil.ItemCallback<AppInfo> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<>() {
                @Override
                public boolean areItemsTheSame(@NonNull AppInfo oldItem, @NonNull AppInfo newItem) {
                    return oldItem.packageName.equals(newItem.packageName);
                }

                @Override
                public boolean areContentsTheSame(@NonNull AppInfo oldItem, @NonNull AppInfo newItem) {
                    if (oldItem.label == null || newItem.label == null) return false;
                    return oldItem.label.equals(newItem.label);
                }
            };

    // ViewHolder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final ImageView icon;
        public final TextView label;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_app_icon);
            label = itemView.findViewById(R.id.tv_app_name);
            itemView.setFocusable(true);
            itemView.setClickable(true);
        }
    }
}