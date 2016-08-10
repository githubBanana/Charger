package com.diy.blelib.ble;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.diy.blelib.profile.BleManager;
import com.diy.blelib.profile.BleProfileService;

public class ChargeService extends BleProfileService implements ChargeManagerCallbacks {
		private static final String TAG = ChargeService.class.getSimpleName();

	public static final String BROADCAST_TPS_MEASUREMENT = "com.diy.blelib.ble.BROADCAST_HTS_MEASUREMENT";
	public static final String BROADCAST_HRS_MEASUREMENT = "com.diy.blelib.ble.BROADCAST_HRS_MEASUREMENT";
	public static final String BROADCAST_OXY_MEASUREMENT = "com.diy.blelib.ble.BROADCAST_OXY_MEASUREMENT";
	public static final String EXTRA_TEMPERATURE = "com.diy.blelib.ble.EXTRA_TEMPERATURE";
	public static final String EXTRA_HEART = "com.diy.blelib.ble.EXTRA_HEART";
	public static final String EXTRA_OXYGEN = "com.diy.blelib.ble.EXTRA_OXYGEN";
	public static final String BROADCAST_UPGAMEARR = "com.diy.blelib.ble.BROADCAST_UPGAMEARR";

	private final static String ACTION_DISCONNECT = "com.diy.blelib.ble.ACTION_DISCONNECT";

	private final static int NOTIFICATION_ID = 267;
	private final static int OPEN_ACTIVITY_REQ = 0;
	private final static int DISCONNECT_REQ = 1;

	private ChargeManager mManager;
	private boolean mBinded;

	private final LocalBinder mBinder = new RSCBinder();

	/**
	 * This local binder is an interface for the binded activity to operate with the HTS sensor
	 */
	public class RSCBinder extends LocalBinder {
		// empty
	}

	@Override
	protected LocalBinder getBinder() {
		return mBinder;
	}

	@Override
	protected BleManager<ChargeManagerCallbacks> initializeManager() {
		return mManager = ChargeManager.getHTSManager();
	}

	@Override
	public void onCreate() {
		super.onCreate();

		final IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_DISCONNECT);
		registerReceiver(mDisconnectActionBroadcastReceiver, filter);

	}

	@Override
	public void onDestroy() {
		// when user has disconnected from the sensor, we have to cancel the notification that we've created some milliseconds before using unbindService
		cancelNotification();
		unregisterReceiver(mDisconnectActionBroadcastReceiver);

		super.onDestroy();
	}

	@Override
	public IBinder onBind(final Intent intent) {
		mBinded = true;
		return super.onBind(intent);
	}

	@Override
	public void onRebind(final Intent intent) {
		mBinded = true;
		// when the activity rebinds to the service, remove the notification
		cancelNotification();

		// read the battery level when back in the Activity
		if (isConnected())
			mManager.readBatteryLevel();
	}

	@Override
	public boolean onUnbind(final Intent intent) {
		mBinded = false;
		// when the activity closes we need to show the notification that user is connected to the sensor
//		createNotifcation(R.string.hts_notification_connected_message, 0);
		return super.onUnbind(intent);
	}

	@Override
	public void onHTValueReceived(final double value) {
		final Intent broadcast = new Intent(BROADCAST_TPS_MEASUREMENT);
		broadcast.putExtra(EXTRA_TEMPERATURE, value);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}
	@Override
	public void onHRSValueReceived(int value) {
		// TODO Auto-generated method stub
		final Intent broadcast = new Intent(BROADCAST_HRS_MEASUREMENT);
		broadcast.putExtra(EXTRA_HEART, value);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onBagReceived(byte config) {
		final Intent broadcast = new Intent(BROADCAST_HRS_MEASUREMENT);
		broadcast.putExtra(EXTRA_HEART,config);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onUpGameArr() {
		final Intent broadcast = new Intent(BROADCAST_UPGAMEARR);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	/**
	 * Creates the notification
	 *
	 *  messageResIdthe
	 *            message resource id. The message must have one String parameter,<br />
	 *            f.e. <code>&lt;string name="name"&gt;%s is connected&lt;/string&gt;</code>
	 * @param defaults
	 *            signals that will be used to notify the user
	 */
	private void createNotifcation(final int messageResId, final int defaults) {
		//暂时屏蔽
/*		final Intent parentIntent = new Intent(this, FeaturesActivity.class);
		parentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		final Intent targetIntent = new Intent(this, HTSActivity.class);

		final Intent disconnect = new Intent(ACTION_DISCONNECT);
		final PendingIntent disconnectAction = PendingIntent.getBroadcast(this, DISCONNECT_REQ, disconnect, PendingIntent.FLAG_UPDATE_CURRENT);

		// both activities above have launchMode="singleTask" in the AndoridManifest.xml file, so if the task is already running, it will be resumed
		final PendingIntent pendingIntent = PendingIntent.getActivities(this, OPEN_ACTIVITY_REQ, new Intent[] { parentIntent, targetIntent }, PendingIntent.FLAG_UPDATE_CURRENT);
		final Notification.Builder builder = new Notification.Builder(this).setContentIntent(pendingIntent);
		builder.setContentTitle(getString(R.string.app_name)).setContentText(getString(messageResId, getDeviceName()));
		builder.setSmallIcon(R.drawable.ic_stat_notify_hts);
		builder.setShowWhen(defaults != 0).setDefaults(defaults).setAutoCancel(true).setOngoing(true);
		builder.addAction(R.drawable.ic_action_bluetooth, getString(R.string.hts_notification_action_disconnect), disconnectAction);

		final Notification notification = builder.build();
		final NotificationManager nm = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(NOTIFICATION_ID, notification);*/
	}

	/**
	 * Cancels the existing notification. If there is no active notification this method does nothing
	 */
	private void cancelNotification() {
	/*	final NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION_ID);*/
	}

	/**
	 * This broadcast receiver listens for {@link #ACTION_DISCONNECT} that may be fired by pressing Disconnect action button on the notification.
	 */
	private BroadcastReceiver mDisconnectActionBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			Log.i(TAG, "[HTS] Disconnect action pressed");
			if (isConnected())
				getBinder().disconnect();
			else
				stopSelf();
		};
	};







}
