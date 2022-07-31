package com.saiho.togglelineageprofiles.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Parcelable;
import android.preference.DialogPreference;
import androidx.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.saiho.togglelineageprofiles.ProfileIconDrawable;
import com.saiho.togglelineageprofiles.ProfileIconList;
import com.saiho.togglelineageprofiles.R;

import java.util.Map;

/**
 * This preference allows the user to choose the image of an icon opening a dialog window that shows all existing icons.
 */
public class SelectIconPreference extends DialogPreference {

    public static final int DEFAULT_ICON_ID = Pref.DEFAULT_ICON;

    private int currentIconId = DEFAULT_ICON_ID;

    private ImageView prefImage = null;


    public SelectIconPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    public SelectIconPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SelectIconPreference(Context context, AttributeSet attrs) {
        // This constructor is necessary to allow adding this custom preference in preference.xml resources.
        super(context, attrs);
        init();
    }

    public SelectIconPreference(Context context) {
        super(context);
        init();
    }

    private static String findIconName(@DrawableRes int iconId) {
        for (Map.Entry<String, Integer> entry : ProfileIconList.ids.entrySet()) {
            if (entry.getValue().equals(iconId)) return entry.getKey();
        }
        return null;
    }

    private void init() {
        setDialogTitle(R.string.select_icon_dialog_title);
        setWidgetLayoutResource(R.layout.select_icon_pref_image);
    }

    /**
     * Method triggered when the user has selected a new icon.
     *
     * @param iconId The resource id of the new icon.
     */
    protected void onSelectIcon(@DrawableRes int iconId) {
        currentIconId = iconId;
        String iconName = findIconName(iconId);
        if (iconName != null)
            persistString(iconName);
        refreshPrefImage();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        prefImage = view.findViewById(R.id.select_icon_pref_image);
        refreshPrefImage();
    }

    @Override
    protected View onCreateDialogView() {
        final View dialogLayout = LayoutInflater.from(getContext()).inflate(R.layout.select_icon_dialog, null);
        final GridLayout grid = dialogLayout.findViewById(R.id.select_icon_table);

        Resources res = getContext().getResources();
        int cellSize = res.getDimensionPixelSize(R.dimen.select_icon_cell_size);
        int cellPadding = res.getDimensionPixelOffset(R.dimen.select_icon_cell_padding);

        int iconsPerRow = (res.getDisplayMetrics().widthPixels - res.getDimensionPixelSize(R.dimen.dialog_total_screen_margin)) / cellSize;
        if (iconsPerRow < 1) iconsPerRow = 1;
        grid.setColumnCount(iconsPerRow);

        for (Map.Entry<String, Integer> entry : ProfileIconList.ids.entrySet()) {
            IconView cellImage = new IconView(getContext(), entry.getValue());
            cellImage.setPadding(cellPadding, cellPadding, cellPadding, cellPadding);
            grid.addView(cellImage);
            cellImage.getLayoutParams().width = cellSize;
            cellImage.getLayoutParams().height = cellSize;
        }

        return dialogLayout;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setPositiveButton(null, null);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        Integer iconId;
        if (restoreValue) {
            iconId = ProfileIconList.ids.get(getPersistedString(null));
        } else {
            iconId = (Integer) defaultValue;
        }
        currentIconId = iconId == null ? DEFAULT_ICON_ID : iconId;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getResourceId(index, DEFAULT_ICON_ID);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        // If the preference is persistent the value is automatically saved and restored from the shared preference storage
        if (isPersistent()) {
            return superState;
        }

        // If the preference has the attribute persistent="false" we have to provide a saved state
        return new SimpleSavedState(superState, currentIconId);
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
        currentIconId = (Integer) simpleState.getValue();
    }

    private void refreshPrefImage() {
        if (prefImage != null) {
            prefImage.setImageDrawable(new ProfileIconDrawable(getContext(), currentIconId, true));
        }
    }

    private class IconView extends ImageButton {
        IconView(final Context context, @DrawableRes final int iconId) {
            super(context);
            setScaleType(ImageView.ScaleType.FIT_XY);
            setBackgroundColor(Color.TRANSPARENT);
            setImageDrawable(new ProfileIconDrawable(context, iconId, false));
            setOnClickListener(view -> {
                onSelectIcon(iconId);
                getDialog().dismiss();
            });
        }
    }
}
