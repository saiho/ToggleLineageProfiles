package com.saiho.togglelineageprofiles.preferences;

import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

/**
 * Helper drawable for ColorPickerBar and ColorPickerPreference. It draws a gradient of colors where two components
 * (hue, saturation or luminance) are fixed and the other is variable ranging from the minimum value to the maximum.
 * <p>
 * Alternatively it can draw only a solid color (so no variable component), which is useful to show the transparency of
 * the specified color over a tiled background.
 */
class ColorPickerHSLDrawable extends Drawable {

    public static final int HUE_COMPONENT = 0;
    public static final int SATURATION_COMPONENT = 1;
    public static final int LUMINANCE_COMPONENT = 2;
    public static final int ALPHA_COMPONENT = 3;
    public static final int SOLID_COMPONENT = 4;

    static final float DEFAULT_HUE = 0f;
    static final float DEFAULT_SATURATION = 1f;
    static final float DEFAULT_LUMINANCE = 0.5f;

    private static final int ALPHA_BACKGROUND_COLOR_1 = 0xffffffff;
    private static final int ALPHA_BACKGROUND_COLOR_2 = 0xff808080;

    private final int component;

    private float hue = DEFAULT_HUE;
    private float saturation = DEFAULT_SATURATION;
    private float luminance = DEFAULT_LUMINANCE;
    private int solidColor = 0;
    private int borderSize = 0;
    private int cornerRadius = 0;

    private final Paint paintComponent = new Paint();
    private Paint paintBorder = null;
    private Paint paintAlphaBackground = null;

    private boolean pendingRefreshPaintComponent = true;

    ColorPickerHSLDrawable(@IntRange(from = 0, to = 4) int component) {
        super();
        this.component = component;
    }

    public void setBorder(int borderSize, @ColorInt int borderColor) {
        this.borderSize = borderSize;
        paintBorder = new Paint();
        paintBorder.setColor(borderColor);
        invalidateCallback();
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        invalidateCallback();
    }

    public void setHue(@FloatRange(from = 0, to = 360) float hue) {
        this.hue = hue;
        pendingRefreshPaintComponent = true;
        invalidateCallback();
    }

    public void setSaturation(@FloatRange(from = 0, to = 1) float saturation) {
        this.saturation = saturation;
        pendingRefreshPaintComponent = true;
        invalidateCallback();
    }

    public void setLuminance(@FloatRange(from = 0, to = 1) float luminance) {
        this.luminance = luminance;
        pendingRefreshPaintComponent = true;
        invalidateCallback();
    }

    public void setSolidColor(@ColorInt int color) {
        this.solidColor = color;
        pendingRefreshPaintComponent = true;
        invalidateCallback();
    }

    public void setAlphaBackgroundTile(int size) {
        if (size <= 0) {
            paintAlphaBackground = null;
        } else {
            // Create a bitmap consistent of tiled squares white and grey
            if (paintAlphaBackground == null) paintAlphaBackground = new Paint();
            Bitmap alphaBackground = Bitmap.createBitmap(new int[]{ALPHA_BACKGROUND_COLOR_1, ALPHA_BACKGROUND_COLOR_2, ALPHA_BACKGROUND_COLOR_2, ALPHA_BACKGROUND_COLOR_1}, 2, 2, Bitmap.Config.ARGB_8888);
            alphaBackground = Bitmap.createScaledBitmap(alphaBackground, size, size, false);
            paintAlphaBackground.setShader(new BitmapShader(alphaBackground, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT));
        }
        invalidateCallback();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        // As a general rule, avoid creating temporal objects in draw methods

        Rect bounds = getBounds();

        float left = bounds.left;
        float top = bounds.top;
        float right = bounds.right;
        float bottom = bounds.bottom;

        if (borderSize > 0) {
            canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, paintBorder);
            left += borderSize;
            top += borderSize;
            right -= borderSize;
            bottom -= borderSize;
        }

        if ((component == ALPHA_COMPONENT || component == SOLID_COMPONENT) && paintAlphaBackground != null) {
            // Be sure that the tiled pattern starts the sequence from the corner of the draw area
            Matrix matrix = new Matrix();
            matrix.reset();
            matrix.preTranslate(left, top);
            paintAlphaBackground.getShader().setLocalMatrix(matrix);

            canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, paintAlphaBackground);
        }

        if (pendingRefreshPaintComponent) refreshPaintComponent(left, right);
        canvas.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, paintComponent);
    }

    @Override
    public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getOpacity() {
        // This is Translucent because the rounded borders don't cover the full drawing area
        return PixelFormat.TRANSLUCENT;
    }

    private void refreshPaintComponent(float left, float right) {

        if (component == SOLID_COMPONENT) {
            paintComponent.setColor(solidColor);
            return;
        }

        float[] hsl = new float[]{hue, saturation, luminance};
        int[] colors;

        switch (component) {
            case HUE_COMPONENT: {
                colors = new int[181];
                for (int i = 0; i < 181; i++) {
                    hsl[HUE_COMPONENT] = i * 2; // Hue goes from 0 to 360
                    colors[i] = ColorUtils.HSLToColor(hsl);
                }
                break;
            }
            case SATURATION_COMPONENT: {
                colors = new int[2];
                hsl[SATURATION_COMPONENT] = 0f;
                colors[0] = ColorUtils.HSLToColor(hsl);
                hsl[SATURATION_COMPONENT] = 1f; // Saturation goes from 0 to 1
                colors[1] = ColorUtils.HSLToColor(hsl);
                break;
            }
            case LUMINANCE_COMPONENT: {
                colors = new int[181];
                for (int i = 0; i < 181; i++) {
                    hsl[LUMINANCE_COMPONENT] = i / 180f; // Luminance goes from 0 to 1
                    colors[i] = ColorUtils.HSLToColor(hsl);
                }
                break;
            }
            case ALPHA_COMPONENT: {
                colors = new int[2];
                colors[0] = ColorUtils.HSLToColor(hsl); // This is opaque
                colors[1] = ColorUtils.setAlphaComponent(colors[0], 0); // This is transparent
                break;
            }
            default:
                return;
        }

        paintComponent.setShader(new LinearGradient(left, 0, right, 0, colors, null, Shader.TileMode.CLAMP));
        pendingRefreshPaintComponent = false;
    }

    /**
     * If there is some callback listening to this drawable, let it know that the content has changed
     */
    private void invalidateCallback() {
        Callback c = getCallback();
        if (c != null) c.invalidateDrawable(this);
    }
}
