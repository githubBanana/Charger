package com.xs.charge.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.xs.charge.R;
import com.xs.charge.dialog.LoadingFragment;

/**
 * @version V1.0 <描述当前版本功能>
 * @author: Xs
 * @date: 2016-06-08 09:05
 * @email Xs.lin@foxmail.com
 */
public abstract class ToolBarActivity extends AppCompatActivity{
    private static final String TAG = "ToolBarActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentView());
        Toolbar _toobar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(_toobar);
        initView();
    }

    public void setDisplayHomeAsUpEnable(boolean enable) {
        getSupportActionBar().setDisplayHomeAsUpEnabled(enable);
    }
    public void setHomeAsUpIndicator(int resid) {
        getSupportActionBar().setHomeAsUpIndicator(resid);
    }
    public void setTitle(int title) {
        getSupportActionBar().setTitle(getString(title));
    }

    protected abstract int getContentView();
    protected abstract void initView();

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showProgress(String message) {
        runOnUiThread(() -> LoadingFragment.getLoad(message).show(getSupportFragmentManager(),"loading"));
    }
    public void dismissProgress() {
        runOnUiThread(() -> {
            Fragment prev = getSupportFragmentManager().findFragmentByTag("loading");
            if (prev != null) {
                DialogFragment df = (DialogFragment) prev;
                df.dismiss();
            }
        });
    }
}
