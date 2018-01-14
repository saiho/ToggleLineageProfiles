package com.saiho.togglelineageprofiles.preferences;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.v4.graphics.ColorUtils;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.widget.SeekBar;

import com.saiho.togglelineageprofiles.R;

import static com.saiho.togglelineageprofiles.preferences.ColorPickerHSLDrawable.ALPHA_COMPONENT;
import static com.saiho.togglelineageprofiles.preferences.ColorPickerHSLDrawable.DEFAULT_HUE;
import static com.saiho.togglelineageprofiles.preferences.ColorPickerHSLDrawable.DEFAULT_LUMINANCE;
import static com.saiho.togglelineageprofiles.preferences.ColorPickerHSLDrawable.DEFAULT_SATURATION;
import static com.saiho.togglelineageprofiles.preferences.ColorPickerHSLDrawable.HUE_COMPONENT;
import static com.saiho.togglelineageprofiles.preferences.ColorPickerHSLDrawable.LUMINANCE_COMPONENT;
import static com.saiho.togglelineageprofiles.preferences.ColorPickerHSLDrawable.SATURATION_COMPONENT;

/**
 * This class is a customization of the standard SeekBar that allows to select the components of a color (hue,
 * saturation and luminance).
 */
public class ColorPickerBar extends AppCompatSeekBar implements SeekBar.OnSeekBarChangeListener {

    private static final float MAX_HUE = 360f;
    private static final float MAX_SATURATION = 1f;
    private static final float MAX_LUMINANCE = 1f;
    private static final int MAX_ALPHA = 255;
    private static final int MAX_PROGRESS = 360000;

    private int component = HUE_COMPONENT; // The component is the value that can be modified by the user dragging the seek bar
    private float hue = DEFAULT_HUE;
    private float saturation = DEFAULT_SATURATION;
    private float luminance = DEFAULT_LUMINANCE;
    private int alpha = MAX_ALPHA;

    private ColorPickerHSLDrawable barDrawable;
    private Drawable thumbForeground = null;
    private SeekBar.OnSeekBarChangeListener externalChangeListener = null;

    public ColorPickerBar(Context context) {
        super(context);
        init(context, null);
    }

    public ColorPickerBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public ColorPickerBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setSplitTrack(false);
        setMax(MAX_PROGRESS);

        // Obtain the custom attribute that specifies the component in the layout XML
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerBar);
            component = a.getInteger(R.styleable.ColorPickerBar_component, component);
            a.recycle();
        }

        // Drawable for the progress bar
        barDrawable = new ColorPickerHSLDrawable(component);
        barDrawable.setCornerRadius(context.getResources().getDimensionPixelSize(R.dimen.color_picker_bar_corner_radius));
        if (component == ALPHA_COMPONENT) {
            barDrawable.setAlphaBackgroundTile(context.getResources().getDimensionPixelSize(R.dimen.color_picker_alpha_background_tile_size));
        }
        setProgressDrawable(barDrawable);

        // Custom drawable for the thumb
        Drawable thumb = context.getDrawable(R.drawable.color_picker_thumb).mutate();
        setThumb(thumb);

        // Allow half of thumb to go beyond the end of the progress bar
        int padAlignment = thumb.getIntrinsicWidth() / 2;
        setThumbOffset(padAlignment);
        setPaddingRelative(padAlignment, getPaddingTop(), padAlignment, getPaddingBottom());

        // Get the part of the thumb that will change of color dynamically
        thumbForeground = ((LayerDrawable) thumb).findDrawableByLayerId(R.id.color_picker_thumb_foreground);
        thumbForeground.setTintMode(PorterDuff.Mode.SRC_IN);

        // Track internally the changes of the progress bar to change the color of the thumb
        super.setOnSeekBarChangeListener(this);
    }

    public float getHueValue() {
        return hue;
    }

    public void setHueValue(@FloatRange(from = 0, to = 360) float hue) {
        if (component == HUE_COMPONENT) {
            setProgress((int) (MAX_PROGRESS * hue / MAX_HUE));
        }
        this.hue = hue;
        barDrawable.setHue(hue);
        refreshThumbTint();
    }

    public float getSaturationValue() {
        return saturation;
    }

    public void setSaturationValue(@FloatRange(from = 0, to = 1) float saturation) {
        if (component == SATURATION_COMPONENT) {
            setProgress((int) (MAX_PROGRESS * saturation / MAX_SATURATION));
        }
        this.saturation = saturation;
        barDrawable.setSaturation(saturation);
        refreshThumbTint();
    }

    public float getLuminanceValue() {
        return luminance;
    }

    public void setLuminanceValue(@FloatRange(from = 0, to = 1) float luminance) {
        if (component == LUMINANCE_COMPONENT) {
            setProgress((int) (MAX_PROGRESS * luminance / MAX_LUMINANCE));
        }
        this.luminance = luminance;
        barDrawable.setLuminance(luminance);
        refreshThumbTint();
    }

    public int getAlphaValue() {
        return alpha;
    }

    public void setAlphaValue(@IntRange(from = 0, to = 255) int alpha) {
        if (component == ALPHA_COMPONENT) {
            setProgress(MAX_PROGRESS * (MAX_ALPHA - alpha) / MAX_ALPHA);
        }
        this.alpha = alpha;
        refreshThumbTint();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            switch (component) {
                case HUE_COMPONENT: {
                    hue = MAX_HUE * progress / MAX_PROGRESS;
                    break;
                }
                case SATURATION_COMPONENT: {
                    saturation = MAX_SATURATION * progress / MAX_PROGRESS;
                    break;
                }
                case LUMINANCE_COMPONENT: {
                    luminance = MAX_LUMINANCE * progress / MAX_PROGRESS;
                    break;
                }
                case ALPHA_COMPONENT: {
                    alpha = MAX_ALPHA - (MAX_ALPHA * progress / MAX_PROGRESS);
                    break;
                }
            }
            refreshThumbTint();
        }

        if (externalChangeListener != null)
            externalChangeListener.onProgressChanged(seekBar, progress, fromUser);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (externalChangeListener != null)
            externalChangeListener.onStartTrackingTouch(seekBar);
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (externalChangeListener != null)
            externalChangeListener.onStopTrackingTouch(seekBar);
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        // Do not call super.setOnSeekBarChangeListener to avoid losing the internal listener set in the constructor
        externalChangeListener = l;
    }

    private void refreshThumbTint() {
        if (thumbForeground != null) {
            int color = ColorUtils.HSLToColor(new float[]{hue, saturation, luminance});
            color = ColorUtils.setAlphaComponent(color, alpha);
            thumbForeground.setTint(color);
        }
    }
}
