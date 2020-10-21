package com.example.josur.bluetoothtester;

public interface BeaconListener {
    void beaconRecieved(String uuid, int minor, int mayor,double distance,String name,int rssi);

    void scaning(Boolean scanning);
}
