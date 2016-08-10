package com.xs.charge.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.diy.blelib.bag.ByteUtil;
import com.xs.charge.R;

/**
 * @version V1.0 <描述当前版本功能>
 * @author: Xs
 * @date: 2016-06-11 05:30
 */
public class InputFragment extends DialogFragment implements TextWatcher{
    private static final String TAG = "InputFragment";

    private TextView _tvJinzhi;
    private String hex;
    private boolean canSend = false;
    private static OnclickSend onclickSend;
    public static InputFragment getInput(Context context) {
        final InputFragment ifm = new InputFragment();
        onclickSend = (OnclickSend) context;
        return ifm;
    }
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        final View _view = getActivity().getLayoutInflater().inflate(R.layout.dialog_input_layout,null);
        final EditText _input = (EditText) _view.findViewById(R.id.et_input);
        _input.addTextChangedListener(this);

        _tvJinzhi = (TextView) _view.findViewById(R.id.tv_jinzhi);

        android.support.v7.app.AlertDialog alertDialog =  new android.support.v7.app.AlertDialog.Builder(getActivity())
                .setView(_view)
                .setPositiveButton("发送", (dialog, which) -> {
                    if (canSend)
                        onclickSend.send(hex);
                    else
                        onclickSend.dataError();
                })
                .setTitle("测试数据交互")
                .show();
        return alertDialog;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        String str = s.toString();
        if (str == null || "".equals(str)) {
            canSend = false;
            return;
        }
        else {
            if (Integer.valueOf(str) > 255 ) {
                _tvJinzhi.setText("异常");
                canSend = false;
                return;
            }
            if ( Integer.valueOf(str) > 100 || Integer.valueOf(str) < 0) {
                _tvJinzhi.setText("超出规定范围");
                canSend = false;
                return;
            }
            canSend = true;
            String hexString = Integer.toHexString(Integer.valueOf(s.toString()));
            hex = s.toString();
            final byte[] charge = ByteUtil.hexStringToBytes(hexString);
            _tvJinzhi.setText("0x"+hexString);
            Log.e(TAG, "onTextChanged: " + s + "  " + hexString
            + "   charge:"+ charge + "  "+ByteUtil.bytesToHexString(ByteUtil.hexStringToBytes(hexString)));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    public interface OnclickSend{
        void send(String hex);
        void dataError();
    }

}
