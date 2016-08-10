package com.diy.blelib.bag;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @version V1.0 <实时转速包>
 *
 * 实时转速包：byte[] data
 * Byte1：0xA1
 * Byte2：0x00
 * Byte3，byte4：实时转速
 * Byte5，byte6：最高转速（本次运动）
 * Byte7: 电池电量
 *
 * @author: Xs
 * @date: 2016-04-18 14:22
 * @email Xs.lin@foxmail.com
 */
public class RealTimeBag implements Serializable{

    byte receiCommand;            //-->Byte1：0xA1
    byte Byte2;                   //-->Byte2：0x00
    int realTimeSpeed;            //-->Byte3，byte4：实时转速
    int maxSpeed;                 //-->Byte5，byte6：最高转速（本次运动）
    int battery;                  //-->Byte7: 电池电量

    String startTime;             //speed > BagHandler.START_COUNT_VALUE 时开始计时
    String startTimeStamp;        //开始时间:时间戳格式
    String endTime;               //时长:mm:ss格式
    String endTimeStamp;          //时长:时间戳格式
    List<Integer> mSpeedList;

    private static RealTimeBag instance = null;
    private RealTimeBag() {
        mSpeedList = new ArrayList<>();
    }
    public static synchronized RealTimeBag getBag() {
        if (instance == null)
            instance = new RealTimeBag();
        return instance;
    }

    public void setBag(byte[] data) {
        receiCommand = data[0];
        Byte2 = data[1];
        realTimeSpeed = Integer.parseInt(ByteUtil.toHexString(BagHandler.newByteArray(data[2], data[3])),16);
        maxSpeed = Integer.parseInt(ByteUtil.toHexString(BagHandler.newByteArray(data[4], data[5])),16);
        battery = Integer.parseInt(ByteUtil.toHexString(BagHandler.newByteArray(data[6])),16);
        if (realTimeSpeed >= BagHandler.START_COUNT_VALUE)
            add(realTimeSpeed);
    }

    public void clear() {
        instance = null;
    }
    public void clearSpeedList() {
        mSpeedList.clear();
    }

    public byte getReceiCommand() {
        return receiCommand;
    }

    public void setReceiCommand(byte receiCommand) {
        this.receiCommand = receiCommand;
    }

    public byte getByte2() {
        return Byte2;
    }

    public void setByte2(byte byte2) {
        Byte2 = byte2;
    }

    public int getRealTimeSpeed() {
        return realTimeSpeed;
    }

    public void setRealTimeSpeed(int realTimeSpeed) {
        this.realTimeSpeed = realTimeSpeed;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public void add(int realTimeSpeed) {
        mSpeedList.add(realTimeSpeed);
    }

    public List<Integer> getSpeedList() {
        return mSpeedList;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getStartTimeStamp() {
        return startTimeStamp;
    }

    public void setStartTimeStamp(String startTimeStamp) {
        this.startTimeStamp = startTimeStamp;
    }

    public String getEndTimeStamp() {
        return endTimeStamp;
    }

    public void setEndTimeStamp(String endTimeStamp) {
        this.endTimeStamp = endTimeStamp;
    }

    @Override
    public String toString() {
        return "RealTimeBag{" +
                "receiCommand=" + receiCommand +
                ", Byte2=" + Byte2 +
                ", realTimeSpeed=" + realTimeSpeed +
                ", maxSpeed=" + maxSpeed +
                ", battery=" + battery +
                ", startTime='" + startTime + '\'' +
                ", startTimeStamp='" + startTimeStamp + '\'' +
                ", endTime='" + endTime + '\'' +
                ", endTimeStamp='" + endTimeStamp + '\'' +
                ", mSpeedList=" + mSpeedList +
                '}';
    }
}
