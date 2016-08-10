package com.xs.charge.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * @version V1.0 <描述当前版本功能>
 * @author: Xs
 * @date: 2016-06-08 09:35
 * @email Xs.lin@foxmail.com
 */
public class AppHelpFragment extends DialogFragment {
    private static final String TAG = "AppHelpFragment";
    private static final String EXTRA_MESSAGE = "message";
    private static final String EXTRA_BTN = "btn";

    public static AppHelpFragment getHelp(String message) {
        final AppHelpFragment af = new AppHelpFragment();
        final Bundle bundle = new Bundle();
        bundle.putString(EXTRA_MESSAGE,message);
        af.setArguments(bundle);
        return af;
    }
    public static AppHelpFragment getHelp(String message,String btn) {
        final AppHelpFragment af = new AppHelpFragment();
        final Bundle bundle = new Bundle();
        bundle.putString(EXTRA_MESSAGE,message);
        bundle.putString(EXTRA_BTN,btn);
        af.setArguments(bundle);
        return af;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Bundle bundle = getArguments();
        final String message =bundle.getString(EXTRA_MESSAGE);
        final String btn = bundle.getString(EXTRA_BTN);
        String btnStr = "ok";

        if (btn == null || "".equals(btn)){}
            else {
            btnStr = btn;
        }

        return new AlertDialog.Builder(getActivity())
                .setMessage(message)
                .setPositiveButton(btnStr,null)
                .show();
    }
}
