package com.diy.blelib.bag;

import java.io.Serializable;

/**
 * @version V1.0 <历史数据转速包>
 *
  历史数据转速包 byte[] data
  Byte1：0xA2
  Byte2：历史时段数据总数
  Byte3，byte4，byte5，byte6：开始时间，UTC编码
  Byte7，byte8：本次运动时长，UTC编码
  Byte9，byte10：最高转速
  Byte11，byte12，--byte18：下一个运动时间段数据

  下一个历史数据转速包：
  Byte1：0xA3
  Byte2：本次数据包个数
  Byte3—byte18：历史包格式参照0xA2的说明
  历史数据包结束：
  Byte1：0xA4
  Byte2：0xFF

 *
 * @author: Xs
 * @date: 2016-04-18 17:42
 * @email Xs.lin@foxmail.com
 */
public class HistoryBag implements Serializable {
    private byte command ;
    private int dataNumber;  //历史时段数据总数 OR  本次数据包个数
    private long startTime;
    private int duration;//second
    private int maxSpeed;

    public HistoryBag(){}

    public void setBag(byte[] data) {
        command = data[0];
        dataNumber = Integer.parseInt(ByteUtil.toHexString(BagHandler.newByteArray(data[1])), 16);
        startTime = Long.parseLong(ByteUtil.toHexString(BagHandler.newByteArray(data[2], data[3], data[4], data[5])), 16);
        duration = Integer.parseInt(ByteUtil.toHexString(BagHandler.newByteArray(data[6], data[7])), 16);
        maxSpeed = Integer.parseInt(ByteUtil.toHexString(BagHandler.newByteArray(data[8], data[9])), 16);
    }
    public byte getCommand() {
        return command;
    }

    public void setCommand(byte command) {
        this.command = command;
    }

    public int getDataNumber() {
        return dataNumber;
    }

    public void setDataNumber(int dataNumber) {
        this.dataNumber = dataNumber;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        duration = duration;
    }

    public int getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(int maxSpeed) {
        this.maxSpeed = maxSpeed;
    }

    @Override
    public String toString() {
        return "HistoryBag{" +
                "command=" + command +
                ", dataNumber=" + dataNumber +
                ", startTime='" + startTime + '\'' +
                ", duration=" + duration +
                ", maxSpeed=" + maxSpeed +
                '}';
    }
}
