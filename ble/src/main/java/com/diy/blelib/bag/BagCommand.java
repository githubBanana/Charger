package com.diy.blelib.bag;

/**
 * @version V1.0 <下位机定义数据收发命令>
 * @author: Xs
 * @date: 2016-04-18 16:37
 * @email Xs.lin@foxmail.com
 */
public class BagCommand {

    /*接收命令*/
    public static final byte BAG_RECEIVE_REALTIME = (byte)0xA1;//实时转速包 command = 0xA1
    public static final byte BAG_RECEIVE_HISTORY_START = (byte)0xA2;//历史数据转速包 开始
    public static final byte BAG_RECEIVE_HISTORY_TRANSFER = (byte)0xA3;//下一个历史数据转速包 传输
    public static final byte BAG_RECEIVE_HISTORY_END = (byte)0xA4;//历史数据包结束
    public static final byte BAG_RECEIVE_SYNC_TIME = (byte) 0xA5;//同步时间
    public static final byte BAG_RECEIVE_MODIFY_DEVICE_NAME = 0;//修改设备名称  无定义
    public static final byte BAG_RECEIVE_CLEAR = 0;//清除历史数据包 无定义

    /*发送命令*/
    //获取历史数据包
    public static final byte BAG_SEND_GET_HISTORY_1 = (byte) 0x52;
    public static final byte BAG_SEND_GET_HISTORY_2 = (byte) 0xAA;
    //时间同步包
    public static final byte BAG_SEND_TIME_SYNC = (byte)0x51;
    //清除历史数据包
    public static final byte BAG_SEND_CLEAR1 = (byte)0x53;
    public static final byte BAG_SEND_CLEAR2 = (byte)0xAA;
    //修改设备名称包
    public static final byte BAG_SEND_MODIFY_DEVICE = (byte)0x54;



}
