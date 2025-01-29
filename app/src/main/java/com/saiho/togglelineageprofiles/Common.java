package com.saiho.togglelineageprofiles;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.StringRes;

import java.util.Arrays;
import java.util.Optional;

import lineageos.app.Profile;
import lineageos.app.ProfileManager;
import lineageos.os.Build;


public final class Common {
    public static final String LOG_TAG = "LogToggleLineageProf";

    @StringRes
    public static int checkSystemProfilesStatusMsg = 0;

    private static final boolean isCompatibleLineage = checkCompatibleLineage();

    private static boolean checkCompatibleLineage() {
        Log.i(LOG_TAG, "LineageOS version = " + Build.LINEAGEOS_VERSION + " (" + Build.LINEAGEOS_DISPLAY_VERSION + ")");
        Log.i(LOG_TAG, "LineageOS SDK level = " + Build.LINEAGE_VERSION.SDK_INT);
        return Build.LINEAGE_VERSION.SDK_INT >= Build.LINEAGE_VERSION_CODES.HACKBERRY ||
                // Lineage 22.1 incorrectly reports SDK_INT as 0, so now the check is softened
                (Build.LINEAGEOS_VERSION != null && !Build.LINEAGEOS_VERSION.isBlank());
    }

    /**
     * Check if LineageOS is present, the system profiles are enabled and at least one profile is defined.
     * <p>
     * If there is some problem, the string id of a descriptive error is set in the variable checkSystemProfilesStatusMsg. If no error, the
     * variable checkSystemProfilesStatusMsg is set to zero.
     *
     * @param context The context
     * @return true if all the checks are correct.
     */
    public static boolean checkSystemProfilesStatus(Context context) {
        if (!isCompatibleLineage) {
            checkSystemProfilesStatusMsg = R.string.msg_no_lineageos;
            return false;
        }
        try {
            ProfileManager pm = ProfileManager.getInstance(context);
            if (pm == null || !pm.isProfilesEnabled()) {
                checkSystemProfilesStatusMsg = R.string.msg_disabled_profiles;
                return false;
            }
            String[] profileNames = pm.getProfileNames();
            if (profileNames == null || profileNames.length == 0) {
                checkSystemProfilesStatusMsg = R.string.msg_no_profiles;
                return false;
            }
            checkSystemProfilesStatusMsg = 0;
            return true;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error getting the list of profiles", e);
            Toast.makeText(context, context.getString(R.string.error_get_list_profiles, errorMessage(e)), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    public static String[] getProfileNames(Context context) {
        if (isCompatibleLineage) {
            try {
                ProfileManager pm = ProfileManager.getInstance(context);
                if (pm != null && pm.isProfilesEnabled()) return pm.getProfileNames();
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error getting the list of profiles", e);
                Toast.makeText(context, context.getString(R.string.error_get_list_profiles, errorMessage(e)), Toast.LENGTH_LONG).show();
            }
        }
        return new String[0];
    }

    public static String getCurrentProfile(Context context) {
        if (isCompatibleLineage) {
            try {
                ProfileManager pm = ProfileManager.getInstance(context);
                if (pm != null && pm.isProfilesEnabled()) {
                    Profile profile = pm.getActiveProfile();
                    if (profile != null) return profile.getName();
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error getting the active profile", e);
                Toast.makeText(context, context.getString(R.string.error_get_active_profile, errorMessage(e)), Toast.LENGTH_LONG).show();
            }
        }
        return null;
    }

    public static void setActiveProfile(Context context, String profileName) {
        if (isCompatibleLineage) {
            try {
                ProfileManager pm = ProfileManager.getInstance(context);
                if (pm != null && pm.isProfilesEnabled()) {
                    ProfileChangeReceiver.dontNotifyChangedProfile = profileName;
                    Profile[] profiles = pm.getProfiles();
                    if (profiles != null) {
                        Optional<Profile> optProfile = Arrays.stream(profiles).filter(profile -> profileName.equals(profile.getName())).findFirst();
                        optProfile.ifPresent(profile -> pm.setActiveProfile(profile.getUuid()));
                    }
                }
            } catch (Exception e) {
                Log.e(LOG_TAG, "Error activating profile " + profileName, e);
                Toast.makeText(context, context.getString(R.string.error_set_active_profile, profileName, errorMessage(e)), Toast.LENGTH_LONG).show();
            }
        }
    }

    public static String errorMessage(Exception e) {
        if (e instanceof NullPointerException) {
            StackTraceElement[] stackTrace = e.getStackTrace();
            if (stackTrace.length > 0)
                return e.getMessage() + " at " + stackTrace[0].toString();
        }
        return e.getMessage();
    }
}
