package com.saiho.togglelineageprofiles.preferences;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.support.annotation.ColorInt;
import android.support.v4.graphics.ColorUtils;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.saiho.togglelineageprofiles.R;

/**
 * This preference allows the user to choose a color opening a dialog window.
 */
public class ColorPickerPreference extends DialogPreference implements SeekBar.OnSeekBarChangeListener, TextWatcher {

    public static final int NO_COLOR = 0;

    private boolean showTransparencyBar = false;

    private int currentColor = NO_COLOR;
    private int newColor = NO_COLOR;
    private boolean refreshing = false;

    private ImageView prefImage = null;
    private ColorPickerBar hueBar, saturationBar, luminanceBar, alphaBar;
    private ColorPickerHSLDrawable previewDrawable;
    private EditText hexText;


    public ColorPickerPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public ColorPickerPreference(Context context, AttributeSet attrs) {
        // This constructor is necessary to allow adding this custom preference in preference.xml resources.
        super(context, attrs);
        init(context, attrs);
    }

    public ColorPickerPreference(Context context) {
        super(context);
        init(context, null);
    }

    private void init(Context context, AttributeSet attrs) {
        setDialogTitle(R.string.color_picker_dialog_title);
        setWidgetLayoutResource(R.layout.color_picker_pref_image);

        // Obtain the custom attribute that specifies if the color may have some transparency
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ColorPickerPreference);
            showTransparencyBar = a.getBoolean(R.styleable.ColorPickerPreference_showTransparencyBar, showTransparencyBar);
            a.recycle();
        }
    }

    /**
     * Method triggered when the user has accepted a new color.
     *
     * @param color The new selected color.
     */
    protected void onPickColor(@ColorInt int color) {
        currentColor = color;
        persistInt(color);
        refreshPrefImage();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        prefImage = view.findViewById(R.id.color_picker_pref_image);
        refreshPrefImage();
    }

    @Override
    protected View onCreateDialogView() {
        final View dialogLayout = LayoutInflater.from(getContext()).inflate(R.layout.color_picker_dialog, null);
        hueBar = dialogLayout.findViewById(R.id.color_picker_hue_bar);
        saturationBar = dialogLayout.findViewById(R.id.color_picker_saturation_bar);
        luminanceBar = dialogLayout.findViewById(R.id.color_picker_luminance_bar);
        alphaBar = dialogLayout.findViewById(R.id.color_picker_alpha_bar);
        TextView alphaText = dialogLayout.findViewById(R.id.color_picker_alpha_text);
        hexText = dialogLayout.findViewById(R.id.color_picker_hex_text);
        ImageView previewImage = dialogLayout.findViewById(R.id.color_picker_preview_image);

        if (!showTransparencyBar) {
            hexText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(6)});
            hexText.setEms(6);
            alphaText.setVisibility(View.GONE);
            alphaBar.setVisibility(View.GONE);
        }

        Resources res = getContext().getResources();
        previewDrawable = new ColorPickerHSLDrawable(ColorPickerHSLDrawable.SOLID_COMPONENT);
        previewDrawable.setAlphaBackgroundTile(res.getDimensionPixelSize(R.dimen.color_picker_alpha_background_tile_size));
        previewDrawable.setCornerRadius(res.getDimensionPixelSize(R.dimen.color_picker_preview_corner_radius));
        // Border is not actually drawn here, but is used to leave a margin. The real border is drawn from the foreground specified in the layout of previewImage
        previewDrawable.setBorder(res.getDimensionPixelSize(R.dimen.color_picker_preview_border_size) / 2, Color.TRANSPARENT);
        previewImage.setImageDrawable(previewDrawable);

        setNewColor(currentColor, true, true);

        hueBar.setOnSeekBarChangeListener(this);
        saturationBar.setOnSeekBarChangeListener(this);
        luminanceBar.setOnSeekBarChangeListener(this);
        alphaBar.setOnSeekBarChangeListener(this);
        hexText.addTextChangedListener(this);

        return dialogLayout;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            onPickColor(newColor);
        }
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            currentColor = getPersistedInt(NO_COLOR);
        } else {
            currentColor = (Integer) defaultValue;
        }
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getColor(index, NO_COLOR);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        // If the preference is persistent the value is automatically saved and restored from the shared preference storage
        if (isPersistent()) {
            return superState;
        }

        // If the preference has the attribute persistent="false" we have to provide a saved state
        return new SimpleSavedState(superState, currentColor);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        // If we didn't save the sate leave the superclass to handle it
        if (isPersistent() || state == null || !(state.getClass().equals(SimpleSavedState.class))) {
            super.onRestoreInstanceState(state);
            return;
        }

        SimpleSavedState simpleState = (SimpleSavedState) state;

        // Allow superclass to parse the parent state
        super.onRestoreInstanceState(simpleState.getSuperState());

        // Get our saved state
        currentColor = (Integer) simpleState.getValue();
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (refreshing || !fromUser) return;

        float hue = hueBar.getHueValue();
        float saturation = saturationBar.getSaturationValue();
        float luminance = luminanceBar.getLuminanceValue();

        if (seekBar == hueBar) {
            saturationBar.setHueValue(hue);
            luminanceBar.setHueValue(hue);
            alphaBar.setHueValue(hue);
        } else if (seekBar == saturationBar) {
            luminanceBar.setSaturationValue(saturation);
            alphaBar.setSaturationValue(saturation);
        } else if (seekBar == luminanceBar) {
            alphaBar.setLuminanceValue(luminance);
        }

        int color = ColorUtils.HSLToColor(new float[]{hue, saturation, luminance});
        if (showTransparencyBar) {
            color = ColorUtils.setAlphaComponent(color, alphaBar.getAlphaValue());
        }

        setNewColor(color, false, true);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (refreshing) return;
        String text = s.toString();
        if (text.matches(showTransparencyBar ? "[0-9a-fA-F]{8}" : "[0-9a-fA-F]{6}")) {
            setNewColor(Color.parseColor("#" + text), true, false);
        }
    }

    private void refreshPrefImage() {
        if (prefImage != null) {
            prefImage.setColorFilter(ColorUtils.setAlphaComponent(currentColor, 255), PorterDuff.Mode.SRC_IN);
        }
    }

    private void setNewColor(@ColorInt int color, boolean refreshBars, boolean refreshText) {
        newColor = color;
        refreshing = true;

        if (refreshBars) {
            float[] hsl = new float[3];
            ColorUtils.colorToHSL(color, hsl);

            hueBar.setHueValue(hsl[0]);

            saturationBar.setHueValue(hsl[0]);
            saturationBar.setSaturationValue(hsl[1]);

            luminanceBar.setHueValue(hsl[0]);
            luminanceBar.setSaturationValue(hsl[1]);
            luminanceBar.setLuminanceValue(hsl[2]);

            alphaBar.setHueValue(hsl[0]);
            alphaBar.setSaturationValue(hsl[1]);
            alphaBar.setLuminanceValue(hsl[2]);
            alphaBar.setAlphaValue(Color.alpha(color));
        }

        if (refreshText) {
            String colorText = "0000000" + Integer.toHexString(color).toUpperCase();
            colorText = colorText.substring(colorText.length() - (showTransparencyBar ? 8 : 6));
            hexText.setText(colorText);
        }

        previewDrawable.setSolidColor(color);

        refreshing = false;
    }
}
