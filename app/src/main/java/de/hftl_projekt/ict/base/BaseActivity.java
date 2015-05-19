package de.hftl_projekt.ict.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

/**
 * basic Activity methods belong here
 * @author Carsten
 */
public abstract class BaseActivity extends AppCompatActivity {

    /**
     * set the contentView
     * inject and set Toolbar
     * @param savedInstanceState savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        ButterKnife.inject(this);
    }

    /**
     * reset Butterknife
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    /**
     * get the layout from all activities that extend BaseActivity
     * @return layout resource id
     */
    protected abstract int getLayoutResourceId();
}
