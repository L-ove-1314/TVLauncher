package com.example.tvlauncher;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

/**
 * 自定义 Drawable：为任意 Bitmap 添加倒影效果
 *
 * 效果说明：
 * - 原图在上方
 * - 原图下方有一个垂直翻转的倒影（截取原图底部 30%）
 * - 倒影从上到下由不透明白色渐变成全透明（模拟镜面倒影）
 * - 原图与倒影之间有 4dp 的间距
 *
 * 使用方式：
 * ImageView icon = findViewById(R.id.iv_app_icon);
 * Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.xxx);
 * ReflectionDrawable reflection = new ReflectionDrawable(getResources(), bitmap);
 * icon.setImageDrawable(reflection);
 * icon.setAdjustViewBounds(true);
 */
public class ReflectionDrawable extends Drawable {

    // ============================================================
    // 一、成员变量
    // ============================================================

    // 1.1 原始 Bitmap（未经修改的完整图片）
    private final Bitmap sourceBitmap;

    // 1.2 倒影 Bitmap（截取原图底部 30%，垂直翻转后的图片）
    private final Bitmap reflectionBitmap;

    // 1.3 原图与倒影之间的间距（px）
    private final int gapPx;

    // 1.4 原图绘制画笔（带抗锯齿和过滤）
    private final Paint bitmapPaint;

    // 1.5 倒影绘制画笔（透明度由渐变遮罩控制，这里不设固定 Alpha）
    private final Paint reflectionPaint;

    // 1.6 渐变遮罩画笔（用于绘制倒影的渐隐效果）
    private final Paint maskPaint;

    // 1.7 倒影的高度（px）= 原图高度的 30%
    private final int reflectionHeight;

    // ============================================================
    // 二、构造函数
    // ============================================================

    /**
     * @param resources 用于 dp 转 px
     * @param bitmap    需要添加倒影的原始图片（不能为 null）
     */
    public ReflectionDrawable(Resources resources, Bitmap bitmap) {
        // 2.1 参数校验
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap must not be null");
        }

        // 2.2 保存原始图片
        this.sourceBitmap = bitmap;

        // 2.3 计算间距（4dp → px）
        this.gapPx = (int) (4 * resources.getDisplayMetrics().density);

        // 2.4 计算倒影高度（原图高度的 30%）
        this.reflectionHeight = (int) (bitmap.getHeight() * 0.3f);

        // 2.5 截取原图底部 30%，然后垂直翻转，生成倒影位图
        int srcTop = bitmap.getHeight() - reflectionHeight;  // 截取起始位置
        if (srcTop < 0) srcTop = 0;
        int actualReflectionH = bitmap.getHeight() - srcTop; // 实际截取高度

        Matrix flipMatrix = new Matrix();
        flipMatrix.preScale(1, -1);  // 垂直翻转矩阵
        this.reflectionBitmap = Bitmap.createBitmap(
                bitmap, 0, srcTop, bitmap.getWidth(), actualReflectionH, flipMatrix, true
        );

        // 2.6 初始化原图画笔（抗锯齿 + 位图过滤，保证缩放平滑）
        bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);

        // 2.7 初始化倒影画笔（透明度完全由渐变遮罩控制）
        reflectionPaint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.ANTI_ALIAS_FLAG);

        // 2.8 初始化渐变遮罩画笔
        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    // ============================================================
    // 三、核心绘制方法
    // ============================================================

    /**
     * 绘制逻辑：原图 → 倒影 → 渐变遮罩
     *
     * 整体布局（从上到下）：
     * ┌─────────────────────┐
     * │      原图           │
     * ├─────────────────────┤
     * │    4dp 间距         │
     * ├─────────────────────┤
     * │ 倒影（垂直翻转）     │ ← 从上到下渐隐
     * └─────────────────────┘
     */
    @Override
    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.isEmpty() || sourceBitmap.isRecycled()) return;

        // 3.1 获取可用区域尺寸
        int viewWidth = bounds.width();
        int viewHeight = bounds.height();
        int bmpWidth = sourceBitmap.getWidth();
        int bmpHeight = sourceBitmap.getHeight();
        int totalHeight = bmpHeight + gapPx + reflectionBitmap.getHeight();

        // 3.2 计算缩放比例（保持宽高比，适配 View 区域）
        float scale = Math.min((float) viewWidth / bmpWidth, (float) viewHeight / totalHeight);
        int drawWidth = (int) (bmpWidth * scale);
        int drawHeight = (int) (bmpHeight * scale);
        int drawRefH = (int) (reflectionBitmap.getHeight() * scale);

        // 3.3 计算居中位置
        int left = bounds.left + (viewWidth - drawWidth) / 2;
        int top = bounds.top + (viewHeight - drawHeight - drawRefH - gapPx) / 2;

        // 3.4 步骤一：绘制原图（上方）
        Rect srcRect = new Rect(0, 0, bmpWidth, bmpHeight);
        Rect dstRect = new Rect(left, top, left + drawWidth, top + drawHeight);
        canvas.drawBitmap(sourceBitmap, srcRect, dstRect, bitmapPaint);

        // 3.5 步骤二：绘制倒影（下方，翻转后的图片）
        int refTop = top + drawHeight + gapPx;  // 倒影起始位置 = 原图底部 + 间距
        Rect refSrc = new Rect(0, 0, reflectionBitmap.getWidth(), reflectionBitmap.getHeight());
        Rect refDst = new Rect(left, refTop, left + drawWidth, refTop + drawRefH);
        canvas.drawBitmap(reflectionBitmap, refSrc, refDst, reflectionPaint);

        // 3.6 步骤三：绘制渐变遮罩（从半透明白到全透明，模拟镜面反射渐隐）
        canvas.save();
        canvas.translate(left, refTop);  // 移到倒影的起始位置
        LinearGradient gradient = new LinearGradient(
                0, 0,              // 渐变起点（倒影顶部）
                0, drawRefH,       // 渐变终点（倒影底部）
                0xAAFFFFFF,        // 顶部颜色：半透明白色（AA = 约 67% 不透明）
                0x00FFFFFF,        // 底部颜色：全透明
                Shader.TileMode.CLAMP
        );
        maskPaint.setShader(gradient);
        canvas.drawRect(0, 0, drawWidth, drawRefH, maskPaint);
        canvas.restore();
    }

    // ============================================================
    // 四、尺寸相关
    // ============================================================

    /**
     * 返回内容的固有宽度（等于原图宽度）
     */
    @Override
    public int getIntrinsicWidth() {
        return sourceBitmap.getWidth();
    }

    /**
     * 返回内容的固有高度（原图高度 + 间距 + 倒影高度）
     */
    @Override
    public int getIntrinsicHeight() {
        return sourceBitmap.getHeight() + gapPx + reflectionBitmap.getHeight();
    }

    // ============================================================
    // 五、透明度与颜色滤镜（必须重写，委托给原图画笔）
    // ============================================================

    @Override
    public void setAlpha(int alpha) {
        bitmapPaint.setAlpha(alpha);
        invalidateSelf();  // 通知重绘
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        bitmapPaint.setColorFilter(colorFilter);
        invalidateSelf();  // 通知重绘
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;  // 支持透明像素
    }
}