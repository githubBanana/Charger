package com.xs.charge.activity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.diy.blelib.ble.ChargeService;
import com.diy.blelib.profile.BleProfileService;
import com.diy.blelib.scanner.ScannerFragment;
import com.diy.blelib.utils.ChargeConfig;
import com.diy.blelib.utils.SharePreferUtil;
import com.jakewharton.rxbinding.view.RxView;
import com.xs.charge.event.NotifyEvent;
import com.xs.charge.R;
import com.xs.charge.dialog.AppHelpFragment;
import com.xs.charge.dialog.InputFragment;
import com.xs.charge.logger.LogFragment;
import com.xs.charge.services.BatteryLocalService;
import com.xs.charge.utils.AnimatorUtil;
import com.xs.charge.utils.SortUtil;
import com.xs.widgetlib.ww.WaterWaveView;

import butterknife.Bind;
import butterknife.ButterKnife;
import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public class MainActivity extends ToolBarActivity
implements ScannerFragment.OnDeviceSelectedListener,InputFragment.OnclickSend{
    private final String TAG = MainActivity.class.getSimpleName();

//    @Bind(R.id.tv_charge_progress)      TextView        _chargeShow;
    @Bind(R.id.waterwave)               WaterWaveView   _waterWave;
    @Bind(R.id.tv_show_other_info)      TextView        _otherInfoShow;
    @Bind(R.id.snackbar_need)           LinearLayout    _ll;
    @Bind(R.id.cv)                      CardView        _cv;
    @Bind(R.id.btn_connect)             AppCompatButton _connect;
    @Bind(R.id.fl_fm)                   FrameLayout     _flFm;
    @Bind(R.id.btn_send)                AppCompatButton _send;

    private                             LogFragment     _fmLog;
    private                             BluetoothAdapter _BtAdapter;
    private                             String           bleAddress = null;

    @Override
    protected int getContentView() {
        return R.layout.activity_main;
    }

    @Override
    protected void initView() {
        EventBus.getDefault().register(this);
        ButterKnife.bind(this);
        setDisplayHomeAsUpEnable(true);
        setHomeAsUpIndicator(android.R.drawable.ic_lock_idle_low_battery);

        _waterWave.setMax(100);
        //监听设置
//        RxView.clicks(_chargeShow).subscribe(aVoid -> AnimatorUtil.scaling(_chargeShow));
        RxView.clicks(_waterWave).subscribe(aVoid -> AnimatorUtil.scaling(_waterWave));
        RxView.clicks(_cv).subscribe(aVoid -> AnimatorUtil.scaling(_cv));
        RxView.clicks(_connect).subscribe(aVoid -> showScannerFragment());
        RxView.clicks(_send).subscribe(aVoid -> InputFragment.getInput(MainActivity.this).show(getSupportFragmentManager(),"input"));

        startService();
        _fmLog = (LogFragment) getSupportFragmentManager().findFragmentById(R.id.fm_log);
        _fmLog.i("Log:");

        //注册监听ble广播
        LocalBroadcastManager.getInstance(this).registerReceiver(_receiver,getIntentFilter());

        setSupportBle();
//        autoConnBle();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setTitle(R.string.test_mode);
        setSupportBle();
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopScan();
    }

    private void setSupportBle() {
        final BluetoothManager manager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        _BtAdapter = manager.getAdapter();
        if (!_BtAdapter.isEnabled())
            _BtAdapter.enable();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clear_log) {
            _fmLog.clear();
            return true;
        }
        if (item.getItemId() == R.id.help) {
            AppHelpFragment.getHelp(getString(R.string.app_help_info)).show(getSupportFragmentManager(),"help");
            return true;
        }
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.mode) {
            ChargeConfig.mode = !ChargeConfig.mode;
            if (ChargeConfig.mode) {
                setTitle(R.string.normal_mode);
                _send.setVisibility(View.GONE);
            }
            else {
                setTitle(R.string.test_mode);
                _send.setVisibility(View.VISIBLE);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private ServiceConnection _connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(final ComponentName name, final IBinder service) {
            final BleProfileService.LocalBinder bleService =mService = (BleProfileService.LocalBinder) service;
            // mDeviceName = bleService.getDeviceName();
            // and notify user if device is connected
            if (bleService.isConnected());
            //  notifyConnected();
        }

        @Override
        public void onServiceDisconnected(final ComponentName name) {
            mService = null;
        }
    };

    private ChargeService.LocalBinder mService;
    private void startService() {
        final Intent service = new Intent(this, BatteryLocalService.class);
        startService(service);
    }
    private void closeService() {
        Intent intent = new Intent(this,BatteryLocalService.class);
        stopService(intent);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        closeService();
        closeBleService();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        startActivity(intent);
    }

    @Subscribe
    public void onEvent(NotifyEvent event) {
        Log.e(TAG, "onEvent: "+event.toString());
        runOnUiThread(() -> {
//            _chargeShow.setText(String.valueOf(event.getLevel())+"%");
            _otherInfoShow.setText(SortUtil.doBatteryInfo(event,this));
//            AnimatorUtil.scaling(_chargeShow);
            AnimatorUtil.scaling(_cv);
            _waterWave.setProgressSync(event.getLevel());
            AnimatorUtil.scaling(_waterWave);

        });
    }

    @Override
    public void onDeviceSelected(BluetoothDevice device, String name) {
        startBleService(device.getAddress());
    }

    @Override
    public void onDialogCanceled() {

    }

    public void startBleService(String address) {
        final Intent service = new Intent(this, ChargeService.class);
        service.putExtra(BleProfileService.EXTRA_DEVICE_ADDRESS, address);
        startService(service);
        bindService(service, _connection, 0);
    }
    public void closeBleService() {
        if(mService != null) {
            Intent intent = new Intent(this,ChargeService.class);
            unbindService(_connection);
            stopService(intent);
            mService = null;
        }
    }
    @Override
    public void send(String hex) {
        if (mService != null && mService.isConnected()) {
            EventBus.getDefault().post(hex);
            Snackbar.make(_ll, "" + hex, Snackbar.LENGTH_SHORT).show();
        }
        else
            Snackbar.make(_ll,"请连接蓝牙",Snackbar.LENGTH_SHORT).show();

    }

    @Override
    public void dataError() {
        Snackbar.make(_ll,"data error",Snackbar.LENGTH_SHORT).show();
    }

    private BroadcastReceiver _receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BleProfileService.BROADCAST_CONNECTION_STATE.equals(action)) {
                final int state = intent.getIntExtra(BleProfileService.EXTRA_CONNECTION_STATE, BleProfileService.STATE_DISCONNECTED);
                switch (state) {
                    case BleProfileService.STATE_CONNECTED:
                        notifyUser(true);
                        showLog("ble 连接成功!");
                        String address = intent.getStringExtra(BleProfileService.EXTRA_DEVICE_ADDRESS);
                        SharePreferUtil.putString(MainActivity.this,"address",address);
                        break;
                    case BleProfileService.STATE_DISCONNECTED:
                        notifyUser(false);
                        closeBleService();
                        showLog("ble 断连~~");
                        break;
                }
            } else if(BleProfileService.BROADCAST_ERROR.equals(action)) {
                Log.e(TAG, "onReceive EXTRA_ERROR_CODE: "+intent.getStringExtra(BleProfileService.EXTRA_ERROR_CODE) );
                Log.e(TAG, "onReceive EXTRA_ERROR_MESSAGE: "+intent.getStringExtra(BleProfileService.EXTRA_ERROR_MESSAGE) );
                notifyUser(false);
                closeBleService();
                showLog("ble 断连 error code:"+intent.getStringExtra(BleProfileService.EXTRA_ERROR_CODE));
            } else if (BleProfileService.BROADCASE_LOG.equals(action)) {
                showLog(intent.getStringExtra(BleProfileService.EXTRA_LOG));
            }

        }
    };
    private void showLog(String message) {
        runOnUiThread(() -> _fmLog.i(message));
    }
    private void notifyUser(boolean connState) {
        runOnUiThread( () -> {
            if (connState) {
                _connect.setEnabled(false);
                _connect.setText("disConnected");
            }
            else {
                _connect.setEnabled(true);
                _connect.setText("Connect");
            }
        });

    }
    private IntentFilter getIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleProfileService.BROADCAST_CONNECTION_STATE);
        intentFilter.addAction(BleProfileService.EXTRA_CONNECTION_STATE);
        intentFilter.addAction(BleProfileService.BROADCAST_ERROR);
        intentFilter.addAction(BleProfileService.BROADCASE_LOG);
        return intentFilter;
    }
    /**
     * auto conn model
     * */
    private void autoConnBle() {
        if (mService == null || !mService.isConnected()) {
            bleAddress = SharePreferUtil.getString(MainActivity.this,"address");
            Log.e(TAG, "autoConnBle: "+bleAddress );
            if (bleAddress == null || "".equals(bleAddress)) {
                showScannerFragment();
            } else {
                showProgress("正在尝试连接蓝牙...");
                startScan();
            }
        }
    }
    private Handler mHandler = new Handler();
    private boolean mIsScanning = false;
    private final static long SCAN_DURATION = 5000;
    public void startScan() {
        // Samsung Note II with Android 4.3 build JSS15J.N7100XXUEMK9 is not filtering by UUID at all. We must parse UUIDs manually
        _BtAdapter.startLeScan(mLEScanCallback);
        mIsScanning = true;
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mIsScanning) {
                    stopScan();
                    onScanResult(false, null);
                }
            }
        }, SCAN_DURATION);
    }
    /**
     * Stop scan if user tap Cancel button.
     */
    public void stopScan() {
        if (mIsScanning) {
            _BtAdapter.stopLeScan(mLEScanCallback);
            mIsScanning = false;
        }
    }
    public void onScanResult(boolean isLast, String address) {
        dismissProgress();
        if (isLast) {
            startBleService(address);
        } else {
            showScannerFragment();
        }
    }

    private BluetoothAdapter.LeScanCallback mLEScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            if (device != null && device.getName() != null) {
                if (bleAddress.equals(device.getAddress())) {
                    onScanResult(true, device.getAddress());
                    stopScan();
                } else {}
            }
        }
    };

    private void showScannerFragment() {
        /*ScannerFragment.getInstance(MainActivity.this, ChargeManager.RX_SERVICE_UUID,false)
                .show(getSupportFragmentManager(),"scanning");*/
        ScannerFragment.getInstance(MainActivity.this, null,false)
                .show(getSupportFragmentManager(),"scanning");
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
