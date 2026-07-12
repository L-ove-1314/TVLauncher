package com.example.tvlauncher;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.tvlauncher.data.livedata.NetworkStateLiveData;
import com.example.tvlauncher.utils.LifecycleClockManager;

public class MainActivity extends AppCompatActivity {

    // ============================================================
    // 一、控件声明
    // ============================================================

    // 1.1 顶部状态栏 —— 时间日期
    private TextView tvTime, tvDate;

    // 1.2 四个应用卡片
    private CardView cardNetflix, cardYoutube, cardPlay, cardChrome;

    // 1.3 底部五个功能按钮
    private CardView btnKeystone, btnMiracast, btnSignalSource, btnMyApps, btnSettings;

    // 1.4 顶部状态栏图标（USB / WiFi）
    private ImageView ivUsb, ivWifi;

    // ============================================================
    // 二、焦点边框相关
    // ============================================================
    private Paint focusBorderPaint;
    private float BORDER_WIDTH;   // 边框宽度（px）
    private float BORDER_RADIUS;  // 边框圆角（px）

    // ============================================================
    // 三、生命周期
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 3.1 全屏沉浸式显示
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN);

        // 3.2 初始化顺序：视图 → UI → 焦点 → 时钟 → WiFi监听 → 边框
        initViews();
        setupUI();
        setupFocusControls();

        // 时钟：一行绑定，自动跟随生命周期
        getLifecycle().addObserver(new LifecycleClockManager(tvTime, tvDate));

        // WiFi：声明式观察，自动切换图标
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

        // 3.3 返回键处理：不让桌面退出，焦点回到 Settings 按钮
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                btnSettings.requestFocus();
            }
        });
    }

    // ============================================================
    // 四、视图绑定
    // ============================================================

    private void initViews() {
        // 4.1 时间日期
        tvTime = findViewById(R.id.tv_time);
        tvDate = findViewById(R.id.tv_date);

        // 4.2 四个应用卡片（include 引入的布局，需先找到父容器再拿子控件）
        LinearLayout containerNetflix = findViewById(R.id.card_netflix);
        LinearLayout containerYoutube = findViewById(R.id.card_youtube);
        LinearLayout containerPlay    = findViewById(R.id.card_play);
        LinearLayout containerChrome  = findViewById(R.id.card_chrome);

        cardNetflix = containerNetflix.findViewById(R.id.card_app);
        cardYoutube = containerYoutube.findViewById(R.id.card_app);
        cardPlay    = containerPlay.findViewById(R.id.card_app);
        cardChrome  = containerChrome.findViewById(R.id.card_app);

        // 4.3 底部按钮
        btnKeystone      = findViewById(R.id.btn_keystone);
        btnMiracast      = findViewById(R.id.btn_miracast);
        btnSignalSource  = findViewById(R.id.btn_signal_source);
        btnMyApps        = findViewById(R.id.btn_my_apps);
        btnSettings      = findViewById(R.id.btn_settings);

        // 4.4 顶部图标
        ivUsb  = findViewById(R.id.ic_usb);
        ivWifi = findViewById(R.id.ic_wifi);
    }

    // ============================================================
    // 五、UI 样式设置（颜色、图标、文字）
    // ============================================================

    private void setupUI() {
        // 5.1 卡片背景色
        int netflixColor = getColor(R.color.card_netflix_bg);
        int youtubeColor = getColor(R.color.card_youtube_bg);
        int playColor    = getColor(R.color.card_play_bg);
        int chromeColor  = getColor(R.color.card_chrome_bg);

        cardNetflix.setCardBackgroundColor(netflixColor);
        cardYoutube.setCardBackgroundColor(youtubeColor);
        cardPlay.setCardBackgroundColor(playColor);
        cardChrome.setCardBackgroundColor(chromeColor);

        // 5.2 设置卡片数据（图标、名称、背景色）
        setCardData(cardNetflix, R.drawable.netflix, "NETFLIX", netflixColor);
        setCardData(cardYoutube, R.drawable.youtube, "YouTube", youtubeColor);
        setCardData(cardPlay, R.drawable.google_play, "Google Play", playColor);
        setCardData(cardChrome, R.drawable.chrome, "chrome", chromeColor);

        // 5.3 设置底部按钮图标和文字
        setBtnIcon(btnKeystone, R.drawable.keystone, "Keystone");
        setBtnIcon(btnMiracast, R.drawable.miracast, "Miracast");
        setBtnIcon(btnSignalSource, R.drawable.signal_source, "Signal Source");
        setBtnIcon(btnMyApps, R.drawable.my_apps, "My Apps");
        setBtnIcon(btnSettings, R.drawable.settings, "Settings");
    }

    /**
     * 设置单个卡片的数据：图标、名称、背景渐变、文字倒影
     */
    private void setCardData(CardView card, int drawableId, String name, int bgColor) {
        ImageView icon = card.findViewById(R.id.iv_app_icon);
        icon.setImageResource(drawableId);

        TextView nameView = card.findViewById(R.id.tv_app_name);
        nameView.setText(name);

        // 文字倒影
        LinearLayout container = (LinearLayout) card.getParent();
        TextView reflectNameView = container.findViewById(R.id.tv_app_name_reflect);
        if (reflectNameView != null) {
            reflectNameView.setText(name);
        }

        // 倒影渐变遮罩
        float cornerRadius = getResources().getDimension(R.dimen.app_card_corner_radius);
        View gradientView = container.findViewById(R.id.reflect_gradient_view);
        int startColor = (bgColor & 0x00FFFFFF) | 0x99000000;
        int endColor = 0x00000000;

        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{startColor, endColor}
        );
        gradient.setCornerRadii(new float[]{cornerRadius, cornerRadius, cornerRadius, cornerRadius, 0, 0, 0, 0});
        gradientView.setBackground(gradient);
    }

    /**
     * 设置底部按钮的图标和文字
     */
    private void setBtnIcon(CardView btn, int drawableId, String name) {
        ImageView icon = btn.findViewById(R.id.btn_icon);
        TextView nameView = btn.findViewById(R.id.btn_name);
        icon.setImageResource(drawableId);
        nameView.setText(name);
    }

    // ============================================================
    // 六、焦点控制（核心：焦点导航 + 点击事件）
    // ============================================================

    private void setupFocusControls() {
        // 6.1 禁用子控件抢焦点（让焦点停留在卡片/按钮的根布局上）
        setChildrenNotFocusable(cardNetflix);
        setChildrenNotFocusable(cardYoutube);
        setChildrenNotFocusable(cardPlay);
        setChildrenNotFocusable(cardChrome);
        setChildrenNotFocusable(btnKeystone);
        setChildrenNotFocusable(btnMiracast);
        setChildrenNotFocusable(btnSignalSource);
        setChildrenNotFocusable(btnMyApps);
        setChildrenNotFocusable(btnSettings);

        // 6.2 允许卡片和按钮获取焦点
        cardNetflix.setFocusable(true);
        cardYoutube.setFocusable(true);
        cardPlay.setFocusable(true);
        cardChrome.setFocusable(true);
        btnKeystone.setFocusable(true);
        btnMiracast.setFocusable(true);
        btnSignalSource.setFocusable(true);
        btnMyApps.setFocusable(true);
        btnSettings.setFocusable(true);

        // ---------- 6.3 焦点导航关系（上下左右） ----------

        // 卡片 ↔ 底部按钮（上下）
        cardNetflix.setNextFocusDownId(R.id.btn_keystone);
        cardYoutube.setNextFocusDownId(R.id.btn_miracast);
        cardPlay.setNextFocusDownId(R.id.btn_signal_source);
        cardChrome.setNextFocusDownId(R.id.btn_settings);

        btnKeystone.setNextFocusUpId(R.id.card_netflix);
        btnMiracast.setNextFocusUpId(R.id.card_youtube);
        btnSignalSource.setNextFocusUpId(R.id.card_play);
        btnMyApps.setNextFocusUpId(R.id.card_play);
        btnSettings.setNextFocusUpId(R.id.card_chrome);

        // 卡片之间（左右）
        cardNetflix.setNextFocusRightId(R.id.card_youtube);
        cardYoutube.setNextFocusLeftId(R.id.card_netflix);
        cardYoutube.setNextFocusRightId(R.id.card_play);
        cardPlay.setNextFocusLeftId(R.id.card_youtube);
        cardPlay.setNextFocusRightId(R.id.card_chrome);
        cardChrome.setNextFocusLeftId(R.id.card_play);

        // 底部按钮之间（左右）
        btnKeystone.setNextFocusRightId(R.id.btn_miracast);
        btnMiracast.setNextFocusLeftId(R.id.btn_keystone);
        btnMiracast.setNextFocusRightId(R.id.btn_signal_source);
        btnSignalSource.setNextFocusLeftId(R.id.btn_miracast);
        btnSignalSource.setNextFocusRightId(R.id.btn_my_apps);
        btnMyApps.setNextFocusLeftId(R.id.btn_signal_source);
        btnMyApps.setNextFocusRightId(R.id.btn_settings);
        btnSettings.setNextFocusLeftId(R.id.btn_my_apps);

        // ---------- 6.4 顶部状态栏图标焦点 ----------
        ivUsb.setFocusable(true);
        ivUsb.setClickable(true);
        ivWifi.setFocusable(true);
        ivWifi.setClickable(true);

        // 卡片向上 → 跳到顶部图标
        cardNetflix.setNextFocusUpId(R.id.ic_usb);
        cardYoutube.setNextFocusUpId(R.id.ic_wifi);
        // 图标向下 → 回到卡片
        ivUsb.setNextFocusDownId(R.id.card_netflix);
        ivWifi.setNextFocusDownId(R.id.card_youtube);
        // 顶部图标左右移动
        ivUsb.setNextFocusRightId(R.id.ic_wifi);
        ivWifi.setNextFocusLeftId(R.id.ic_usb);

        // ---------- 6.5 顶部图标点击事件 ----------
        // USB：优先打开文件管理器，失败则跳转存储设置
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

        // WiFi：直接跳转系统 WiFi 设置
        ivWifi.setOnClickListener(v -> {
            startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
        });

        // ---------- 6.6 默认焦点 ----------
        btnSettings.requestFocus();

        // ---------- 6.7 底部按钮点击事件 ----------
        // Settings → 系统设置
        btnSettings.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_SETTINGS)));
        // My Apps → 全部应用列表
        btnMyApps.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AllAppsActivity.class)));

        // Keystone（梯形校正）：暂无通用接口，提示暂不支持。厂商接口已预留
        btnKeystone.setOnClickListener(v -> {
            // ***** 厂商接口预留 *****
            // 如果硬件厂商提供了梯形校正的 Activity，请取消下面的注释，
            // 并将包名和类名替换为真实值
            /*
            try {
                Intent intent = new Intent();
                intent.setClassName("com.example.projector", "com.example.projector.KeystoneActivity");
                startActivity(intent);
                return;
            } catch (Exception e) {
            }
            */
            Toast.makeText(this, "该设备暂不支持", Toast.LENGTH_SHORT).show();
        });

        // Miracast（无线投屏）：优先系统投屏设置，失败则提示。厂商接口已预留
        btnMiracast.setOnClickListener(v -> {
            // ***** 厂商接口预留 *****
            // 如果厂商有自己的投屏应用，请取消下面的注释并填写包名/类名
            /*
            try {
                Intent intent = new Intent();
                intent.setClassName("com.example.tvcast", "com.example.tvcast.MainActivity");
                startActivity(intent);
                return;
            } catch (Exception e) {
            }
            */
            try {
                startActivity(new Intent("android.settings.WIFI_DISPLAY_SETTINGS"));
            } catch (Exception e) {
                Toast.makeText(this, "该设备暂不支持", Toast.LENGTH_SHORT).show();
            }
        });

        // Signal Source（信号源切换）：暂无通用接口，提示暂不支持。厂商接口已预留
        btnSignalSource.setOnClickListener(v -> {
            // ***** 厂商接口预留 *****
            // 如果硬件厂商提供了信号源切换页面，请取消下面的注释并替换包名/类名
            /*
            try {
                Intent intent = new Intent();
                intent.setClassName("com.example.tvinput", "com.example.tvinput.SourceSelectActivity");
                startActivity(intent);
                return;
            } catch (Exception e) {
            }
            */
            Toast.makeText(this, "该设备暂不支持", Toast.LENGTH_SHORT).show();
        });

        // ---------- 6.8 四个应用卡片点击启动 ----------
        cardNetflix.setOnClickListener(v -> launchAppRobust(
                new String[]{"com.netflix.ninja", "com.netflix.mediaclient"},
                "https://www.netflix.com"));

        cardYoutube.setOnClickListener(v -> launchAppRobust(
                new String[]{"com.google.android.youtube.tv", "com.google.android.youtube", "com.android.smarttv.youtube"},
                "https://www.youtube.com"));

        cardPlay.setOnClickListener(v -> launchAppRobust(
                new String[]{"com.android.vending"},
                "https://play.google.com/store"));

        cardChrome.setOnClickListener(v -> launchAppRobust(
                new String[]{"com.android.chrome"},
                "https://www.google.com"));
    }

    // ============================================================
    // 七、应用启动工具方法（健壮启动：多包名 + 网页兜底）
    // ============================================================

    /**
     * 依次尝试多个包名启动应用，若都不存在则用网页后备
     * @param packageNames 可能的包名列表
     * @param fallbackUrl  后备网页 URL
     */
    private void launchAppRobust(String[] packageNames, String fallbackUrl) {
        for (String pkg : packageNames) {
            Intent intent = getPackageManager().getLaunchIntentForPackage(pkg);
            if (intent != null) {
                startActivity(intent);
                return;
            }
        }
        if (fallbackUrl != null && !fallbackUrl.isEmpty()) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(fallbackUrl));
            webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                startActivity(webIntent);
            } catch (Exception ignored) {
            }
        }
    }

    // ============================================================
    // 八、焦点辅助方法
    // ============================================================

    /**
     * 递归禁止子控件获取焦点，确保焦点停留在父布局上
     */
    private void setChildrenNotFocusable(View view) {
        if (view instanceof android.view.ViewGroup) {
            android.view.ViewGroup group = (android.view.ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                setChildrenNotFocusable(group.getChildAt(i));
            }
        }
        view.setFocusable(false);
        view.setClickable(false);
    }

    // ============================================================
    // 九、焦点边框绘制
    // ============================================================

    /**
     * 初始化焦点边框画笔
     */
    private void initFocusBorderPaint() {
        BORDER_WIDTH = 2 * getResources().getDisplayMetrics().density;
        BORDER_RADIUS = 16 * getResources().getDisplayMetrics().density;
        focusBorderPaint = new Paint();
        focusBorderPaint.setColor(ContextCompat.getColor(this, android.R.color.white));
        focusBorderPaint.setStyle(Paint.Style.STROKE);
        focusBorderPaint.setStrokeWidth(BORDER_WIDTH);
        focusBorderPaint.setAntiAlias(true);
    }

    /**
     * 为所有卡片和按钮添加焦点边框
     */
    private void addFocusBorderToAllViews() {
        addFocusBorder(cardNetflix);
        addFocusBorder(cardYoutube);
        addFocusBorder(cardPlay);
        addFocusBorder(cardChrome);
        addFocusBorder(btnKeystone);
        addFocusBorder(btnMiracast);
        addFocusBorder(btnSignalSource);
        addFocusBorder(btnMyApps);
        addFocusBorder(btnSettings);
    }

    /**
     * 为单个 View 添加焦点边框 + 缩放动画
     */
    private void addFocusBorder(View view) {
        view.setWillNotDraw(false);
        view.setOnFocusChangeListener((v, hasFocus) -> {
            v.invalidate();
            // 等比缩放动画
            float scale = hasFocus ? 1.06f : 1.0f;
            v.animate().scaleX(scale).scaleY(scale)
                    .setDuration(200)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        });

        // 绘制白色圆角边框
        view.setForeground(new android.graphics.drawable.Drawable() {
            @Override
            public void draw(Canvas canvas) {
                if (view.isFocused()) {
                    RectF borderRect = new RectF(
                            BORDER_WIDTH / 2, BORDER_WIDTH / 2,
                            view.getWidth() - BORDER_WIDTH / 2,
                            view.getHeight() - BORDER_WIDTH / 2);
                    canvas.drawRoundRect(borderRect, BORDER_RADIUS, BORDER_RADIUS, focusBorderPaint);
                }
            }
            @Override public void setAlpha(int alpha) {}
            @Override public void setColorFilter(android.graphics.ColorFilter colorFilter) {}
            @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }
        });
    }

    // ============================================================
    // 十、全局按键拦截
    // ============================================================

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 菜单键 / 黄色功能键 → 打开全部应用列表（防重复堆叠）
        if (keyCode == KeyEvent.KEYCODE_MENU || keyCode == KeyEvent.KEYCODE_BUTTON_Y) {
            Intent intent = new Intent(MainActivity.this, AllAppsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}