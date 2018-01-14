package com.saiho.togglelineageprofiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.MutableInt;

import cyanogenmod.app.Profile;
import cyanogenmod.app.ProfileManager;
import cyanogenmod.os.Build;

public final class Common {
    public static final String LOG_TAG = "LogToggleLineageProf";

    /**
     * Check if LineageOS is present, the system profiles are enabled and at least one profile is defined.
     *
     * @param context
     * @param statusMsgRef Returns the resource id of an error message if there is some problem. Otherwise the
     *                     returning value is set to zero.
     * @return true if all the checks are correct.
     */
    @SuppressLint("ObsoleteSdkInt")
    public static boolean checkSystemProfilesStatus(Context context, MutableInt statusMsgRef) {
        if (Build.CM_VERSION.SDK_INT <= 0) {
            if (statusMsgRef != null) statusMsgRef.value = R.string.msg_no_lineageos;
            return false;
        } else {
            ProfileManager pm = ProfileManager.getInstance(context);
            if (pm == null || !pm.isProfilesEnabled()) {
                if (statusMsgRef != null) statusMsgRef.value = R.string.msg_disabled_profiles;
                return false;
            } else {
                String[] profileNames = pm.getProfileNames();
                if (profileNames == null || profileNames.length == 0) {
                    if (statusMsgRef != null) statusMsgRef.value = R.string.msg_no_profiles;
                    return false;
                }
            }
        }
        if (statusMsgRef != null) statusMsgRef.value = 0;
        return true;
    }

    @SuppressLint("ObsoleteSdkInt")
    private static ProfileManager getProfileManagerIfEnabled(Context context) {
        if (Build.CM_VERSION.SDK_INT > 0) {
            ProfileManager pm = ProfileManager.getInstance(context);
            if (pm.isProfilesEnabled()) return pm;
        }
        return null;
    }

    public static String[] getProfileNames(Context context) {
        ProfileManager pm = getProfileManagerIfEnabled(context);
        if (pm != null) {
            return pm.getProfileNames();
        }
        return new String[0];
    }

    public static String getCurrentProfile(Context context) {
        ProfileManager pm = getProfileManagerIfEnabled(context);
        if (pm != null) {
            Profile profile = pm.getActiveProfile();
            if (profile != null) {
                return profile.getName();
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static void setCurrentProfile(Context context, String profileName) {
        ProfileManager pm = getProfileManagerIfEnabled(context);
        if (pm != null) {
            ProfileChangeReceiver.dontNotifyChangedProfile = profileName;
            pm.setActiveProfile(profileName);
        }
    }
}
