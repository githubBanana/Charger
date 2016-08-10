package com.diy.blelib.bag;
import android.util.Log;
import java.util.List;

/**
 * @version V1.0 <数据包解析>
 * @author: Xs
 * @date: 2016-04-18 19:24
 * @email Xs.lin@foxmail.com
 */
public class BagHandler {

    private static final String TAG = "BagHandler";
    public static final int START_COUNT_VALUE = 300;//转速为300时开始计时并临时保存实时数据list,该值为测试结果得出

    public static byte doBag(byte[] data) {
        byte recei = data[0];
        /**实时数据获取**/
        if (recei == BagCommand.BAG_RECEIVE_REALTIME) {
            RealTimeBag.getBag().setBag(data);
            Log.i(TAG, "dobag-实时数据:" + ByteUtil.toHexString(data) + "   " + RealTimeBag.getBag().getRealTimeSpeed());
            return  recei;
        }
        /**历史数据获取--start**/
        if (recei == BagCommand.BAG_RECEIVE_HISTORY_START || recei == BagCommand.BAG_RECEIVE_HISTORY_TRANSFER) {
            if (recei == BagCommand.BAG_RECEIVE_HISTORY_START) {
                Log.e(TAG,"getHistorySpeedData---------start");
                HistoryBagGroup.getBagGroup().clear();
            }
            HistoryBag bag_front = new HistoryBag();
            bag_front.setBag(data);
            if (bag_front.getDataNumber() == 0)//若数据量为0，则无需解析保存
                return recei;
            HistoryBag bag_after = new HistoryBag();
            bag_after.setBag(convertData2(data));
            HistoryBagGroup.getBagGroup().addBag(bag_front);
            HistoryBagGroup.getBagGroup().addBag(bag_after);
            Log.e(TAG, "getHistorySpeedData---------trancfer:"+ByteUtil.toHexString(data)+" 下一个运动时间段数据:"+ByteUtil.toHexString(convertData2(data)));
            return  recei;
        }
        /**历史数据获取--end**/
        if (recei == BagCommand.BAG_RECEIVE_HISTORY_END) {
            Log.e(TAG,"getHistorySpeedData----------------------------end------------------------>>>");
            List<HistoryBag> bags = HistoryBagGroup.getBagGroup().getBagList();
            for (int i = 0; i < bags.size(); i++) {
                Log.e(TAG,"HistoryBagGroup: "+i+" "+bags.get(i).toString());
            }
            Log.e(TAG,"getHistorySpeedData----------------------------end------------------------<<<");
            if (bags.size() == 0)
                return 0x00;//历史数据包为0时 则返回00 不上传
            return  recei;

        }
        /**同步时间返回数据**/
        if (recei == BagCommand.BAG_RECEIVE_SYNC_TIME) {
            byte[] bytes = new byte[4];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = data[i+1];
            }
            long L = Long.parseLong(ByteUtil.toHexString(bytes), 16);
            Log.e(TAG, "同步时间返回数据:" + ByteUtil.toHexString(data)+"   "+BagHandler.utc2Local2(L));
            return recei;
        }
        return  recei;
    }

    /**
     * Byte11，byte12，--byte18：下一个运动时间段数据
     * 截取data的后半段 作为下一个运动数据（因为一个数据包里面包含了两个历史数据）
     * @param data
     * @return
     */
    public static byte[] convertData2(byte[] data){
        byte[] data2 = new byte[10];
        data2[0] = data[0];
        data2[1]= data[1];
        for (int i = 10; i < 18; i++) {
            data2[i-8] = data[i];
        }
        return data2;
    }

    public static byte[] newByteArray(byte b0,byte b1){
        byte[] byteArray = new byte[2];
        /*由于下位机数据高低8位被互换，所以上位机需要再次互换过来*/
        byteArray[0] = b1;
        byteArray[1] = b0;
        return byteArray;
    }

    public static byte[] newByteArray(byte b0,byte b1,byte b2,byte b3){
        byte[] byteArray = new byte[4];
        /*由于下位机数据高低8位被互换，所以上位机需要再次互换过来*/
        byteArray[0] = b3;
        byteArray[1] = b2;
        byteArray[2] = b1;
        byteArray[3] = b0;
        return byteArray;
    }

    public static byte[] newByteArray(byte b0) {
        byte[] byteArray = {b0};
        return byteArray;
    }


    /**
     * Unix时间戳 --> 本地时间
     * @param utcTime
     */
    public static String utc2Local2(long utcTime) {
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").
                format(new java.util.Date(utcTime * 1000));
        return date;
    }

    public static long getCurrTime() {
        return  (System.currentTimeMillis() / 1000);
    }
}
