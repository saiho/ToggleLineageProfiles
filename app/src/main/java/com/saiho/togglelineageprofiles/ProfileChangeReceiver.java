package com.saiho.togglelineageprofiles;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.util.Log;

import com.saiho.togglelineageprofiles.preferences.Pref;
import com.saiho.togglelineageprofiles.widget.ProfileWidget;

import static com.saiho.togglelineageprofiles.Common.LOG_TAG;
import static com.saiho.togglelineageprofiles.Common.getCurrentProfile;

public class ProfileChangeReceiver extends BroadcastReceiver {

    public static final int NOTIFICATION_ID_PROFILE_CHANGED = 1;
    public static String dontNotifyChangedProfile = null;

    private static Boolean cachedManifestStateEnabled = null;

    public static void refreshManifestState(Context context) {
        int newState;
        if (!Pref.profileNotify.isEmpty() || ProfileWidget.isVisible(context)) {
            if (cachedManifestStateEnabled != null && cachedManifestStateEnabled) return;
            cachedManifestStateEnabled = true;
            newState = PackageManager.COMPONENT_ENABLED_STATE_ENABLED;
            Log.i(LOG_TAG, "Enable Profile Change Receiver");
        } else {
            if (cachedManifestStateEnabled != null && !cachedManifestStateEnabled) return;
            cachedManifestStateEnabled = false;
            newState = PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
            Log.i(LOG_TAG, "Disable Profile Change Receiver");
        }

        PackageManager pm = context.getPackageManager();
        ComponentName compName = new ComponentName(context.getApplicationContext(), ProfileChangeReceiver.class);
        pm.setComponentEnabledSetting(compName, newState, PackageManager.DONT_KILL_APP);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(LOG_TAG, "Profile change event");
        Pref.loadContextPreferences(context);
        ProfileWidget.doProfileChanged(context);
        showNotification(context);
    }

    private void showNotification(Context context) {
        if (Pref.profileNotify.isEmpty()) return;

        String currentProfile = getCurrentProfile(context);
        if (currentProfile == null) return;

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        nm.cancelAll();

        if (!currentProfile.equals(dontNotifyChangedProfile)) {
            if (Pref.profileNotify.contains(currentProfile)) {
                Integer icon = Pref.profileIcons.get(currentProfile);
                if (icon == null) icon = Pref.DEFAULT_ICON;

                Bitmap iconBitmap = new ProfileIconDrawable(context, icon, false).convertToBitmap(
                        context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_width),
                        context.getResources().getDimensionPixelSize(android.R.dimen.notification_large_icon_height));

                Notification notification = new Notification.Builder(context)
                        .setContentTitle(context.getString(R.string.notification_title, currentProfile))
                        .setSmallIcon(R.drawable.notification_icon)
                        .setLargeIcon(iconBitmap)
                        .setAutoCancel(true)
                        .build();

                nm.notify(NOTIFICATION_ID_PROFILE_CHANGED, notification);
            }
        }

        dontNotifyChangedProfile = null;
    }
}
