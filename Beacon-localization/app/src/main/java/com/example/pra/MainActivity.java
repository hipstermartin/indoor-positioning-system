package com.example.pra;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PointF;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.clj.fastble.BleManager;
import com.clj.fastble.callback.BleScanCallback;
import com.clj.fastble.data.BleDevice;
import com.clj.fastble.scan.BleScanRuleConfig;
import com.kiba.coordinateaxischart.ChartConfig;
import com.kiba.coordinateaxischart.CoordinateAxisChart;
import com.kiba.coordinateaxischart.SinglePoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static java.lang.StrictMath.max;

public class MainActivity extends AppCompatActivity {
    ArrayList<Integer> a = new ArrayList();

    // Allowed Bluetooth device MAC and number
    private HashMap<String, Integer> allowBluetoothDeviceMacs = new HashMap<String, Integer>(){{
        // Initialize the allowed Bluetooth devices
        put("73:0B:0F:4A:F6:29", 0);
//        put("EC:F4:A0:9B:56:23", 0);  // For test
        put("A0:E6:F8:69:90:E4", 1);
        put("A0:E6:F8:69:93:E5", 2);
    }};
    // Bluetooth device object
    private ArrayList<BleDevice> bluetoothDevices = new ArrayList<BleDevice>() {{
        add(null);
        add(null);
        add(null);
    }};

    private boolean [] bluetoothReadyStates = new boolean[] {false, false, false};

    // Try increasing the distance step
    private static final double TRY_DISTANCE_STEP = 0.01;

    private CardView bleCard1;
    private CardView bleCard2;
    private CardView bleCard3;

    private TextView bleDevice1Rssi;
    private TextView bleDevice2Rssi;
    private TextView bleDevice3Rssi;
    private EditText roomX;
    private EditText roomY;
    private Button refreshButton;
    private TextView location;
    private CoordinateAxisChart coordinateAxisChart;
    private FloatingActionButton fab;
    private CoordinatorLayout myCoordinatorLayout;
    private Snackbar snackbar;



