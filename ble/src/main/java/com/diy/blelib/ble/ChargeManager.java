/*******************************************************************************
 * Copyright (c) 2013 Nordic Semiconductor. All Rights Reserved.
 *
 * The information contained herein is property of Nordic Semiconductor ASA.
 * Terms and conditions of usage are described in detail in NORDIC SEMICONDUCTOR STANDARD SOFTWARE LICENSE AGREEMENT.
 * Licensees are granted free, non-transferable use of the information. NO WARRANTY of ANY KIND is provided.
 * This heading must NOT be removed from the file.
 ******************************************************************************/
package com.diy.blelib.ble;

import java.util.List;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.diy.blelib.bag.ByteUtil;
import com.diy.blelib.profile.BleManager;
import com.diy.blelib.utils.ChargeConfig;
import com.diy.blelib.utils.SharePreferUtil;

/**
 * HTSManager class performs BluetoothGatt operations for connection, service discovery, enabling indication and reading characteristics. All operations required to connect to device with BLE HT
 * Service and reading health thermometer values are performed here. HTSActivity implements HTSManagerCallbacks in order to receive callbacks of BluetoothGatt operations
 */
public class ChargeManager implements BleManager<ChargeManagerCallbacks> {
	private final String TAG = "ChargeManager";
	private ChargeManagerCallbacks mCallbacks;
	private BluetoothGatt mBluetoothGatt;
	private Context mContext;

	//温度服务
	public final static UUID TP_SERVICE_UUID = UUID.fromString("00001809-0000-1000-8000-00805f9b34fb");
	private static final UUID TP_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A1C-0000-1000-8000-00805f9b34fb");

	//心率服务
	public final static UUID HR_SERVICE_UUID = UUID.fromString("0000180D-0000-1000-8000-00805f9b34fb");
	private static final UUID HR_SENSOR_LOCATION_CHARACTERISTIC_UUID = UUID.fromString("00002A38-0000-1000-8000-00805f9b34fb");
	private static final UUID HR_MEASUREMENT_CHARACTERISTIC_UUID = UUID.fromString("00002A37-0000-1000-8000-00805f9b34fb");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

	//串口服务
	public static final UUID RX_SERVICE_UUID = UUID.fromString("00000001-0000-1000-8000-00805f9b34fb");
	//串口写特征值
	public static final UUID W_RX_CHAR_UUID = UUID.fromString("00000002-0000-1000-8000-00805f9b34fb");
	//串口读特征值
	public static final UUID R_TX_CHAR_UUID = UUID.fromString("00000003-0000-1000-8000-00805f9b34fb");

    //电池服务
	public final static UUID BATTERY_SERVICE = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
	private final static UUID BATTERY_LEVEL_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

	private final static String ERROR_CONNECTION_STATE_CHANGE = "Error on connection state change";
	private final static String ERROR_DISCOVERY_SERVICE = "Error on discovering services";
	private final static String ERROR_READ_CHARACTERISTIC = "Error on reading characteristic";
	private final static String ERROR_WRITE_DESCRIPTOR = "Error on writing descriptor";
	private final static String ERROR_AUTH_ERROR_WHILE_BONDED = "Phone has lost bonding information";

	private BluetoothGattCharacteristic  mBatteryCharacteritsic,mHrsCharacteristic,mHistoryCharacteristic,mTpCharacteristic,
			mR_TXCharacteristic,mW_TXCharacteristic;

	private final int HIDE_MSB_8BITS_OUT_OF_32BITS = 0x00FFFFFF;
	private final int HIDE_MSB_8BITS_OUT_OF_16BITS = 0x00FF;
	private final int SHIFT_LEFT_8BITS = 8;
	private final int SHIFT_LEFT_16BITS = 16;
	private final int GET_BIT24 = 0x00400000;
	private static final int FIRST_BIT_MASK = 0x01;
	private static ChargeManager managerInstance = null;

	/**
	 * singleton implementation of HTSManager class
	 */
	public static synchronized ChargeManager getHTSManager() {
		if (managerInstance == null) {
			managerInstance = new ChargeManager();
		}
		return managerInstance;
	}

	/**
	 * callbacks for activity {HTSActivity} that implements HTSManagerCallbacks interface activity use this method to register itself for receiving callbacks
	 */
	@Override
	public void setGattCallbacks(ChargeManagerCallbacks callbacks) {
		mCallbacks = callbacks;
	}

	@Override
	public void connect(Context context, BluetoothDevice device) {
		mBluetoothGatt = device.connectGatt(context, false, mGattCallback);
		mContext = context;
	}

	@Override
	public void disconnect() {
		Log.d(TAG, "Disconnecting device");
		if (mBluetoothGatt != null) {
			mBluetoothGatt.disconnect();
		}
	}

	private BroadcastReceiver mBondingBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(final Context context, final Intent intent) {
			final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
			final int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
			final int previousBondState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, -1);

			Log.d(TAG, "Bond state changed for: " + device.getAddress() + " new state: " + bondState + " previous: " + previousBondState);

