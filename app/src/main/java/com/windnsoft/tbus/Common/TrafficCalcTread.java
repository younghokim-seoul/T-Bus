package com.windnsoft.tbus.Common;

import android.app.Activity;
import android.widget.TextView;

import com.windnsoft.tbus.General.WsTraffic;


/**
 * Created by Windn(Windnsoft@gmail.com) on 2016.
 */
public class TrafficCalcTread extends Thread {
    Activity activity;
    WsTraffic traffic;
    TextView outputViewTotal;
    TextView outputViewPerMin;
    TextView outputViewTime;
    boolean stopSign;
    long startTime, pastTime, totalTime;
    boolean streaming;

    public TrafficCalcTread(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void run() {
        super.run();


        while (!interrupted() && !stopSign) {
            try {
                Thread.sleep(1000);
                if (!streaming) continue;

                if (traffic == null)
                    traffic = new WsTraffic(activity);

                totalTime = (System.currentTimeMillis() - startTime + pastTime) / 1000;

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        long trafficBytes = traffic.getCalcTrafficBytes();
                        outputViewTime.setText("통화시간: " + totalTime + "초");
                        outputViewTotal.setText("총 " + trafficBytes / 1024 + "KB");
                        if (totalTime != 0)
                            outputViewPerMin.setText("분당 " + trafficBytes / 1024 / totalTime * 60 + "KB");
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void stopRunning() {
        stopSign = true;
        interrupt();
    }

    public void notifyStreamingStart() {
        startTime = System.currentTimeMillis();
        streaming = true;
    }

    public void notifyStreamingEnd() {
        pastTime += System.currentTimeMillis() - startTime;
        streaming = false;
    }

    public void resetValue() {
        pastTime = totalTime = 0;
        startTime = System.currentTimeMillis();
        if(traffic!=null)
            traffic.reset();
    }
}
