package de.hftl_projekt.ict;

import android.os.Bundle;

import de.hftl_projekt.ict.base.BaseActivity;
import de.hftl_projekt.ict.ui.SettingsFragment;

/**
 * handles the SettingsFragment
 * @author Carsten
 */
public class SettingsActivity extends BaseActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //add the SettingsFragment to layout
        getFragmentManager().beginTransaction().replace(R.id.fragment_container,
                new SettingsFragment()).commit();
    }

    @Override
    protected int getLayoutResourceId() {
        return R.layout.activity_settings;
    }
}
