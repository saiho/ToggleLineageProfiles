package com.saiho.togglelineageprofiles.preferences;

import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;

/**
 * Helper class for custom preferences.
 */
class SimpleSavedState extends Preference.BaseSavedState {

    // Standard creator object using an instance of this class.
    // It is accessed by the android framework using reflection.
    public static final Parcelable.Creator<SimpleSavedState> CREATOR =
            new Parcelable.Creator<>() {
                public SimpleSavedState createFromParcel(Parcel in) {
                    return new SimpleSavedState(in);
                }

                public SimpleSavedState[] newArray(int size) {
                    return new SimpleSavedState[size];
                }
            };

    // Value that is persisted
    private Object value;

    public Object getValue() {
        return value;
    }

    public SimpleSavedState(Parcelable superState, Object value) {
        super(superState);
        this.value = value;
    }

    public SimpleSavedState(Parcel source) {
        super(source);
        // Get the current preference's value
        value = source.readValue(value.getClass().getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        // Write the preference's value
        dest.writeValue(value);
    }
}
