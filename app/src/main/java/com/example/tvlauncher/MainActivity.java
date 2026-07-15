package com.example.tvlauncher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.tvlauncher.base.BaseActivity;
import com.example.tvlauncher.data.livedata.NetworkStateLiveData;
import com.example.tvlauncher.utils.LifecycleClockManager;

import java.io.File;

public class MainActivity extends BaseActivity {

    // 控件声明
    private TextView tvTime, tvDate;
    private CardView cardNetflix, cardYoutube, cardPlay, cardChrome;
    private CardView btnKeystone, btnMiracast, btnSignalSource, btnMyApps, btnSettings;
    private ImageView ivUsb, ivWifi;

    // 焦点边框
    private Paint focusBorderPaint;
    private float BORDER_WIDTH;
    private float BORDER_RADIUS;

    // USB 监听
    private BroadcastReceiver usbReceiver;

    // 生命周期
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setFullScreen();

        initViews();
        setupUI();
        setupFocusControls();

        getLifecycle().addObserver(new LifecycleClockManager(tvTime, tvDate));

        NetworkStateLiveData wifiState = new NetworkStateLiveData(getApplication());
        wifiState.observe(this, isConnected -> {
            if (isConnected) {
                ivWifi.setImageResource(R.drawable.ic_wifi_on);
                ivWifi.setImageTintList(ColorStateList.valueOf(Color.WHITE));
            } else {
                ivWifi.setImageResource(R.drawable.ic_wifi_off);
                ivWifi.setImageTintList(null);
            }
        });

