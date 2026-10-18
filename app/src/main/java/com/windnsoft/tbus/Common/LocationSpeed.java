package com.windnsoft.tbus.Common;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.windnsoft.tbus.General.WsLog;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationSpeed implements LocationListener {
    public interface OnChangeSpeedListener {
        void onChangeSpeed(int speed);
    }

    OnChangeSpeedListener listener;
    LocationManager locationManager;
    boolean flagRunning = false;
    int minTime = 10000;
    int minDistance = 0;
    Location lastLocation;
    int speed;

    public static LocationSpeed instance;

    public static LocationSpeed getInstance() {
        if (instance == null)
            instance = new LocationSpeed();
        return instance;
    }

    private LocationSpeed() {
        locationManager = (LocationManager) BaseApplication.getApplication().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
    }


    public void setOnChangeSpeedListener(OnChangeSpeedListener listener) {
        this.listener = listener;
    }

    public OnChangeSpeedListener getOnChangeSpeedListener() {
        return listener;
    }

    public void start() {
        if (locationManager == null)
            locationManager = (LocationManager) BaseApplication.getApplication().getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, this);
        flagRunning = true;
    }

    public void stop() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        flagRunning = false;
    }

    public boolean isRunning() {
        return flagRunning;
    }

    @Override
    public void onLocationChanged(Location location) {
        setLastLocation(location);
        speed = Math.round(transKilometersPerHour(location.getSpeed()));
        if (listener != null)
            listener.onChangeSpeed(speed);

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }

    @Override
    public void onProviderEnabled(String provider) {
    }

    @Override
    public void onProviderDisabled(String provider) {
    }

    public static float transKilometersPerHour(float speed) {
        return Math.round((speed * 3600) / 1000);
    }

    void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation;
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public int getSpeed() {
        return speed;
    }

    public String getAddress() {
        if (lastLocation == null) {
            lastLocation = getLastBestLocation();
            if (lastLocation == null) return null;
        }
        String addressStr = "";
        Geocoder gcd = new Geocoder(BaseApplication.getApplication().getBaseContext(), Locale.getDefault());
        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(lastLocation.getLatitude(), lastLocation.getLongitude(), 2);
//            addresses = gcd.getFromLocation(37.566660, 126.977926, 10);
            WsLog.w("lat:" + lastLocation.getLatitude() + "lng:" + lastLocation.getLongitude());
            if (addresses.size() > 0) {
                Address address = addresses.get(addresses.size() - 1);
                if (address.getAdminArea() != null)
                    addressStr += address.getAdminArea();
                if (address.getLocality() != null)
                    addressStr += " " + address.getLocality();
                if (address.getThoroughfare() != null)
                    addressStr += " " + address.getThoroughfare();
                if (address.getFeatureName() != null && !address.getFeatureName().equals(address.getThoroughfare()))
                    addressStr += " " + address.getFeatureName();
            }

            for (Address a : addresses) {
                WsLog.w("address:" + a.toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        WsLog.w("address:" + addressStr);
        return addressStr;
    }

    public Location getLastBestLocation() {
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if (0 < GPSLocationTime - NetLocationTime) {
            return locationGPS;
        } else {
            return locationNet;
        }
    }
}
