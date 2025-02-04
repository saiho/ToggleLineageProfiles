package com.saiho.togglelineageprofiles;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.annotation.AttrRes;
import android.annotation.ColorInt;
import android.annotation.DrawableRes;

import com.saiho.togglelineageprofiles.preferences.Pref;

public class ProfileIconDrawable extends LayerDrawable {

    private static final int DISABLED_BACKGROUND_COLOR_ATTR = android.R.attr.colorControlNormal;
    private static final int DISABLED_FOREGROUND_COLOR_ATTR = android.R.attr.colorBackground;

    private static final int[] NOT_ENABLED_STATE_SET = new int[]{-android.R.attr.state_enabled}; // Negative state means NOT enabled
    private static final int[] ANY_STATE_SET = new int[0];

    public ProfileIconDrawable(Context context, @DrawableRes int iconId, boolean stateful) {
        super(buildLayers(context, iconId, stateful));
    }

    private static Drawable[] buildLayers(Context context, @DrawableRes int iconId, boolean stateful) {
        Drawable icon_background = context.getDrawable(R.drawable.profile_icon_background).mutate();
        icon_background.setTintMode(PorterDuff.Mode.SRC_IN);

        Drawable icon_image = context.getDrawable(iconId).mutate();
        icon_image.setTintMode(PorterDuff.Mode.SRC_IN);

        if (stateful) {
            final int[][] states = new int[][]{NOT_ENABLED_STATE_SET, ANY_STATE_SET};

            int disabled_background_color = getDisabledAttributeColor(context, DISABLED_BACKGROUND_COLOR_ATTR, Color.BLACK);
            int disabled_foreground_color = getDisabledAttributeColor(context, DISABLED_FOREGROUND_COLOR_ATTR, Color.WHITE);
            icon_background.setTintList(new ColorStateList(states, new int[]{disabled_background_color, Pref.iconBackgroundColor}));
            icon_image.setTintList(new ColorStateList(states, new int[]{disabled_foreground_color, Pref.iconForegroundColor}).withAlpha(255));
        } else {
            icon_background.setTint(Pref.iconBackgroundColor);
            icon_image.setTint(Pref.iconForegroundColor);
        }

        return new Drawable[]{icon_background, icon_image};
    }

    private static int getDisabledAttributeColor(Context context, @AttrRes int attrId, @ColorInt int defaultColor) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(new int[]{attrId});
        try {
            if (ta.hasValue(0)) {
                ColorStateList csl = ta.getColorStateList(0);
                if (csl != null) {
                    return csl.getColorForState(NOT_ENABLED_STATE_SET, defaultColor);
                }
            }

            //Log.w(Common.LOG_TAG, "ProfileIconDrawable: Attribute 0x" + Integer.toHexString(attrId) + " not found in context theme.");
            return defaultColor;
        } finally {
            ta.recycle();
        }
    }

    public Bitmap convertToBitmap(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Rect backBounds = copyBounds();
        setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        draw(canvas);
        setBounds(backBounds);
        return bitmap;
    }
}
