package com.diy.blelib.bag;

import android.util.Log;

/**
 * @version V1.0 <主动发送命令包>
 * @author: Xs
 * @date: 2016-05-04 16:41
 * @email Xs.lin@foxmail.com
 */
public class ActiveSend {

    /**
     * 发送 获取历史数据命令
     * @return
     */
    public static byte[] toGetHistorySpeedData() {
        byte[] command = new byte[2];
        command[0] = BagCommand.BAG_SEND_GET_HISTORY_1;
        command[1] = BagCommand.BAG_SEND_GET_HISTORY_2;
        return command;
    }

    /**
     * 发送 同步时间命令
     * @return
     */
    public static byte[] toSyncTime() {
        String string = Long.toHexString(BagHandler.getCurrTime());//获取当前时间
        byte[] array = ByteUtil.hexStringToBytes(string);
        byte[] command = new byte[5];
        command[0] = BagCommand.BAG_SEND_TIME_SYNC;
        for (int i = 1; i < command.length; i++)
            command[i] = array[i-1];
        return command;
    }

    /**
     * 发送 修改设备名称命令
     * @param name
     * @return
     */
    public static byte[] toModifyDeviceName(String name) {
        byte[] command = new byte[8];
        command[0] = BagCommand.BAG_SEND_MODIFY_DEVICE;
        if (name.length() <= 6) {
            command[1] = (byte) name.length();
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                command[i+2] = (byte) c;
            }
            return command;
        }
        return null;
    }

    /**
     * 发送 清除历史数据命令
     * @return
     */
    public static byte[] toClearHistory() {
        byte[] command = new byte[2];
        command[0] = BagCommand.BAG_SEND_CLEAR1;
        command[1] = BagCommand.BAG_SEND_CLEAR2;
        return command;
    }
}
