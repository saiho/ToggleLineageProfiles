package com.saiho.togglelineageprofiles.quicksettings;

import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import com.saiho.togglelineageprofiles.R;
import com.saiho.togglelineageprofiles.preferences.Pref;
import com.saiho.togglelineageprofiles.widget.ProfileWidgetPopup;

import static com.saiho.togglelineageprofiles.Common.checkSystemProfilesStatus;
import static com.saiho.togglelineageprofiles.Common.getCurrentProfile;


public class ProfileTileService extends TileService {
    @Override
    public void onStartListening() {
        super.onStartListening();

        Pref.loadContextPreferences(this);

        Tile tile = getQsTile();

        if (checkSystemProfilesStatus(this)) {
            String currentProfile = getCurrentProfile(this);
            if (currentProfile == null) {
                tile.setLabel(getString(R.string.widget_no_active_profile));
                tile.setIcon(null);
                tile.setState(Tile.STATE_INACTIVE);
            } else {
                tile.setLabel(currentProfile);
                Integer icon = Pref.profileIcons.get(currentProfile);
                if (icon == null) icon = Pref.DEFAULT_ICON;

                // Crop the margin of the icon to make it a little bigger
                Bitmap iconBitmap = BitmapFactory.decodeResource(getResources(), icon);
                int cropBorder = this.getResources().getInteger(R.integer.quick_settings_icon_crop_border);
                iconBitmap = Bitmap.createBitmap(iconBitmap, cropBorder, cropBorder, iconBitmap.getWidth() - cropBorder * 2, iconBitmap.getHeight() - cropBorder * 2);

                tile.setIcon(Icon.createWithBitmap(iconBitmap));
                tile.setState(Tile.STATE_ACTIVE);
            }
        } else {
            tile.setLabel(getString(R.string.widget_no_active_profile));
            tile.setIcon(null);
            tile.setState(Tile.STATE_UNAVAILABLE);
        }

        tile.updateTile();
    }

    @Override
    public void onClick() {
        super.onClick();

        Pref.loadContextPreferences(this);

        Intent intent = ProfileWidgetPopup.generateIntent(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intent, PendingIntent.FLAG_IMMUTABLE);
            startActivityAndCollapse(pendingIntent);
        } else {
            startActivityAndCollapse(intent);
        }
    }
}
