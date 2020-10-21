package com.example.josur.bluetoothtester;

import android.widget.LinearLayout;

public interface RegionsBeaconService {
    void manualScan();
    void setListener(BeaconListener listener);
    boolean isBlueToothOn();
    void restart();
}
