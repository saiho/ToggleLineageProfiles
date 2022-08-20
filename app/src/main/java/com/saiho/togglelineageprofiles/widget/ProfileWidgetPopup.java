package com.saiho.togglelineageprofiles.widget;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.saiho.togglelineageprofiles.R;

import static com.saiho.togglelineageprofiles.Common.getCurrentProfile;
import static com.saiho.togglelineageprofiles.Common.getProfileNames;
import static com.saiho.togglelineageprofiles.Common.setActiveProfile;

/**
 * Shows a popup with the list of existing profiles. When the user clicks one of them, it is activated.
 */
public class ProfileWidgetPopup extends Activity implements RadioGroup.OnCheckedChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.widget_popup);
        setShowWhenLocked(true);
        getWindow().setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);

        RadioGroup radioGroup = findViewById(R.id.widget_popup_radiogroup);

        String currentProfile = getCurrentProfile(this);
        String[] profileNames = getProfileNames(this);
        for (String name : profileNames) {
            RadioButton radioButton = new RadioButton(this);
            radioButton.setText(name);
            radioButton.setChecked(name.equals(currentProfile));
            radioGroup.addView(radioButton);
        }

        radioGroup.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        RadioButton radioButton = group.findViewById(checkedId);
        String name = radioButton.getText().toString();
        setActiveProfile(this, name);
        finish();
    }

    public static Intent generateIntent(Context context)
    {
        Intent popupIntent = new Intent(context, ProfileWidgetPopup.class);
        popupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        popupIntent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        popupIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        popupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return popupIntent;
    }

}
