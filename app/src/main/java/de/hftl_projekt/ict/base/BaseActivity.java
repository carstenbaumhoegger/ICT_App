package de.hftl_projekt.ict.base;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import de.hftl_projekt.ict.R;

/**
 * basic Activity methods belong here
 * @author Carsten
 */
public abstract class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";

    @InjectView(R.id.toolbar) Toolbar mToolbar;

    /**
     * set the contentView
     * inject and set Toolbar
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResourceId());
        ButterKnife.inject(this);
        setSupportActionBar(mToolbar);
    }

    /**
     * reset Butterknife
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        ButterKnife.reset(this);
    }

    protected abstract int getLayoutResourceId();
}
