package com.saiho.togglelineageprofiles.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.util.MutableInt;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.saiho.togglelineageprofiles.ProfileChangeReceiver;
import com.saiho.togglelineageprofiles.R;
import com.saiho.togglelineageprofiles.widget.ProfileWidget;

import static com.saiho.togglelineageprofiles.Common.checkSystemProfilesStatus;
import static com.saiho.togglelineageprofiles.Common.getProfileNames;

public class SettingsActivity extends PreferenceActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if the system profiles are present and enabled
        MutableInt profileStatusMsgRef = new MutableInt(0);
        if (!checkSystemProfilesStatus(this, profileStatusMsgRef)) {
            // Show returned error message
            setContentView(R.layout.no_profiles);
            TextView textView = findViewById(R.id.no_profiles_text);
            textView.setText(profileStatusMsgRef.value);
            return;
        }

        // If a previous state is restored there is no need to create the SettingsFragment, because it is automatically created in the super.onCreate.
        // This makes a difference for non persisted preferences. Creating them again will put the default value instead of using the value that was restored.
        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFragment()).commit();
        }
    }

    public static class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            getPreferenceManager().setSharedPreferencesName(Pref.STORAGE_NAME);
            addPreferencesFromResource(R.xml.preferences);

            PreferenceCategory iconsCategory = (PreferenceCategory) getPreferenceScreen().findPreference(Pref.KEY_CATEGORY_PROFILE_ICONS);
            PreferenceCategory notificationsCategory = (PreferenceCategory) getPreferenceScreen().findPreference(Pref.KEY_CATEGORY_PROFILE_NOTIFICATIONS);
            Context context = getActivity();

            // Add preferences for each profile
            String[] profileNames = getProfileNames(context);
            for (String name : profileNames) {
                SelectIconPreference iconPref = new SelectIconPreference(context);
                iconPref.setKey(Pref.buildProfilePrefKey(Pref.KEY_PREFIX_ICON, name));
                iconPref.setTitle(getString(R.string.pref_select_icon_title, name));
                iconsCategory.addPreference(iconPref);

                Preference notifyPref = new CheckBoxPreference(context);
                notifyPref.setDefaultValue(Pref.DEFAULT_NOTIFY);
                notifyPref.setKey(Pref.buildProfilePrefKey(Pref.KEY_PREFIX_NOTIFY, name));
                notifyPref.setTitle(getString(R.string.pref_notify_title, name));
                notificationsCategory.addPreference(notifyPref);
            }

            // Is necessary to load the stored preferences to get the colors of the icons that will be visualized in the preference screen
            Pref.loadContextPreferences(getContext());
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            // Set the Pref.* global variables used in the whole app
            Pref.refreshPreferences(sharedPreferences, getActivity());

            // Force redrawing the icons in case that the color preferences have changed
            ListAdapter root = getPreferenceScreen().getRootAdapter();
            if (root instanceof BaseAdapter) {
                ((BaseAdapter) root).notifyDataSetChanged();
            }

            // Run methods that depend of preference changes
            ProfileChangeReceiver.refreshManifestState(getActivity());
            ProfileWidget.doPreferencesChanged(getActivity());
        }
    }
}