        initFocusBorderPaint();
        addFocusBorderToAllViews();
        registerUsbReceiver();

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                btnSettings.requestFocus();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usbReceiver != null) {
            unregisterReceiver(usbReceiver);
        }
    }

    // 视图绑定
    private void initViews() {
        tvTime = findViewById(R.id.tv_time);
        tvDate = findViewById(R.id.tv_date);

        LinearLayout containerNetflix = findViewById(R.id.card_netflix);
        LinearLayout containerYoutube = findViewById(R.id.card_youtube);
        LinearLayout containerPlay    = findViewById(R.id.card_play);
        LinearLayout containerChrome  = findViewById(R.id.card_chrome);

        cardNetflix = containerNetflix.findViewById(R.id.card_app);
        cardYoutube = containerYoutube.findViewById(R.id.card_app);
        cardPlay    = containerPlay.findViewById(R.id.card_app);
        cardChrome  = containerChrome.findViewById(R.id.card_app);

        btnKeystone      = findViewById(R.id.btn_keystone);
        btnMiracast      = findViewById(R.id.btn_miracast);
        btnSignalSource  = findViewById(R.id.btn_signal_source);
        btnMyApps        = findViewById(R.id.btn_my_apps);
        btnSettings      = findViewById(R.id.btn_settings);

        ivUsb  = findViewById(R.id.ic_usb);
        ivWifi = findViewById(R.id.ic_wifi);
    }

    // UI 设置
    private void setupUI() {
        int netflixColor = getColor(R.color.card_netflix_bg);
        int youtubeColor = getColor(R.color.card_youtube_bg);
        int playColor    = getColor(R.color.card_play_bg);
        int chromeColor  = getColor(R.color.card_chrome_bg);

        cardNetflix.setCardBackgroundColor(netflixColor);
        cardYoutube.setCardBackgroundColor(youtubeColor);
        cardPlay.setCardBackgroundColor(playColor);
        cardChrome.setCardBackgroundColor(chromeColor);

        setCardData(cardNetflix, R.drawable.netflix, "NETFLIX", netflixColor);
        setCardData(cardYoutube, R.drawable.youtube, "YouTube", youtubeColor);
        setCardData(cardPlay, R.drawable.google_play, "Google Play", playColor);
        setCardData(cardChrome, R.drawable.chrome, "chrome", chromeColor);

        setBtnIcon(btnKeystone, R.drawable.keystone, "Keystone");
        setBtnIcon(btnMiracast, R.drawable.miracast, "Miracast");
        setBtnIcon(btnSignalSource, R.drawable.signal_source, "Signal Source");
        setBtnIcon(btnMyApps, R.drawable.my_apps, "My Apps");
        setBtnIcon(btnSettings, R.drawable.settings, "Settings");
    }

    // 卡片数据
    private void setCardData(CardView card, int drawableId, String name, int bgColor) {
        ImageView icon = card.findViewById(R.id.iv_app_icon);
        icon.setImageResource(drawableId);

        TextView nameView = card.findViewById(R.id.tv_app_name);
        nameView.setText(name);

        LinearLayout container = (LinearLayout) card.getParent();
        TextView reflectNameView = container.findViewById(R.id.tv_app_name_reflect);
        if (reflectNameView != null) reflectNameView.setText(name);

        float cornerRadius = getResources().getDimension(R.dimen.app_card_corner_radius);
        View gradientView = container.findViewById(R.id.reflect_gradient_view);
        int startColor = (bgColor & 0x00FFFFFF) | 0x99000000;
        int endColor = 0x00000000;

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{startColor, endColor});
        gradient.setCornerRadii(new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius, 0, 0, 0, 0});
        gradientView.setBackground(gradient);
    }

    // 按钮图标
    private void setBtnIcon(CardView btn, int drawableId, String name) {
        ImageView icon = btn.findViewById(R.id.btn_icon);
        TextView nameView = btn.findViewById(R.id.btn_name);
        icon.setImageResource(drawableId);
        nameView.setText(name);
    }

    // 焦点控制
    private void setupFocusControls() {
        setChildrenNotFocusable(cardNetflix);
        setChildrenNotFocusable(cardYoutube);
        setChildrenNotFocusable(cardPlay);
        setChildrenNotFocusable(cardChrome);
        setChildrenNotFocusable(btnKeystone);
        setChildrenNotFocusable(btnMiracast);
        setChildrenNotFocusable(btnSignalSource);
        setChildrenNotFocusable(btnMyApps);
        setChildrenNotFocusable(btnSettings);

        cardNetflix.setFocusable(true);
        cardYoutube.setFocusable(true);
        cardPlay.setFocusable(true);
        cardChrome.setFocusable(true);
        btnKeystone.setFocusable(true);
        btnMiracast.setFocusable(true);
        btnSignalSource.setFocusable(true);
        btnMyApps.setFocusable(true);
        btnSettings.setFocusable(true);

        cardNetflix.setNextFocusDownId(R.id.btn_keystone);
        cardYoutube.setNextFocusDownId(R.id.btn_miracast);
        cardPlay.setNextFocusDownId(R.id.btn_signal_source);
        cardChrome.setNextFocusDownId(R.id.btn_settings);
        btnKeystone.setNextFocusUpId(R.id.card_netflix);
        btnMiracast.setNextFocusUpId(R.id.card_youtube);
        btnSignalSource.setNextFocusUpId(R.id.card_play);
        btnMyApps.setNextFocusUpId(R.id.card_play);
        btnSettings.setNextFocusUpId(R.id.card_chrome);

        cardNetflix.setNextFocusRightId(R.id.card_youtube);
        cardYoutube.setNextFocusLeftId(R.id.card_netflix);
        cardYoutube.setNextFocusRightId(R.id.card_play);
        cardPlay.setNextFocusLeftId(R.id.card_youtube);
        cardPlay.setNextFocusRightId(R.id.card_chrome);
        cardChrome.setNextFocusLeftId(R.id.card_play);

        btnKeystone.setNextFocusRightId(R.id.btn_miracast);
        btnMiracast.setNextFocusLeftId(R.id.btn_keystone);
        btnMiracast.setNextFocusRightId(R.id.btn_signal_source);
        btnSignalSource.setNextFocusLeftId(R.id.btn_miracast);
        btnSignalSource.setNextFocusRightId(R.id.btn_my_apps);
        btnMyApps.setNextFocusLeftId(R.id.btn_signal_source);
        btnMyApps.setNextFocusRightId(R.id.btn_settings);
        btnSettings.setNextFocusLeftId(R.id.btn_my_apps);

        ivUsb.setFocusable(true);
        ivUsb.setClickable(true);
        ivWifi.setFocusable(true);
        ivWifi.setClickable(true);

        cardNetflix.setNextFocusUpId(R.id.ic_usb);
        cardYoutube.setNextFocusUpId(R.id.ic_wifi);
        ivUsb.setNextFocusDownId(R.id.card_netflix);
        ivWifi.setNextFocusDownId(R.id.card_youtube);
        ivUsb.setNextFocusRightId(R.id.ic_wifi);
        ivWifi.setNextFocusLeftId(R.id.ic_usb);

        // 点击事件
        ivUsb.setOnClickListener(v -> {
            Intent fileIntent = new Intent(Intent.ACTION_VIEW);
            fileIntent.setDataAndType(Uri.parse("/storage"), "resource/folder");
            fileIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (fileIntent.resolveActivity(getPackageManager()) != null) {
                startActivity(fileIntent);
            } else {
                startActivity(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));
            }
        });

        ivWifi.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS)));

        btnSettings.requestFocus();

        btnSettings.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_SETTINGS)));
        btnMyApps.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AllAppsActivity.class)));

        btnKeystone.setOnClickListener(v -> Toast.makeText(this, "该设备暂不支持", Toast.LENGTH_SHORT).show());
        btnMiracast.setOnClickListener(v -> {
            try {
                startActivity(new Intent("android.settings.WIFI_DISPLAY_SETTINGS"));
            } catch (Exception e) {
                Toast.makeText(this, "该设备暂不支持", Toast.LENGTH_SHORT).show();
            }
        });
        btnSignalSource.setOnClickListener(v -> Toast.makeText(this, "该设备暂不支持", Toast.LENGTH_SHORT).show());

        cardNetflix.setOnClickListener(v -> launchAppRobust(
                new String[]{"com.netflix.ninja", "com.netflix.mediaclient"}, "https://www.netflix.com"));
        cardYoutube.setOnClickListener(v -> launchAppRobust(
                new String[]{"com.google.android.youtube.tv", "com.google.android.youtube", "com.android.smarttv.youtube"}, "https://www.youtube.com"));
        cardPlay.setOnClickListener(v -> launchAppRobust(
                new String[]{"com.android.vending"}, "https://play.google.com/store"));
        cardChrome.setOnClickListener(v -> launchAppRobust(
                new String[]{"com.android.chrome"}, "https://www.google.com"));
    }

    // 应用启动
    private void launchAppRobust(String[] packageNames, String fallbackUrl) {
        for (String pkg : packageNames) {
            Intent intent = getPackageManager().getLaunchIntentForPackage(pkg);
            if (intent != null) { startActivity(intent); return; }
        }
        if (fallbackUrl != null && !fallbackUrl.isEmpty()) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try { startActivity(webIntent); } catch (Exception ignored) {}
        }
    }

    // 禁止子控件抢焦点
    private void setChildrenNotFocusable(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) setChildrenNotFocusable(group.getChildAt(i));
        }
        view.setFocusable(false);
        view.setClickable(false);
    }

    // 焦点边框绘制
    private void initFocusBorderPaint() {
        BORDER_WIDTH = 2 * getResources().getDisplayMetrics().density;
        BORDER_RADIUS = 16 * getResources().getDisplayMetrics().density;
        focusBorderPaint = new Paint();
        focusBorderPaint.setColor(ContextCompat.getColor(this, android.R.color.white));
        focusBorderPaint.setStyle(Paint.Style.STROKE);
        focusBorderPaint.setStrokeWidth(BORDER_WIDTH);
        focusBorderPaint.setAntiAlias(true);
    }

    private void addFocusBorderToAllViews() {
        addFocusBorder(cardNetflix); addFocusBorder(cardYoutube); addFocusBorder(cardPlay); addFocusBorder(cardChrome);
        addFocusBorder(btnKeystone); addFocusBorder(btnMiracast); addFocusBorder(btnSignalSource);
        addFocusBorder(btnMyApps); addFocusBorder(btnSettings);
    }

    private void addFocusBorder(View view) {
        view.setWillNotDraw(false);
        view.setOnFocusChangeListener((v, hasFocus) -> {
            v.invalidate();
            float scale = hasFocus ? 1.06f : 1.0f;
            v.animate().scaleX(scale).scaleY(scale).setDuration(200)
                    .setInterpolator(new DecelerateInterpolator()).start();
        });
        view.setForeground(new Drawable() {
            @Override public void draw(@NonNull Canvas canvas) {
                if (view.isFocused()) {
                    RectF borderRect = new RectF(BORDER_WIDTH / 2, BORDER_WIDTH / 2,
                            view.getWidth() - BORDER_WIDTH / 2, view.getHeight() - BORDER_WIDTH / 2);
                    canvas.drawRoundRect(borderRect, BORDER_RADIUS, BORDER_RADIUS, focusBorderPaint);
                }
            }
            @Override public void setAlpha(int alpha) {}
            @Override public void setColorFilter(ColorFilter colorFilter) {}
            @Override public int getOpacity() { return PixelFormat.TRANSLUCENT; }
        });
    }

    // 全局按键
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BUTTON_Y) {
            Intent intent = new Intent(MainActivity.this, AllAppsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // USB 插拔监听（媒体挂载广播）
    private void registerUsbReceiver() {
        usbReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(@NonNull Context context, @NonNull Intent intent) {
                String action = intent.getAction();
                if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                    ivUsb.setVisibility(View.VISIBLE);
                } else if (Intent.ACTION_MEDIA_UNMOUNTED.equals(action)
                        || Intent.ACTION_MEDIA_REMOVED.equals(action)) {
                    ivUsb.setVisibility(View.GONE);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addDataScheme("file");
        registerReceiver(usbReceiver, filter);

        ivUsb.setVisibility(externalStorageExists() ? View.VISIBLE : View.GONE);
    }

    // 检查是否有外部存储
    private boolean externalStorageExists() {
        File[] dirs = getExternalFilesDirs(null);
        return dirs != null && dirs.length > 1;
    }
}
