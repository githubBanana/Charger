package com.xs.charge.logger;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.xs.charge.R;
import com.xs.charge.utils.TimeUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @version V1.0 <描述当前版本功能>
 * @author: Xs
 * @date: 2016-06-11 03:17
 */
public class LogFragment extends Fragment implements PrintLog{
    private static final String TAG = "LogFragment";

    @Bind(R.id.tv_logger)   TextView    _tvLogger;
    @Bind(R.id.sv)          ScrollView  _sv;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_logger_layout,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this,view);
    }

    @Override
    public void i(String text) {
        _tvLogger.append(TimeUtil.read() + " -> " + text + "\n");
        getActivity().runOnUiThread(() -> _sv.fullScroll(ScrollView.FOCUS_DOWN));
    }

    @Override
    public void clear() {
        _tvLogger.setText(null);
    }

}
