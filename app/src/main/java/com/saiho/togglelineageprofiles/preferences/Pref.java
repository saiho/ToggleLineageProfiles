package com.saiho.togglelineageprofiles.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;

import com.saiho.togglelineageprofiles.ProfileIconList;
import com.saiho.togglelineageprofiles.R;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

import static com.saiho.togglelineageprofiles.Common.LOG_TAG;
import static com.saiho.togglelineageprofiles.Common.getProfileNames;

/**
 * This class helps to parse the preferences stored by the app.
 * The preferences are conveniently saved into global variables for easier access from the rest of the code.
 * <p>
 * Global variables should not be modified externally. Access should be read only, but getters are not provided to get
 * code shorter and faster.
 */
public final class Pref {
    static final String STORAGE_NAME = "ToggleLineageProfilesPreferences";
    static final String KEY_CATEGORY_PROFILE_ICONS = "category_profile_icons";
    static final String KEY_CATEGORY_PROFILE_NOTIFICATIONS = "category_profile_notifications";
    static final String KEY_PREFIX_ICON = "icon_";
    static final String KEY_PREFIX_NOTIFY = "notify_";
    static final String KEY_FOREGROUND_COLOR = "foreground_color";
    static final String KEY_BACKGROUND_COLOR = "background_color";
    static final String KEY_QUICK_TOGGLE = "quick_toggle";
    static final String KEY_TEXT_SIZE = "text_size";
    static final String KEY_SMALL_ICON = "small_icon";

    public static final int DEFAULT_ICON = R.drawable.profile_icon_swatches;
    static final boolean DEFAULT_NOTIFY = false;
    static final boolean DEFAULT_QUICK_TOGGLE = false;
    static final float DEFAULT_TEXT_SIZE = 14f;
    static final boolean DEFAULT_SMALL_ICON = false;

    public static final HashMap<String, Integer> profileIcons = new HashMap<>();
    public static final ArrayList<String> profileNotify = new ArrayList<>();
    public static int iconBackgroundColor = Color.BLACK;
    public static int iconForegroundColor = Color.WHITE;
    public static boolean quickToggle = DEFAULT_QUICK_TOGGLE;
    public static float textSize = DEFAULT_TEXT_SIZE;
    public static boolean smallIcon = DEFAULT_SMALL_ICON;

    private static boolean loaded = false;

    /**
     * Reloads always the stored preferences.
     *
     * @param preferences
     * @param context
     */
    static void refreshPreferences(SharedPreferences preferences, Context context) {
        loaded = true;

        profileIcons.clear();
        profileNotify.clear();

        String[] profileNames = getProfileNames(context);
        for (String name : profileNames) {

            Integer iconId = null;
            String iconName = preferences.getString(buildProfilePrefKey(KEY_PREFIX_ICON, name), null);
            if (iconName != null) iconId = ProfileIconList.ids.get(iconName);
            if (iconId == null) iconId = DEFAULT_ICON;
            profileIcons.put(name, iconId);

            if (preferences.getBoolean(buildProfilePrefKey(KEY_PREFIX_NOTIFY, name), DEFAULT_NOTIFY)) {
                profileNotify.add(name);
            }
        }

        iconForegroundColor = preferences.getInt(KEY_FOREGROUND_COLOR, context.getColor(R.color.default_icon_foreground));
        iconBackgroundColor = preferences.getInt(KEY_BACKGROUND_COLOR, context.getColor(R.color.default_icon_background));

        quickToggle = preferences.getBoolean(KEY_QUICK_TOGGLE, DEFAULT_QUICK_TOGGLE);
        textSize = Float.parseFloat(preferences.getString(KEY_TEXT_SIZE, Float.toString(DEFAULT_TEXT_SIZE)));
        smallIcon = preferences.getBoolean(KEY_SMALL_ICON, DEFAULT_SMALL_ICON);
    }

    /**
     * Loads the stored preferences if they haven´t loaded before.
     *
     * @param context
     */
    public static void loadContextPreferences(Context context) {
        if (!loaded) {
            SharedPreferences preferences = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
            refreshPreferences(preferences, context);
        }
    }

    /**
     * Generate a unique key id using the profile name. Actually the profile name itself could be used as key, but as
     * the profileName is a user entered value, to avoid any risk I prefer using the MD5 value which will never contain
     * special characters.
     *
     * @param prefix
     * @param profileName
     * @return
     */
    static String buildProfilePrefKey(String prefix, String profileName) {
        String hash;
        try {
            hash = new BigInteger(1, MessageDigest.getInstance("MD5").digest(profileName.getBytes(StandardCharsets.UTF_8))).toString(32);
        } catch (NoSuchAlgorithmException e) {
            Log.e(LOG_TAG, "No MD5", e);
            hash = Integer.toHexString(profileName.hashCode());
        }
        return prefix + hash;
    }
}
