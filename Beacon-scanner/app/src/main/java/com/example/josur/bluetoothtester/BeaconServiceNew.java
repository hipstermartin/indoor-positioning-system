package com.example.josur.bluetoothtester;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class BeaconServiceNew extends Service implements RegionsBeaconService{

    private final IBinder mBinder = new LocalBinder();

    BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    List<BluetoothDevice> deviceList = new ArrayList<>();

    private Boolean enabled = false;

    BeaconListener listener;

    BluetoothAdapter.LeScanCallback callback;

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
            setCallback();


        }else {
            Log.i("no adapter","");
        }
    }
    private void setCallback(){
        callback = new BluetoothAdapter.LeScanCallback() {
            public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
                listener.scaning(true);
                device.getUuids();
                String Name;
                Name = (String)device.getName();

                int startByte = 2;
                boolean patternFound = false;
                while (startByte <= 5) {
                    if (    ((int) scanRecord[startByte + 2] & 0xff) == 0x02 && //Identifies an iBeacon
                            ((int) scanRecord[startByte + 3] & 0xff) == 0x15) { //Identifies correct data length
                        patternFound = true;
                        break;
                    }
                    startByte++;
                }

                if (patternFound) {
                    //Convert to hex String
                    byte[] uuidBytes = new byte[16];
                    System.arraycopy(scanRecord, startByte+4, uuidBytes, 0, 16);
                    String hexString = bytesToHex(uuidBytes);

                    //Here is your UUID
                    String uuid =  hexString.substring(0,8) + "-" +
                            hexString.substring(8,12) + "-" +
                            hexString.substring(12,16) + "-" +
                            hexString.substring(16,20) + "-" +
                            hexString.substring(20,32);

                    //Here is your Major value
                    int major = (scanRecord[startByte+20] & 0xff) * 0x100 + (scanRecord[startByte+21] & 0xff);

                    //Here is your Minor value
                    int minor = (scanRecord[startByte+22] & 0xff) * 0x100 + (scanRecord[startByte+23] & 0xff);
                    int txPower = (int)scanRecord[startByte+24];;//hard coded power value. Usually ranges between -59 to -65
                    double distance;
                    if (rssi == 0) {
                        distance= -1.0;
                    }

                    double ratio = rssi*1.0/txPower;
                    if (ratio < 1.0) {
                        distance=Math.pow(ratio,10);
                    }
                    else {
                        // distance = (0.51120) * Math.pow(ratio, 6.100) + 0.115;
                        distance=(0.882909233)*Math.pow((rssi/-58),4.57459326) + 0.045275821;
                    }

                    listener.beaconRecieved(uuid,minor,major,distance,Name,rssi);
                    Log.e("","");
                }



                Log.e("","");
            }
        };
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        BeaconServiceNew getService() {
            // Return this instance of LocalService so clients can call public methods
            return BeaconServiceNew.this;
        }
    }

    @Override
    public void manualScan(){
        if(enabled){
            mBluetoothAdapter.startLeScan(callback);

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    listener.scaning(false);
                    mBluetoothAdapter.stopLeScan(callback);
                }
            }, 4000);
        }
    }

    @Override
    public void setListener(BeaconListener listener) {
        this.listener = listener;
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
        enabled = true;
        setCallback();
    }

    static final char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


}
