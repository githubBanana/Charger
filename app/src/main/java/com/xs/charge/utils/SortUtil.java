package com.xs.charge.utils;

import android.content.Context;
import android.os.BatteryManager;

import com.xs.charge.activity.Test;
import com.xs.charge.event.NotifyEvent;


/**
 * @version V1.0 <描述当前版本功能>
 * @author: Xs
 * @date: 2016-06-08 17:10
 * @email Xs.lin@foxmail.com
 */
public class SortUtil {

    /**
     * 整理电池信息
     * @param event
     * @return
     */
    public static StringBuilder doBatteryInfo(NotifyEvent event,Context context) {
        StringBuilder s = new StringBuilder();

        switch (event.getStatus()) {
            case BatteryManager.BATTERY_STATUS_CHARGING:
                if (event.getPlugged() == BatteryManager.BATTERY_PLUGGED_AC)
                    s.append("使用充电器充电中...");
                else if (event.getPlugged() == BatteryManager.BATTERY_PLUGGED_USB)
                    s.append("使用USB充电中...");
                else if (event.getPlugged() == BatteryManager.BATTERY_PLUGGED_WIRELESS)
                    s.append("使用无线充电中...");
                break;
            case BatteryManager.BATTERY_STATUS_DISCHARGING:
                s.append("放电中...");
                break;
            case BatteryManager.BATTERY_STATUS_FULL:
                s.append("已充满...");
                break;
            case BatteryManager.BATTERY_STATUS_NOT_CHARGING:
                s.append("未充满...");
                break;
        }

        // 电池状态
        switch (event.getHealth()) {
            case BatteryManager.BATTERY_HEALTH_DEAD:
                s.append("\n电池状态 电池已损坏！");
                break;
            case BatteryManager.BATTERY_HEALTH_GOOD:
                s.append("\n电池状态 健康");
                break;
            case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE:
                s.append("\n电池状态 电压过高");
                break;
            case BatteryManager.BATTERY_HEALTH_OVERHEAT:
                s.append("\n电池状态 温度过高");
                break;
            case BatteryManager.BATTERY_HEALTH_UNKNOWN:
                s.append("\n电池状态 未知");
                break;
            case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE:
                s.append("\n电池状态 未知故障");
                break;
        }
        s.append("\n电池最大容量 "+event.getScale())
                .append("\n电池伏数 "+event.getVoltage()+"mV")
                .append("\n电池温度 "+event.getTemperature() / 10.0 +"℃")
                .append("\n电池技术 "+event.getTechnology())
                .append("\n电池容量 "+ Test.getBatteryCapacity2(context));
        return s;
    }
}
