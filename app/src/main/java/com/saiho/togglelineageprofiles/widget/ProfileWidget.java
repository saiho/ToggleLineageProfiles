package com.saiho.togglelineageprofiles.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Handler;
import android.support.v4.graphics.ColorUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.RemoteViews;

import com.saiho.togglelineageprofiles.ProfileChangeReceiver;
import com.saiho.togglelineageprofiles.R;
import com.saiho.togglelineageprofiles.preferences.Pref;

import static com.saiho.togglelineageprofiles.Common.checkSystemProfilesStatus;
import static com.saiho.togglelineageprofiles.Common.getCurrentProfile;
import static com.saiho.togglelineageprofiles.Common.getProfileNames;
import static com.saiho.togglelineageprofiles.Common.setCurrentProfile;

public class ProfileWidget extends AppWidgetProvider {

    private static String delayedProfile = null; // The name of the profile that is pending to be activated
    private static Handler delayHandler = null;

    /**
     * Returns true if some widget of this class has been added to the home screen.
     *
     * @param context
     * @return
     */
    public static boolean isVisible(Context context) {
        // Use AppWidgetManager to get the number of widgets registered
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, ProfileWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        return appWidgetIds.length > 0;
    }

    /**
     * Update icon and label of the widget.
     * Even if multiple widgets exists in the home screen, all of them will show same information, so all are updated.
     *
     * @param context
     */
    private static void updateAllAppWidgets(Context context) {
        // Use AppWidgetManager to get the ids of the widgets registered and proceed to update them
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, ProfileWidget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
        if (appWidgetIds.length > 0) updateAppWidget(context, appWidgetManager, appWidgetIds);
    }

    private static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);

        String currentProfile = delayedProfile;
        if (delayedProfile == null) {
            currentProfile = getCurrentProfile(context);
        }

        // Set current label and icon
        if (currentProfile != null) {
            views.setTextViewText(R.id.widget_label, currentProfile);
            Integer icon = Pref.profileIcons.get(currentProfile);
            if (icon == null) icon = Pref.DEFAULT_ICON;
            views.setImageViewResource(R.id.widget_icon_image, icon);
        } else {
            views.setTextViewText(R.id.widget_label, context.getString(R.string.widget_no_active_profile));
            views.setImageViewIcon(R.id.widget_icon_image, null);
        }

        // Show / hide label
        if (Pref.textSize > 0) {
            views.setTextViewTextSize(R.id.widget_label, TypedValue.COMPLEX_UNIT_DIP, Pref.textSize);
            views.setViewVisibility(R.id.widget_label, View.VISIBLE);
        } else {
            views.setViewVisibility(R.id.widget_label, View.GONE);
        }

        // Reduce icon size if preferred
        if (Pref.smallIcon) {
            int smallIconPadding = (int) context.getResources().getDimension(R.dimen.small_icon_padding);
            views.setViewPadding(R.id.widget_icon_frame, smallIconPadding, smallIconPadding, smallIconPadding, smallIconPadding);
        } else {
            views.setViewPadding(R.id.widget_icon_frame, 0, 0, 0, 0);
        }

        // Set icon colors
        views.setInt(R.id.widget_icon_background, "setColorFilter", ColorUtils.setAlphaComponent(Pref.iconBackgroundColor, 255));
        views.setInt(R.id.widget_icon_background, "setImageAlpha", Color.alpha(Pref.iconBackgroundColor));
        views.setInt(R.id.widget_icon_image, "setColorFilter", ColorUtils.setAlphaComponent(Pref.iconForegroundColor, 255));
        views.setInt(R.id.widget_icon_image, "setImageAlpha", delayedProfile != null ? 128 : 255);

        // Click actions of the widget will be received by ClickReceiver
        Intent notifyIntent = new Intent(context, ClickReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

        // Refresh widgets on screen
        for (int appWidgetId : appWidgetIds) {
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    /**
     * This method is executed when a new profile has been activated in the system.
     *
     * @param context
     */
    public static void doProfileChanged(Context context) {
        delayedProfile = null;
        updateAllAppWidgets(context);
    }

    /**
     * This method is executed when the preferences of the app have changed.
     * It is expected that the new preferences are already loaded in Pref.*
     *
     * @param context
     */
    public static void doPreferencesChanged(Context context) {
        updateAllAppWidgets(context);
    }

    /**
     * Triggered when a new widget is going to be added to the home screen and when phone boots an all widgets are about to be shown.
     *
     * @param context
     * @param appWidgetManager
     * @param appWidgetIds
     */
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        Pref.loadContextPreferences(context);
        ProfileChangeReceiver.refreshManifestState(context);
        updateAppWidget(context, appWidgetManager, appWidgetIds);
    }

    /**
     * Triggered when all widgets have been removed from the home screen.
     *
     * @param context
     */
    @Override
    public void onDisabled(Context context) {
        Pref.loadContextPreferences(context);
        ProfileChangeReceiver.refreshManifestState(context);
        super.onDisabled(context);
    }

    /**
     * Process click actions done by the user.
     */
    public static class ClickReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Pref.loadContextPreferences(context);

            if (!checkSystemProfilesStatus(context)) {
                updateAllAppWidgets(context); // Be sure that the "No active profile" icon is shown
                return;
            }

            // If quickToggle mode is on, look for the next profile and postpone its activation a few seconds
            if (Pref.quickToggle) {
                // If some toggle action was pending, cancel it
                if (delayHandler == null) {
                    delayHandler = new Handler();
                } else {
                    delayHandler.removeCallbacksAndMessages(null);
                }

                // Find next profile to activate in the list of existing profiles
                String[] profileNames = getProfileNames(context);
                if (profileNames.length > 0) {

                    String currentProfile = delayedProfile;
                    if (delayedProfile == null) {
                        currentProfile = getCurrentProfile(context);
                    }

                    int next = 0;
                    if (currentProfile != null) {
                        while (next < profileNames.length) {
                            if (currentProfile.equals(profileNames[next])) break;
                            next++;
                        }
                        if (++next >= profileNames.length) next = 0;
                    }
                    delayedProfile = profileNames[next];
                    updateAllAppWidgets(context);

                    // Wait some seconds to effectively change the profile
                    delayHandler.postDelayed(new ToggleRunnable(context), context.getResources().getInteger(R.integer.quick_toggle_delay_ms));
                }
            } else {
                // If quickToggle is off, show a popup to allow the user select the new profile
                Intent popupIntent = new Intent(context, ProfileWidgetPopup.class);
                popupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                popupIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                popupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(popupIntent);
            }
        }
    }

    /**
     * Toggle action is delayed some seconds after clicking to allow the user to cycle the different profiles without actually
     * changing anything. Change is done once user stops clicking.
     */
    static class ToggleRunnable implements Runnable {
        private final Context context;

        public ToggleRunnable(Context context) {
            this.context = context;
        }

        @Override
        public void run() {
            setCurrentProfile(context, delayedProfile);
        }
    }
}