    private void calculate() {
        // First determine if all three devices are ready
        for (int i = 0; i < 2; i++) {
            if (!bluetoothReadyStates[i]) {
                int number = i + 1;
//                Toast.makeText(getApplicationContext(), "Cannot get No." + number + " beacon's RSSI.\n Get location FAILED!",
//                        Toast.LENGTH_LONG).show();
                if(snackbar != null) snackbar.dismiss();
                snackbar.make(myCoordinatorLayout, "Cannot get No." + number + " beacon's RSSI.\nGet location FAILED!", Snackbar.LENGTH_INDEFINITE).show();
                return;
            }
        }

        // If all is ready，Then start calculating
        // Define the coordinates of three devices

        MathTool.Point device1Point, device2Point, device3Point;

        try {
            device1Point = new MathTool.Point(0, 0);
            device2Point = new MathTool.Point(Double.parseDouble(roomX.getText().toString()), 0);
            device3Point = new MathTool.Point(0, Double.parseDouble(roomY.getText().toString()));
        } catch (Exception e) {
            if(snackbar != null) snackbar.dismiss();
            snackbar.make(myCoordinatorLayout, "Invalid x or y range !", Snackbar.LENGTH_INDEFINITE).show();
            return;
        }

        // Convert all three rssi to actual distance
        double [] distances = new double[3];
        for (int i = 0; i < 3; i++) {
            switch (i) {
                default:
                    distances[i] = MathTool.rssiToDistance(bluetoothDevices.get(i).getRssi()) * Math.cos(Math.toRadians(45));
                    break;
            }
            Log.d("RSSI to distance : ", Double.toString(distances[i]));
        }

        // Abstract circle
        MathTool.Circle circle1 = new MathTool.Circle(
                new MathTool.Point(device1Point.x, device1Point.y),
                distances[0]);
        MathTool.Circle circle2 = new MathTool.Circle(
                new MathTool.Point(device2Point.x, device2Point.y),
                distances[1]
        );
        MathTool.Circle circle3 = new MathTool.Circle(
                new MathTool.Point(device3Point.x, device3Point.y),
                distances[2]
        );
        // Try to perform an operation
        while (true) {
            // First look at whether there are intersections between the three circles.
            // If 1、2 no intersection between the two circles
            if (!MathTool.isTwoCircleIntersect(circle1, circle2)) {
                // Try increasing the radius of a circle，Who is bigger and who increases
                if (circle1.r > circle2.r) {
                    circle1.r += TRY_DISTANCE_STEP;
                } else {
                    circle2.r += TRY_DISTANCE_STEP;
                }
                continue;
            }
            // If there is no intersection between the two circles of 1, 3
            if (!MathTool.isTwoCircleIntersect(circle1, circle3)) {
                // Try increasing the radius
                // If the radius of c3 is smaller than either of them
                if (circle3.r < circle1.r && circle3.r < circle2.r) {
                    circle1.r += TRY_DISTANCE_STEP;
                    circle2.r += TRY_DISTANCE_STEP;
                } else {
                    circle3.r += TRY_DISTANCE_STEP;
                }
                continue;
            }
            // If there is no intersection between the two originals
            if (!MathTool.isTwoCircleIntersect(circle2, circle3)) {
                // Try increasing the radius
                // If the radius of c3 is smaller than either of them
                if (circle3.r < circle1.r && circle3.r < circle2.r) {
                    circle1.r += TRY_DISTANCE_STEP;
                    circle2.r += TRY_DISTANCE_STEP;
                } else {
                    circle3.r += TRY_DISTANCE_STEP;
                }
                continue;
            }

            // When you try to find that the three circles have intersections, find the intersection between the two circles.
            MathTool.PointVector2 temp1 = MathTool.getIntersectionPointsOfTwoIntersectCircle(circle1, circle2);
            MathTool.PointVector2 temp2 = MathTool.getIntersectionPointsOfTwoIntersectCircle(circle2, circle3);
            MathTool.PointVector2 temp3 = MathTool.getIntersectionPointsOfTwoIntersectCircle(circle3, circle1);
            // The point where the intersection of the two circles of 1 and 2 takes y > 0
            MathTool.Point resultPoint1 = temp1.p1.y > 0 ?
                    new MathTool.Point(temp1.p1.x, temp1.p1.y):
                    new MathTool.Point(temp1.p2.x, temp1.p2.y);
            Log.d("resultPoint1", temp1.p1.toString() + "  " + temp1.p2.toString());
            // The intersection of 2, 3 and 2 circles takes the mean of the two
            MathTool.Point resultPoint2 = new MathTool.Point(
                    max(temp2.p1.x, temp2.p2.x),
                    max(temp2.p1.y, temp2.p2.y)
            );
            // 3, 1 the intersection of the two circles takes the point where x > 0
            MathTool.Point resultPoint3 = temp3.p1.x > 0 ?
                    new MathTool.Point(temp3.p1.x, temp3.p1.y):
                    new MathTool.Point(temp3.p2.x, temp3.p2.y);

            // Find the center point of three points
            MathTool.Point resultPoint = MathTool.getCenterOfThreePoint(
                    resultPoint1,
                    resultPoint2,
                    resultPoint3
            );

            Log.d("Location", resultPoint1.toString() + "  " + resultPoint2.toString() + "  " + resultPoint3.toString());

            // Update result display
            if(snackbar != null) snackbar.dismiss();
            snackbar.make(myCoordinatorLayout, "Get the location!", Snackbar.LENGTH_LONG).show();

            location.setText(resultPoint.toString());

            float x_float = (float)resultPoint.x;
            float y_float = (float)resultPoint.y;

            ChartConfig config = new ChartConfig();


            // the max value of the axis Maximum value of the axis
//            config.setMax(10);
            try {
                config.setMax(max(Integer.parseInt(roomX.getText().toString()), Integer.parseInt(roomY.getText().toString())));
            } catch (Exception e) {
//                Toast.makeText(getApplicationContext(), "Invalid x or y range !", Toast.LENGTH_SHORT).show();
                if(snackbar != null) snackbar.dismiss();
                snackbar.make(myCoordinatorLayout, "Invalid x or y range !", Snackbar.LENGTH_INDEFINITE).show();
                return;
            }
            config.setPrecision(1);
            config.setSegmentSize(50);

            coordinateAxisChart.setConfig(config);

            coordinateAxisChart.reset();
            coordinateAxisChart.invalidate();

            SinglePoint locPoint = new SinglePoint(new PointF(x_float, y_float));
            locPoint.setPointColor(Color.RED);
            coordinateAxisChart.addPoint(locPoint);

            float x_range_float, y_range_float;

            try {
                x_range_float = (float)Integer.parseInt(roomX.getText().toString());
                y_range_float = (float)Integer.parseInt(roomY.getText().toString());
            } catch (Exception e) {
                if(snackbar != null) snackbar.dismiss();
                snackbar.make(myCoordinatorLayout, "Invalid x or y range !", Snackbar.LENGTH_LONG).show();
                return;
            }

            SinglePoint ble1Point = new SinglePoint(new PointF(0, 0));
            SinglePoint ble2Point = new SinglePoint(new PointF(x_range_float, 0));
            SinglePoint ble3Point = new SinglePoint(new PointF(0, y_range_float));
            ble1Point.setPointColor(Color.GREEN);
            ble2Point.setPointColor(Color.GREEN);
            ble3Point.setPointColor(Color.GREEN);
            coordinateAxisChart.addPoint(ble1Point);
            coordinateAxisChart.addPoint(ble2Point);
            coordinateAxisChart.addPoint(ble3Point);

            coordinateAxisChart.invalidate();
            break;
        }
    }
    private void scan() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        bluetoothDevices.clear();
        for (int i = 0; i < 3; i++) bluetoothDevices.add(null);
        // Configuring scan rules
        BleManager.getInstance()
                .initScanRule(new BleScanRuleConfig.Builder()
                        .setAutoConnect(false)
                        .setScanTimeOut(1000)
                        .build()
                );
        // Turn on Bluetooth
        BleManager.getInstance().enableBluetooth();
        // Start scanning
        BleManager.getInstance().scan(new BleScanCallback() {
            @Override
            public void onScanFinished(List<BleDevice> scanResultList) {
//                Toast.makeText(getApplicationContext(), "Finish Scanning!", Toast.LENGTH_SHORT)
//                        .show();
                fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.colorAccent)));
                fab.setClickable(true);
                calculate();
            }

            @Override
            public void onScanStarted(boolean success) {
                if(snackbar != null) snackbar.dismiss();
                snackbar.make(myCoordinatorLayout, "Locating...", Snackbar.LENGTH_INDEFINITE).show();
                fab.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.disabled)));
                fab.setClickable(false);
            }

            @Override
            public void onScanning(final BleDevice bleDevice) {
                // If you have scanned a new device
                // See if he is pre-set by several devices
                for (String mac : allowBluetoothDeviceMacs.keySet()) {
                    Log.d("BL", "get an bl device");
                    // if
                    if (mac.equals(bleDevice.getMac())) {
                        // Get index
                        int index = allowBluetoothDeviceMacs.get(mac);
                        // Add the device to the list
                        bluetoothDevices.remove(index);
                        bluetoothDevices.add(index, bleDevice);

                        // Update display status
                        bluetoothReadyStates[index] = true;
                        switch (index) {
                            case 0:
//                                bleDevice1Ready.setText("Ready");
                                bleCard1.setCardBackgroundColor(getResources().getColor(R.color.success));
                                bleDevice1Rssi.setText(String.valueOf(bleDevice.getRssi()));
                                break;
                            case 1:
//                                bleDevice2Ready.setText("Ready");
                                bleCard2.setCardBackgroundColor(getResources().getColor(R.color.success));
                                bleDevice2Rssi.setText(String.valueOf(bleDevice.getRssi()));
                                break;
                            case 2:
//                                bleDevice3Ready.setText("Ready");
                                bleCard3.setCardBackgroundColor(getResources().getColor(R.color.success));
                                bleDevice3Rssi.setText(String.valueOf(bleDevice.getRssi()));
                                break;
                            default:
                                break;
                        }
                    }
                }
            }
        });
    }

    private void bindComponent() {

        bleCard1 = (CardView)findViewById(R.id.bleCard1);
        bleCard2 = (CardView)findViewById(R.id.bleCard2);
        bleCard3 = (CardView)findViewById(R.id.bleCard3);

        bleDevice1Rssi = findViewById(R.id.bleDevice1_rssi);
        bleDevice2Rssi = findViewById(R.id.bleDevice2_rssi);
        bleDevice3Rssi = findViewById(R.id.bleDevice3_rssi);
        roomX = findViewById(R.id.room_x);
        roomY = findViewById(R.id.room_y);
//        refreshButton = findViewById(R.id.refresh_button);
//        calculateButton = findViewById(R.id.calculate_button);
        location = findViewById(R.id.location);

        coordinateAxisChart = (CoordinateAxisChart)findViewById(R.id.coordinateAxisChart);

        fab = (FloatingActionButton)findViewById(R.id.fab);
        myCoordinatorLayout = (CoordinatorLayout)findViewById(R.id.myCoordinatorLayout);





        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                for (int i = 0; i < 2; i++) {
                    bluetoothReadyStates[i] = false;
                }
//                bleDevice1Ready.setText("Not ready");
                bleCard1.setCardBackgroundColor(getResources().getColor(R.color.error));
                bleDevice1Rssi.setText("N/A");
//                bleDevice2Ready.setText("Not ready");
                bleCard2.setCardBackgroundColor(getResources().getColor(R.color.error));
                bleDevice2Rssi.setText("N/A");
//                bleDevice3Ready.setText("Not ready");
                bleCard3.setCardBackgroundColor(getResources().getColor(R.color.error));
                bleDevice3Rssi.setText("N/A");
                // Start a new round of scanning
                /* Declaring array of n elements, the value
                 * of n is provided by the user
                 */
                scan();
// for automation
//                final ExecutorService es = Executors.newCachedThreadPool();
//                ScheduledExecutorService ses = Executors.newSingleThreadScheduledExecutor();
//                ses.scheduleAtFixedRate(new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        es.submit(new Runnable()
//                        {
//                            @Override
//                            public void run()
//                            {
//                                scan();
//                            }
//                        });
//
//                    }
//                }, 0, 9, TimeUnit.SECONDS);
            }
        });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BleManager.getInstance().init(getApplication());
        bindComponent();
    }
}
