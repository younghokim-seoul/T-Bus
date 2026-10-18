package com.windnsoft.tbus.Common;

import android.util.Log;

/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class DataUsage {
    Thread dataUsage;
    boolean flagContinue = true;

    public DataUsage() {
        dataUsage = new Thread() {
            @Override
            public void run() {
                while (!interrupted() && flagContinue) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    Log.e("bytes recvd", "" + android.net.TrafficStats.getMobileRxBytes());
                    Log.e("Total", "Bytes received" + android.net.TrafficStats.getTotalRxBytes());
                }
            }
        };
    }

    public void start() {
        dataUsage.start();
    }

    public void stop(){
        flagContinue = false;
    }
}
