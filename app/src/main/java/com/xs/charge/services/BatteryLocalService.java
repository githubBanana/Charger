package com.xs.charge.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.diy.blelib.utils.ChargeConfig;
import com.xs.charge.event.NotifyEvent;
import com.xs.charge.activity.MainActivity;

import de.greenrobot.event.EventBus;

/**
 * @version V1.0 <描述当前版本功能>
 * @author: Xs
 * @date: 2016-06-08 10:49
 * @email Xs.lin@foxmail.com
 */
public class BatteryLocalService extends Service {
    private static final String TAG = "BatteryLocalService";
    private NotificationManager _nm;
    private Notification _notification;


    public class LocalBinder extends Binder {
        public LocalBinder getService() {
            return LocalBinder.this;
        }
    }

    @Override
    public void onCreate() {
        Log.e(TAG, "onCreate: " );
        _nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        showNotification();
        BatteryManager b = (BatteryManager) getSystemService(BATTERY_SERVICE);
        registerReceiver(_receiver,getIntentFilter());
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e(TAG, "onStartCommand: start id:"+startId +"  intent:"+intent );
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy: " );
        _nm.cancel(1);
        unregisterReceiver(_receiver);
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return _binder;
    }
    private final IBinder _binder = new LocalBinder();

    /**
     * Show a notification while this service is running.
     */
    private void showNotification(int level) {
        // In this sample, we'll use the same text for the ticker and the expanded notification

        // The PendingIntent to launch our activity if the user selects this notification
/*        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, LocalServiceActivities.Controller.class), 0);*/
        PendingIntent contentIntent = PendingIntent.getActivity(this,0,new Intent(this, MainActivity.class),0);
        // Set the info for the views that show in the notification panel.
        _notification = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.ic_lock_idle_low_battery)  // the status icon
                .setWhen(System.currentTimeMillis())  // the time stamp
                .setContentTitle("Charge")  // the label of the entry
                .setContentText(level+"%")  // the contents of the entry
//                .setContentIntent(contentIntent)  // The intent to send when the entry is clicked
                .build();
        _notification.flags = Notification.FLAG_ONGOING_EVENT; // 设置常驻 Flag
        // Send the notification.
        _nm.notify(1, _notification);
    }

    private BroadcastReceiver _receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
            int health = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
            boolean present = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, false);
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0); //电池电量，数字
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0); //电池最大容量
            int icon_small = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, 0);
            int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);  //充电类型
            int voltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0); //电池伏数
            int temperature = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0);//电池温度
            String technology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY); //电池技术
            NotifyEvent event = new NotifyEvent(status,health,present,level,scale,icon_small,plugged,voltage,temperature,technology);
            EventBus.getDefault().post(event);
            if (ChargeConfig.mode)
                EventBus.getDefault().post(""+level);
            ChargeConfig.battery = level;
            //update notification
            showNotification(level);
        }
    };
    private IntentFilter getIntentFilter() {
        final IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction("test");
        return filter;
    }
}
