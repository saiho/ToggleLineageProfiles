package com.saiho.togglelineageprofiles.api;

import android.app.Activity;
import android.os.RemoteException;

import static com.saiho.togglelineageprofiles.Common.setActiveProfile;

public class SetProfile extends Activity {

    public static final String PROFILE_NAME = "com.saiho.togglelineageprofiles.api.profileName";

    protected void onStart() {
        super.onStart();
        try {
            performTask();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void performTask() throws RemoteException {
        String profileName = getIntent().getStringExtra( PROFILE_NAME );
        setActiveProfile( getApplicationContext(), profileName );
        finish();
    }
}
