package com.windnsoft.tbus.General;

import android.content.Context;
import android.net.TrafficStats;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class WsTraffic {
    int uid;
    public static long startRx, startTx;

    public WsTraffic(Context context) {
        this.uid = context.getApplicationInfo().uid;
        reset();
    }

    public void reset() {
        startRx = TrafficStats.getUidRxBytes(uid);
        startTx = TrafficStats.getUidTxBytes(uid);
    }

    public long getCalcReciebedBytes() {
        return TrafficStats.getUidRxBytes(uid) - startRx;
    }

    public long getCalcSendBytes() {
        return TrafficStats.getUidTxBytes(uid) - startTx;
    }

    public long getCalcTrafficBytes() {
        return getCalcReciebedBytes() + getCalcSendBytes();
    }
}
