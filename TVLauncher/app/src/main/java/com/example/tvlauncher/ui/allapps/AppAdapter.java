package com.example.tvlauncher.ui.allapps;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tvlauncher.R;
import com.example.tvlauncher.data.model.AppInfo;

public class AppAdapter extends ListAdapter<AppInfo, AppAdapter.ViewHolder> {

    public AppAdapter() {
        super(DIFF_CALLBACK);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_app, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AppInfo app = getItem(position);
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

    private static final DiffUtil.ItemCallback<AppInfo> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<AppInfo>() {
                @Override
                public boolean areItemsTheSame(AppInfo oldItem, AppInfo newItem) {
                    return oldItem.packageName.equals(newItem.packageName);
                }

                @Override
                public boolean areContentsTheSame(AppInfo oldItem, AppInfo newItem) {
                    return oldItem.label.equals(newItem.label);
                }
            };

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView label;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.iv_app_icon);
            label = itemView.findViewById(R.id.tv_app_name);
            itemView.setFocusable(true);
            itemView.setClickable(true);
        }
    }
}