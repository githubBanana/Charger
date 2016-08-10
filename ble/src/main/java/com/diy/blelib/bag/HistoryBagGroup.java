package com.diy.blelib.bag;

import java.util.ArrayList;
import java.util.List;

/**
 * @version V1.0 <历史数据包组>
 * @author: Xs
 * @date: 2016-04-19 13:49
 * @email Xs.lin@foxmail.com
 */
public class HistoryBagGroup {

    private static HistoryBagGroup instance = null;
    private HistoryBagGroup(){
        bagList = new ArrayList<>();
    }
    public static synchronized HistoryBagGroup getBagGroup() {
        if (instance == null)
            instance = new HistoryBagGroup();
        return instance;
    }
    List<HistoryBag> bagList;

    public List<HistoryBag> getBagList() {
        return bagList;
    }

    public void setBagList(List<HistoryBag> bagList) {
        this.bagList = bagList;
    }

    public void addBag(HistoryBag bag) {
        bagList.add(bag);
    }

    public void clear() {
        instance = null;
    }

}
