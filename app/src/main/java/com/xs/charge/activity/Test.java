package com.xs.charge.activity;

import android.content.Context;
import android.util.Log;

/**
 * Created by Administrator on 2016/6/16.
 */
public class Test {

    public static final String TAG = Test.class.getSimpleName();
    public static String getBatteryCapacity(Context context) {
        Object mPowerProfile_ = null;

        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(context);
        } catch (Exception e) {
            e.printStackTrace();
        }
        double batteryCapacity = 0.0;
        try {
            batteryCapacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(mPowerProfile_, "battery.capacity");
            Log.e(TAG, "getBatteryCapacity: "+batteryCapacity );
        } catch (Exception e) {
            e.printStackTrace();
            return "0";
        }
        return ""+batteryCapacity;
    }

    public static String getBatteryCapacity2(Context context) {

        // Power profile class instance
        Object mPowerProfile_ = null;

        // Reset variable for battery capacity
        double batteryCapacity = 0;

        // Power profile class name
        final String POWER_PROFILE_CLASS = "com.android.internal.os.PowerProfile";

        try {

            // Get power profile class and create instance. We have to do this
            // dynamically because android.internal package is not part of public API
            mPowerProfile_ = Class.forName(POWER_PROFILE_CLASS)
                    .getConstructor(Context.class).newInstance(context);

        } catch (Exception e) {

            // Class not found?
            e.printStackTrace();
        }

        try {
            // Invoke PowerProfile method "getAveragePower" with param "battery.capacity"
            batteryCapacity = (Double) Class
                    .forName(POWER_PROFILE_CLASS)
                    .getMethod("getAveragePower", java.lang.String.class)
                    .invoke(mPowerProfile_, "battery.capacity");
        } catch (Exception e) {

            // Something went wrong
            e.printStackTrace();
        }

        return batteryCapacity+"";
    }

}
