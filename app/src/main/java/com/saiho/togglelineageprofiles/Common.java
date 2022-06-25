package com.saiho.togglelineageprofiles;

import android.content.Context;
import android.util.Log;

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

    private static final boolean isCompatibleLineage = getCompatibleLineage();

    private static boolean getCompatibleLineage() {
        Log.i(LOG_TAG, "LineageOS version = " + Build.LINEAGEOS_VERSION + " (" + Build.LINEAGEOS_DISPLAY_VERSION + ")");
        Log.i(LOG_TAG, "LineageOS SDK level = " + Build.LINEAGE_VERSION.SDK_INT);
        return Build.LINEAGE_VERSION.SDK_INT >= Build.LINEAGE_VERSION_CODES.HACKBERRY;
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
        if (isCompatibleLineage) {
            ProfileManager pm = ProfileManager.getInstance(context);
            if (pm == null || !pm.isProfilesEnabled()) {
                checkSystemProfilesStatusMsg = R.string.msg_disabled_profiles;
                return false;
            } else {
                String[] profileNames = pm.getProfileNames();
                if (profileNames == null || profileNames.length == 0) {
                    checkSystemProfilesStatusMsg = R.string.msg_no_profiles;
                    return false;
                }
            }
            checkSystemProfilesStatusMsg = 0;
            return true;
        } else {
            checkSystemProfilesStatusMsg = R.string.msg_no_lineageos;
            return false;
        }

    }

    public static String[] getProfileNames(Context context) {
        if (isCompatibleLineage) {
            ProfileManager pm = ProfileManager.getInstance(context);
            if (pm != null && pm.isProfilesEnabled()) return pm.getProfileNames();
        }
        return new String[0];
    }

    public static String getCurrentProfile(Context context) {
        if (isCompatibleLineage) {
            ProfileManager pm = ProfileManager.getInstance(context);
            if (pm != null && pm.isProfilesEnabled()) {
                Profile profile = pm.getActiveProfile();
                if (profile != null) return profile.getName();
            }
        }
        return null;
    }

    public static void setCurrentProfile(Context context, String profileName) {
        if (isCompatibleLineage) {
            ProfileManager pm = ProfileManager.getInstance(context);
            if (pm != null && pm.isProfilesEnabled()) {
                ProfileChangeReceiver.dontNotifyChangedProfile = profileName;
                Profile[] profiles = pm.getProfiles();
                if (profiles != null) {
                    Optional<Profile> optProfile = Arrays.stream(profiles).filter(profile -> profileName.equals(profile.getName())).findFirst();
                    if (optProfile.isPresent()) {
                        pm.setActiveProfile(optProfile.get().getUuid());
                    }
                }
            }
        }
    }
}
