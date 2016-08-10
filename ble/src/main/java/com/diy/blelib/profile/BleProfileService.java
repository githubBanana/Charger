/*******************************************************************************
 * Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 *
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided.
 * This heading must NOT be removed from the file.
 ******************************************************************************/
package com.diy.blelib.profile;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.diy.blelib.R;
import com.diy.blelib.bag.ByteUtil;

import de.greenrobot.event.EventBus;
import de.greenrobot.event.Subscribe;

public abstract class BleProfileService extends Service implements BleManagerCallbacks {
	private static final String TAG = "BleProfileService";

	public static final String BROADCAST_CONNECTION_STATE = "com.xs.charge.BROADCAST_CONNECTION_STATE";
	public static final String BROADCAST_SERVICES_DISCOVERED = "com.xs.charge.BROADCAST_SERVICES_DISCOVERED";
	public static final String BROADCAST_BOND_STATE = "com.xs.charge.BROADCAST_BOND_STATE";
	public static final String BROADCAST_BATTERY_LEVEL = "com.xs.charge.BROADCAST_BATTERY_LEVEL";
	public static final String BROADCAST_ERROR = "com.xs.charge.BROADCAST_ERROR";
	//add by xs.lin on 2016-6-13
	public static final String BROADCASE_LOG = "com.xs.charge.log";
	public static final String EXTRA_LOG = "com.xs.charge.log.EXTRA_LOG";

	/** The parameter passed when creating the service. Must contain the address of the sensor that we want to connect to */
	public static final String EXTRA_DEVICE_ADDRESS = "com.xs.charge.EXTRA_DEVICE_ADDRESS";
	/** The key for the device name that is returned in {@link #BROADCAST_CONNECTION_STATE} with state {@link #STATE_CONNECTED}. */
	public static final String EXTRA_DEVICE_NAME = "com.xs.charge.EXTRA_DEVICE_NAME";
	public static final String EXTRA_LOG_URI = "com.xs.charge.EXTRA_LOG_URI";
	public static final String EXTRA_CONNECTION_STATE = "com.xs.charge.EXTRA_CONNECTION_STATE";
	public static final String EXTRA_BOND_STATE = "com.xs.charge.EXTRA_BOND_STATE";
	public static final String EXTRA_SERVICE_PRIMARY = "com.xs.charge.EXTRA_SERVICE_PRIMARY";
	public static final String EXTRA_SERVICE_SECONDARY = "com.xs.charge.EXTRA_SERVICE_SECONDARY";
	public static final String EXTRA_BATTERY_LEVEL = "com.xs.charge.EXTRA_BATTERY_LEVEL";
	public static final String EXTRA_ERROR_MESSAGE = "com.xs.charge.EXTRA_ERROR_MESSAGE";
	public static final String EXTRA_ERROR_CODE = "com.xs.charge.EXTRA_ERROR_CODE";
	public static final String EXTRA_DESTORY_SERVICE = "com.xs.charge.EXTRA_DESTORY_SERVICE";
	public static final int STATE_LINK_LOSS = -1;
	public static final int STATE_DISCONNECTED = 0;
	public static final int STATE_CONNECTED = 1;
	public static final int STATE_CONNECTING = 2;
	public static final int STATE_DISCONNECTING = 3;
	//add by siushen 1227
	public static final int STATE_BROADCAST_ERROR = 4;
	public static final int STATE_RECONN_FAIL = 5;
	public static final int EXTRA_DESTORY_SERVICE_INT = 6; //hand to stop ble service

	private BleManager<BleManagerCallbacks> mBleManager;
	private Handler mHandler;

	public static boolean mConnected;
	private String mDeviceAddress;
	private String mDeviceName;
	private boolean serviceLife = false;
	private BluetoothAdapter mBlutoothAdapter;

	public class LocalBinder extends Binder {
		/**
		 * Disconnects from the sensor.
		 */
		public final void disconnect() {
			if (!mConnected) {
				onDeviceDisconnected(false);
				return;
			}

			// notify user about changing the state to DISCONNECTING
			final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
			broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_DISCONNECTING);
			LocalBroadcastManager.getInstance(BleProfileService.this).sendBroadcast(broadcast);

