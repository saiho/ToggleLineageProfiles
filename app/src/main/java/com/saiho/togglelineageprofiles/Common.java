package com.saiho.togglelineageprofiles;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.util.MutableInt;

import java.lang.reflect.Method;


public final class Common {
    public static final String LOG_TAG = "LogToggleLineageProf";

    private enum APIType {
        NONE,
        CYANOGENMOD_14, // Cyanogenmod 13 or Lineage 14
        LINEAGEOS_15 // Lineage 15 or higher
    }

    private static final APIType currentAPIType = getCurrentAPIType();

    @SuppressLint({"PrivateApi", "ObsoleteSdkInt"})
    private static APIType getCurrentAPIType() {
        try {
            // It is not possible to get lineageos.os.Build.LINEAGE_VERSION.SDK_INT because when the
            // LINEAGE_VERSION class is initialized it causes "java.lang.IllegalArgumentException: key.length > 31"
            // in Android previous to Oreo, due to an internal call to SystemProperties.getInt using a
            // very long name. That exception cannot be controlled.
            // So we get the property manually controlling the exceptions properly.
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Method getInt = systemProperties.getMethod("getInt", String.class, int.class);
            int sdk_int = (Integer) getInt.invoke(null, "ro.lineage.build.version.plat.sdk", 0);
            Log.i(LOG_TAG, "LineageOS SDK level = " + sdk_int);
            if (sdk_int > 0) return APIType.LINEAGEOS_15;
        } catch (Exception e) {
            Log.d(LOG_TAG, "No LineageOS 15 since there was an error getting the SDK level.", e);
        }

        Log.i(LOG_TAG, "CyanogenMod SDK level = " + cyanogenmod.os.Build.CM_VERSION.SDK_INT);
        if (cyanogenmod.os.Build.CM_VERSION.SDK_INT > 0) return APIType.CYANOGENMOD_14;

        return APIType.NONE;
    }

    /**
     * Check if LineageOS is present, the system profiles are enabled and at least one profile is defined.
     *
     * @param context      The context
     * @param statusMsgRef Returns the resource id of an error message if there is some problem. Otherwise the
     *                     returning value is set to zero.
     * @return true if all the checks are correct.
     */
    public static boolean checkSystemProfilesStatus(Context context, MutableInt statusMsgRef) {
        switch (currentAPIType) {
            case LINEAGEOS_15: {
                lineageos.app.ProfileManager pm = lineageos.app.ProfileManager.getInstance(context);
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
                break;
            }
            case CYANOGENMOD_14: {
                cyanogenmod.app.ProfileManager pm = cyanogenmod.app.ProfileManager.getInstance(context);
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
                break;
            }
            default: {
                if (statusMsgRef != null) statusMsgRef.value = R.string.msg_no_lineageos;
                return false;
            }
        }

        if (statusMsgRef != null) statusMsgRef.value = 0;
        return true;
    }

    public static String[] getProfileNames(Context context) {
        switch (currentAPIType) {
            case LINEAGEOS_15: {
                lineageos.app.ProfileManager pm = lineageos.app.ProfileManager.getInstance(context);
                if (pm != null && pm.isProfilesEnabled()) return pm.getProfileNames();
                break;
            }
            case CYANOGENMOD_14: {
                cyanogenmod.app.ProfileManager pm = cyanogenmod.app.ProfileManager.getInstance(context);
                if (pm != null && pm.isProfilesEnabled()) return pm.getProfileNames();
                break;
            }
        }
        return new String[0];
    }

    public static String getCurrentProfile(Context context) {
        switch (currentAPIType) {
            case LINEAGEOS_15: {
                lineageos.app.ProfileManager pm = lineageos.app.ProfileManager.getInstance(context);
                if (pm != null && pm.isProfilesEnabled()) {
                    lineageos.app.Profile profile = pm.getActiveProfile();
                    if (profile != null) return profile.getName();
                }
                break;
            }
            case CYANOGENMOD_14: {
                cyanogenmod.app.ProfileManager pm = cyanogenmod.app.ProfileManager.getInstance(context);
                if (pm != null && pm.isProfilesEnabled()) {
                    cyanogenmod.app.Profile profile = pm.getActiveProfile();
                    if (profile != null) return profile.getName();
                }
                break;
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static void setCurrentProfile(Context context, String profileName) {
        switch (currentAPIType) {
            case LINEAGEOS_15: {
                lineageos.app.ProfileManager pm = lineageos.app.ProfileManager.getInstance(context);
                if (pm != null && pm.isProfilesEnabled()) {
                    ProfileChangeReceiver.dontNotifyChangedProfile = profileName;
                    pm.setActiveProfile(profileName);
                }
                break;
            }
            case CYANOGENMOD_14: {
                cyanogenmod.app.ProfileManager pm = cyanogenmod.app.ProfileManager.getInstance(context);
                if (pm != null && pm.isProfilesEnabled()) {
                    ProfileChangeReceiver.dontNotifyChangedProfile = profileName;
                    pm.setActiveProfile(profileName);
                }
                break;
            }
        }
    }
}
