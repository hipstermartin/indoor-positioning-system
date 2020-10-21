package com.example.josur.bluetoothtester;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BeaconServiceOld extends Service implements RegionsBeaconService{

    private final IBinder mBinder = new LocalBinder();

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    List<BluetoothDevice> deviceList = new ArrayList<>();

    private Boolean enabled = false;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {

        if (mBluetoothAdapter != null) {
            startBlueService();
        }

        return mBinder;
    }

    private void startBlueService(){
        if (mBluetoothAdapter.isEnabled()) {
            enabled = true;

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            //filter.addAction(BluetoothDevice.);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mReceiver, filter);
            mBluetoothAdapter.startDiscovery();

        }else {
            //should i ask the user to turn on bluetooth? maybe
            //Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //startActivityForResult(enableBtIntent, 6969);
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.e("","");
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address
                deviceList.add(device);
            }else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {

            }else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (deviceList.size() >0){
                    String deviceName = deviceList.get(0).getName();
                    String deviceHardwareAddress = deviceList.get(0).getAddress();
                    String a = deviceList.get(0).toString();
                    String b = deviceList.get(0).getBluetoothClass().toString();
                    Log.e("","");
                }
                Log.e("","");
            }
        }
    };

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        BeaconServiceOld getService() {
            // Return this instance of LocalService so clients can call public methods
            return BeaconServiceOld.this;
        }
    }

    public void manualScan(){
        if(enabled){
            mBluetoothAdapter.startLeScan(new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                    device.getUuids();
                    Log.e("","");
                }
            });
        }
    }

    @Override
    public void setListener(BeaconListener listener) {

    }

    @Override
    public boolean isBlueToothOn() {
        boolean on = false;

        if(mBluetoothAdapter != null){
            on = mBluetoothAdapter.isEnabled();
        }

        return on;
    }

    @Override
    public void restart() {
        startBlueService();
    }


}