			mBleManager.disconnect();
		}

		/**
		 * Returns the device address
		 *
		 * @return device address
		 */
		public String getDeviceAddress() {
			return mDeviceAddress;
		}

		/**
		 * Returns the device name
		 *
		 * @return the device name
		 */
		public String getDeviceName() {
			return mDeviceName;
		}

		/**
		 * Returns <code>true</code> if the device is connected to the sensor.
		 *
		 * @return <code>true</code> if device is connected to the sensor, <code>false</code> otherwise
		 */
		public boolean isConnected() {
			return mConnected;
		}

		/**
		 * notify from service to manager
		 * @param command
		 */
		public void serviceToManager(byte[] command) {
			mBleManager.serviceToManager(command);
		}
	}

	@Override
	public IBinder onBind(final Intent intent) {
		return getBinder();
	}

	/**
	 * Returns the binder implementation. This must return class implementing the additional manager interface that may be used in the binded activity.
	 *
	 * @return the service binder
	 */
	protected LocalBinder getBinder() {
		// default implementation returns the basic binder. You can overwrite the LocalBinder with your own, wider implementation
		return new LocalBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// we must allow to rebind to the same service
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onCreate() {
		super.onCreate();
		serviceLife = true;
		mHandler = new Handler();

		// initialize the manager
		mBleManager = initializeManager();
		mBleManager.setGattCallbacks(this);
		mBlutoothAdapter = BluetoothAdapter.getDefaultAdapter();

		EventBus.getDefault().register(this);
	}

	@SuppressWarnings("rawtypes")
	protected abstract BleManager initializeManager();



	@Override
	public int onStartCommand(final Intent intent, final int flags, final int startId) {
		if (intent == null || !intent.hasExtra(EXTRA_DEVICE_ADDRESS))
			throw new UnsupportedOperationException("No device address at EXTRA_DEVICE_ADDRESS key");

		mDeviceAddress = intent.getStringExtra(EXTRA_DEVICE_ADDRESS);
		Log.i(TAG, "Service started");
		//每次打开服务再次确认蓝牙是否打开
		if (!mBlutoothAdapter.isEnabled())
			mBlutoothAdapter.enable();
		// notify user about changing the state to CONNECTING
		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_CONNECTING);
		LocalBroadcastManager.getInstance(BleProfileService.this).sendBroadcast(broadcast);

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
		final BluetoothAdapter adapter = bluetoothManager.getAdapter();
		final BluetoothDevice device = adapter.getRemoteDevice(mDeviceAddress);
		mDeviceName = device.getName();
		onServiceStarted();

		Log.v(TAG, "Connecting...");
		mBleManager.connect(BleProfileService.this, device);
		return START_REDELIVER_INTENT;
	}

	/**
	 * Called when the service has been started. The device name and address are set. It nRF Logger is installed than logger was also initialized.
	 */
	protected void onServiceStarted() {
		// empty default implementation
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		final Intent broadcast = new Intent(EXTRA_DESTORY_SERVICE);
		LocalBroadcastManager.getInstance(BleProfileService.this).sendBroadcast(broadcast);
		// shutdown the manager
//		if(mBleManager == null)
		mBleManager.closeBluetoothGatt();
		mBleManager = null;
		mDeviceAddress = null;
		mDeviceName = null;
		mConnected = false;
		serviceLife = false;

		EventBus.getDefault().unregister(this);
	}


	@Override
	public void onDeviceConnected() {
		mConnected = true;

		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_CONNECTED);
		broadcast.putExtra(EXTRA_DEVICE_ADDRESS, mDeviceAddress);
		broadcast.putExtra(EXTRA_DEVICE_NAME, mDeviceName);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}


	@Override
	public void onLinklossOccur() {
		Log.w(TAG, "Connection lost");
		mConnected = false;

		final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
		broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_LINK_LOSS);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onServicesDiscovered(final boolean optionalServicesFound) {
		Log.i(TAG, "Services Discovered");
		Log.v(TAG, "Primary service found");
		if (optionalServicesFound)
			Log.v(TAG, "Secondary service found");

		final Intent broadcast = new Intent(BROADCAST_SERVICES_DISCOVERED);
		broadcast.putExtra(EXTRA_SERVICE_PRIMARY, true);
		broadcast.putExtra(EXTRA_SERVICE_SECONDARY, optionalServicesFound);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onDeviceNotSupported() {
		Log.i(TAG, "Services Discovered");
		Log.w(TAG, "Device is not supported");

		final Intent broadcast = new Intent(BROADCAST_SERVICES_DISCOVERED);
		broadcast.putExtra(EXTRA_SERVICE_PRIMARY, false);
		broadcast.putExtra(EXTRA_SERVICE_SECONDARY, false);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);

		// no need for disconnecting, it will be disconnected by the manager automatically
	}

	@Override
	public void onBatteryValueReceived(final int value) {
		Log.i(TAG, "Battery level received: " + value + "%");

		final Intent broadcast = new Intent(BROADCAST_BATTERY_LEVEL);
		broadcast.putExtra(EXTRA_BATTERY_LEVEL, value);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onBondingRequired() {
		Log.v(TAG, "Bond state: Bonding...");
//		showToast(R.string.bonding);

		final Intent broadcast = new Intent(BROADCAST_BOND_STATE);
		broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDING);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}

	@Override
	public void onBonded() {
		Log.i(TAG, "Bond state: Bonded");
//		showToast(R.string.bonded);

		final Intent broadcast = new Intent(BROADCAST_BOND_STATE);
		broadcast.putExtra(EXTRA_BOND_STATE, BluetoothDevice.BOND_BONDED);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
	}
	@Override
	public void onError(String message, int errorCode, boolean hand) {
		// TODO Auto-generated method stub
		Log.e(TAG, message + " (" + errorCode + ")");
		if(serviceLife) {
			final Intent broadcast = new Intent(BROADCAST_ERROR);
			broadcast.putExtra(EXTRA_ERROR_MESSAGE, message);
			broadcast.putExtra(EXTRA_ERROR_CODE, errorCode);
			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
			mBleManager.disconnect();
		}
	//	stopSelf();
	}
	@Override
	public void onDeviceDisconnected(boolean hand) {
		// TODO Auto-generated method stub
		Log.i(TAG, "Disconnected");
		mConnected = false;
		mDeviceAddress = null;
		mDeviceName = null;
		if(serviceLife) {
			final Intent broadcast = new Intent(BROADCAST_CONNECTION_STATE);
			broadcast.putExtra(EXTRA_CONNECTION_STATE, STATE_DISCONNECTED);
			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
		}
		// user requested disconnection. We must stop the service
//		stopSelf();
	}

	@Override
	public void onLogRecord(String message) {
		if(serviceLife) {
			final Intent broadcast = new Intent(BROADCASE_LOG);
			broadcast.putExtra(EXTRA_LOG,message);
			LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
		}
	}

	/**
	 * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
	 *
	 * @param messageResId
	 *            an resource id of the message to be shown
	 */
	protected void showToast(final int messageResId) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(BleProfileService.this, messageResId, Toast.LENGTH_SHORT).show();
			}
		});
	}

	/**
	 * Shows a message as a Toast notification. This method is thread safe, you can call it from any thread
	 *
	 * @param message
	 *            a message to be shown
	 */
	protected void showToast(final String message) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(BleProfileService.this, message, Toast.LENGTH_SHORT).show();
			}
		});
	}


	/**
	 * Returns the device address
	 *
	 * @return device address
	 */
	protected String getDeviceAddress() {
		return mDeviceAddress;
	}

	/**
	 * Returns the device name
	 *
	 * @return the device name
	 */
	protected String getDeviceName() {
		return mDeviceName;
	}

	/**
	 * Returns <code>true</code> if the device is connected to the sensor.
	 *
	 * @return <code>true</code> if device is connected to the sensor, <code>false</code> otherwise
	 */
	protected boolean isConnected() {
		return mConnected;
	}

	@Subscribe
	public void onEvent(String batteryStr) {
		String hexString = Integer.toHexString(Integer.valueOf(batteryStr));
		byte[] battery = ByteUtil.hexStringToBytes(hexString);
		mBleManager.serviceToManager(battery);
	}

}
