package de.hftl_projekt.ict.ui;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import de.hftl_projekt.ict.R;

/**
 *
 * @author Carsten
 */
public class SettingsFragment extends PreferenceFragment {
    public static final String TAG = "SettingsFragment";


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //load the preferences from XML resource
        addPreferencesFromResource(R.xml.settings);
    }

}