			// skip other devices
			if (!device.getAddress().equals(mBluetoothGatt.getDevice().getAddress()))
				return;

			if (bondState == BluetoothDevice.BOND_BONDED) {
				// We've read Battery Level, now enabling HT indications
				if (mHrsCharacteristic != null) {
					enableHRNotification();
				}
				mContext.unregisterReceiver(this);
				mCallbacks.onBonded();
			}
		}
	};

	/**
	 * BluetoothGatt callbacks for connection/disconnection, service discovery, receiving indication, etc
	 */
	private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (newState == BluetoothProfile.STATE_CONNECTED) {
					Log.d(TAG, "Device connected");
					mBluetoothGatt.discoverServices();
					//This will sendd callback to HTSActivity when device get connected
					mCallbacks.onDeviceConnected();
				} else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
					Log.e(TAG, "Device disconnected1");
					//This will send callback to HTSActivity when device get disconnected
					if(mCallbacks != null);
						mCallbacks.onDeviceDisconnected(false);
				}
			} else {
				if(mCallbacks != null) {
					mCallbacks.onError(ERROR_CONNECTION_STATE_CHANGE, status, false);
					Log.e(TAG, "Device disconnected2");
				}
			}
		}

		@Override
		public void onServicesDiscovered(BluetoothGatt gatt, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				List<BluetoothGattService> services = gatt.getServices();
				for (BluetoothGattService service : services) {
					if (service.getUuid().equals(HR_SERVICE_UUID)) {
                        mHrsCharacteristic = service.getCharacteristic(HR_MEASUREMENT_CHARACTERISTIC_UUID);
						mHistoryCharacteristic = service.getCharacteristic(HR_SENSOR_LOCATION_CHARACTERISTIC_UUID);
                    }
					if (service.getUuid().equals(TP_SERVICE_UUID)) {
						mTpCharacteristic = service.getCharacteristic(TP_MEASUREMENT_CHARACTERISTIC_UUID);
					}
					if (service.getUuid().equals(BATTERY_SERVICE)) {
						mBatteryCharacteritsic = service.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC);
					}
					if (service.getUuid().equals(RX_SERVICE_UUID)) {
						mR_TXCharacteristic = service.getCharacteristic(R_TX_CHAR_UUID);
					}
				}

			/*	if (mTpCharacteristic == null) {
					mCallbacks.onDeviceNotSupported();
					gatt.disconnect();
				}*/

			/*	if (mHistoryCharacteristic != null)
					Log.e(TAG,"mHistoryCharacteristic:"+mHistoryCharacteristic);

				if (mHrsCharacteristic != null) {
					enableHRNotification();
					mCallbacks.onServicesDiscovered(false);
                } else {
					Log.e(TAG,"mHrsCharacteristic == null");
					mCallbacks.onDeviceNotSupported();
					gatt.disconnect();
					return;
				}*/
				/*if (mBatteryCharacteritsic != null) {
					readBatteryLevel();
                    enableHRNotification();
//					enableTPIndication();
                } else {
//					enableTPIndication();
                    enableHRNotification();
				}*/
				if (mR_TXCharacteristic != null) {
					mCallbacks.onServicesDiscovered(false);
				} else {
					Log.e(TAG,"mR_TXCharacteristic == null");
					mCallbacks.onDeviceNotSupported();
					gatt.disconnect();
					return;
				}
			} else {
				mCallbacks.onError(ERROR_DISCOVERY_SERVICE, status,false);
			}
		}

		@Override
		public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				if (characteristic.getUuid().equals(BATTERY_LEVEL_CHARACTERISTIC)) {
					int batteryValue = characteristic.getValue()[0];
					Log.e(TAG, "onCharacteristicRead: "+batteryValue );
					mCallbacks.onBatteryValueReceived(batteryValue);
				}

			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
				if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_NONE) {
					Log.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					mCallbacks.onError(ERROR_AUTH_ERROR_WHILE_BONDED, status,false);
				}
			} else {
				mCallbacks.onError(ERROR_READ_CHARACTERISTIC, status,false);
			}
		}
        /**
         * 转速数据获取通道
         * @param gatt
         * @param characteristic
         */
		@Override
		public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
			if(characteristic.getUuid().equals(HR_MEASUREMENT_CHARACTERISTIC_UUID)) {
				byte[] data = characteristic.getValue();
				mCallbacks.onLogRecord("下位机 send : "+ByteUtil.bytesToHexString(characteristic.getValue()));
				try {
//                    mCallbacks.onBagReceived(by);
					if (data[0] == 0x01) {//下位机允许接受数据
						if (ChargeConfig.mode) {
							byte[] bytes = ByteUtil.hexStringToBytes(Integer.toHexString(ChargeConfig.battery));
							sendCommand(bytes);
						} else {//test mode

						}
					}
                } catch (Exception e) {
                    e.printStackTrace();
                }
			}
			if (characteristic.getUuid().equals(TP_MEASUREMENT_CHARACTERISTIC_UUID)) {
				Log.e(TAG, "onCharacteristicChanged: "+characteristic.getValue()[0] );

			}
			if (characteristic.getUuid().equals(R_TX_CHAR_UUID)) {
				byte[] data = characteristic.getValue();
				mCallbacks.onLogRecord("下位机 send : "+ByteUtil.bytesToHexString(characteristic.getValue()));
				try {
					if (data[0] == 0x01) {//下位机允许接受数据
						if (ChargeConfig.mode) {
							byte[] bytes = ByteUtil.hexStringToBytes(Integer.toHexString(ChargeConfig.battery));
							sendCommand(bytes);
						} else {//test mode

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		@Override
		public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
			if (status == BluetoothGatt.GATT_SUCCESS) {
				// HT indications has been enabled
			} else if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION) {
				if (gatt.getDevice().getBondState() == BluetoothDevice.BOND_NONE) {
					mCallbacks.onBondingRequired();

					final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
					mContext.registerReceiver(mBondingBroadcastReceiver, filter);
				} else {
					Log.w(TAG, ERROR_AUTH_ERROR_WHILE_BONDED);
					mCallbacks.onError(ERROR_AUTH_ERROR_WHILE_BONDED, status,false);
				}
			} else {
				Log.e(TAG, ERROR_WRITE_DESCRIPTOR + " (" + status + ")");
				mCallbacks.onError(ERROR_WRITE_DESCRIPTOR, status,false);
			}
		}
	};


	public void readBatteryLevel() {
		if (mBatteryCharacteritsic != null) {
			mBluetoothGatt.readCharacteristic(mBatteryCharacteritsic);
		} else {
			Log.e(TAG, "Battery Level Characteristic is null");
		}
	}
	/**
	 * Enabling notification on Heart Rate Characteristic
	 */
	private void enableHRNotification() {
		Log.d(TAG, "Enabling heart rate notifications");
		mBluetoothGatt.setCharacteristicNotification(mHrsCharacteristic, true);
		BluetoothGattDescriptor descriptor = mHrsCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
		mBluetoothGatt.writeDescriptor(descriptor);
	}

	/**
	 *  发送命令
	 * @param command
	 */
	void sendCommand(byte[] command) {
		/*mHistoryCharacteristic.setValue(command);
		mBluetoothGatt.writeCharacteristic(mHistoryCharacteristic);*/
		writeCharacteristic(command);
		if (ChargeConfig.mode)
			Log.e(TAG, "正常模式 sendCommand: "+command[0] );
		else
			Log.e(TAG, "测试模式 sendCommand: "+command[0] );
		mCallbacks.onLogRecord("app send byte : "+ByteUtil.bytesToHexString(command));
	}

	private short convertNegativeByteToPositiveShort(byte octet) {
		if (octet < 0) {
			return (short) (octet & HIDE_MSB_8BITS_OUT_OF_16BITS);
		} else {
			return octet;
		}
	}

	private int getTwosComplimentOfNegativeMantissa(int mantissa) {
		if ((mantissa & GET_BIT24) != 0) {
			return ((((~mantissa) & HIDE_MSB_8BITS_OUT_OF_32BITS) + 1) * (-1));
		} else {
			return mantissa;
		}
	}

	/**
	 * This method will check if Heart rate value is in 8 bits or 16 bits
	 */
	private boolean isHeartRateInUINT16(byte value) {
		if ((value & FIRST_BIT_MASK) != 0)
			return true;
		return false;
	}
	@Override
	public void closeBluetoothGatt() {
		try {
			mContext.unregisterReceiver(mBondingBroadcastReceiver);
		} catch (Exception e) {
			// the receiver must have been not registered or unregistered before
		}
		if (mBluetoothGatt != null) {
			mBluetoothGatt.close();
			mBluetoothGatt = null;
			mBatteryCharacteritsic = null;
			mHrsCharacteristic = null;

		}
	}

	@Override
	public void serviceToManager(byte[] command) {
		sendCommand(command);
	}

	/**
	 * enable Health Thermometer indication on Health Thermometer Measurement characteristic
	 */
	private void enableTPIndication() {
		mBluetoothGatt.setCharacteristicNotification(mTpCharacteristic, true);
		BluetoothGattDescriptor descriptor = mTpCharacteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR_UUID);
		descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
		mBluetoothGatt.writeDescriptor(descriptor);
	}


	public void writeCharacteristic(byte[] value){

		if(mBluetoothGatt != null){
			BluetoothGattService RxService = mBluetoothGatt.getService(RX_SERVICE_UUID);
			if (RxService == null) {
				return;
			}
			BluetoothGattCharacteristic RxChar = RxService.getCharacteristic(W_RX_CHAR_UUID);
			if (RxChar == null) {
				return;
			}
			RxChar.setValue(value);
			boolean status = mBluetoothGatt.writeCharacteristic(RxChar);
		}
		//	enableTPIndication();
	}

}
