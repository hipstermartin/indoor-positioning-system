package com.example.josur.bluetoothtester;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT = 312;
    RegionsBeaconService regionsNewBeaconService;

    Boolean mBeaconBound =  false;

    List<String> beacons = new ArrayList<>();

    LinearLayout linearLayout;

    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setListener();

        linearLayout = findViewById(R.id.beacon_list);

        Intent intent = new Intent(getApplicationContext(), BeaconServiceNew.class);
        getApplicationContext().bindService(intent,mConnection, Context.BIND_AUTO_CREATE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        66667);
            }
        }

    }

    private void setListener(){

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.INVISIBLE);

        findViewById(R.id.test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regionsNewBeaconService.manualScan();
            }
        });
    }


    /** Defines callbacks for service binding, passed to bindService() */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {

            if(service instanceof BeaconServiceOld.LocalBinder){
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                BeaconServiceOld.LocalBinder binder = (BeaconServiceOld.LocalBinder) service;
                regionsNewBeaconService = binder.getService();
                mBeaconBound = true;
            }else if(service instanceof BeaconServiceNew.LocalBinder){
                // We've bound to LocalService, cast the IBinder and get LocalService instance
                BeaconServiceNew.LocalBinder binder = (BeaconServiceNew.LocalBinder) service;
                regionsNewBeaconService = binder.getService();
                regionsNewBeaconService.setListener(new BeaconListener() {
                    @Override
                    public void beaconRecieved(String uuid, int minor, int mayor, double distance,String name,int rssi ) {
                        Log.e("","");
                        if(!beacons.contains(uuid)){
                            beacons.add(uuid);
                            View view = getLayoutInflater().inflate(R.layout.beacon,null);
                            ((TextView)view.findViewById(R.id.t8)).setText(""+name);
                            ((TextView)view.findViewById(R.id.t2)).setText(uuid);
                            ((TextView)view.findViewById(R.id.t4)).setText(""+minor);
                            ((TextView)view.findViewById(R.id.t6)).setText(""+mayor);
                            ((TextView)view.findViewById(R.id.t10)).setText(""+rssi);
                            ((TextView)view.findViewById(R.id.t12)).setText(""+distance);
                            linearLayout.addView(view);
                        }
                    }

                    @Override
                    public void scaning(Boolean scanning) {
                        if(scanning){
                            progressBar.setVisibility(View.VISIBLE);
                            ((TextView)findViewById(R.id.t_scanning)).setText("Scanning");
                        }else {
                            progressBar.setVisibility(View.INVISIBLE);
                            ((TextView)findViewById(R.id.t_scanning)).setText("Not Scanning");
                        }
                    }
                });
                if(!regionsNewBeaconService.isBlueToothOn()){
                    askForBlueTooth();
                }
                mBeaconBound = true;
            }


        }


        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBeaconBound = false;
        }
    };

    private void askForBlueTooth(){
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == REQUEST_ENABLE_BT){
            regionsNewBeaconService.restart();
        }
    }
}
