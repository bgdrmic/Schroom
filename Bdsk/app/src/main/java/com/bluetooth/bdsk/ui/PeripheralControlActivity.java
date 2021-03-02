package com.bluetooth.bdsk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.os.Message;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

import hr.drmic.db.model.Culture;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Line;
import lecho.lib.hellocharts.model.LineChartData;
import lecho.lib.hellocharts.model.PointValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.view.LineChartView;

import com.bluetooth.bdsk.Constants;
import com.bluetooth.bdsk.R;
import com.bluetooth.bdsk.bluetooth.BleAdapterService;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.concurrent.TimeUnit;

import android.os.Handler;
import android.widget.SeekBar;
import android.widget.TextView;

import hr.drmic.db.model.Measurement;
import hr.drmic.db.dao.MeasurementDatabaseHelper;

public class PeripheralControlActivity extends Activity {

    private BleAdapterService bluetooth_le_adapter;
    private String[] chartOrder = {"soilHumidity", "airTemperature", "ambientLight", "airHumidity", "soilPH", "airPressure"};

    private boolean back_requested = false;
    private String device_address;
    private int position = 1;
    private SharedPreferences sharedPreferences = MainActivity.sharedPreferences;
    private SharedPreferences.Editor sharedPreferencesEditor = MainActivity.sharedPreferencesEditor;
    private MeasurementDatabaseHelper db = new MeasurementDatabaseHelper(this);


    private SeekBar periodLengthSeekBar;

    class MyGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if(e2.getX() > e1.getX()) {
                if(position > 1) {
                    position--;
                    ((TextView)findViewById(R.id.chartNameLabel)).setText(chartOrder[position-1]);
                    drawChart();
                }

            } else if(e1.getX() > e2.getX()) {
                if(position < 6) {
                    position++;
                    ((TextView)findViewById(R.id.chartNameLabel)).setText(chartOrder[position-1]);
                    drawChart();
                }
            }

            return true;
        }
    }

    private final ServiceConnection service_connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            bluetooth_le_adapter = ((BleAdapterService.LocalBinder) service).getService();
            bluetooth_le_adapter.setActivityHandler(message_handler);
            onConnect();
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetooth_le_adapter = null;
        }
    };

    @SuppressLint("HandlerLeak")
    private final Handler message_handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            Bundle bundle;
            String characteristic_uuid;
            byte[] b;

            // message handling logic
            switch (msg.what) {
                case BleAdapterService.MESSAGE:
                    bundle = msg.getData();
                    String text = bundle.getString(BleAdapterService.PARCEL_TEXT);
                    showMsg(text);
                    break;
                case BleAdapterService.GATT_CONNECTED:
                    bluetooth_le_adapter.discoverServices();
                    break;
                case BleAdapterService.GATT_DISCONNECT:
                    // we're disconnected
                    showMsg("DISCONNECTED");
                    findViewById(R.id.menuBar).setBackgroundColor(getResources().getColor(R.color.colorDisconnected));
                    if (back_requested) {
                        PeripheralControlActivity.this.finish();
                    }
                    break;
                case BleAdapterService.GATT_SERVICES_DISCOVERED:
                    // validate services and if ok...
                    List<BluetoothGattService> slist = bluetooth_le_adapter.getSupportedGattServices();

                    boolean measurement_service_present = false;

                    for (BluetoothGattService svc : slist) {
                        Log.d(Constants.TAG, "UUID=" + svc.getUuid().toString().toUpperCase() + " INSTANCE=" + svc.getInstanceId());

                        if (svc.getUuid().toString().equalsIgnoreCase(BleAdapterService.MEASUREMENT_SERVICE_UUID)) {
                            measurement_service_present = true;
                            break;
                        }
                    }

                    if (measurement_service_present) {
                        bluetooth_le_adapter.readCharacteristic(
                                BleAdapterService.MEASUREMENT_SERVICE_UUID, BleAdapterService.MEASUREMENT_CHARACTERISTIC_UUID
                        );

                        bluetooth_le_adapter.setIndicationsState(BleAdapterService.MEASUREMENT_SERVICE_UUID,
                                BleAdapterService.MEASUREMENT_CHARACTERISTIC_UUID, true);
                    } else {
                        showMsg("Device not compatible. Choose other device.");
                        startActivity(new Intent(PeripheralControlActivity.this, BleChooserActivity.class));
                        finish();
                    }

                    showMsg("CONNECTED");
                    findViewById(R.id.menuBar).setBackgroundColor(getResources().getColor(R.color.colorConnected));

                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    boolean firstTimeConnected = sharedPreferences.getBoolean(Constants.PARCEL_FIRST_TIME_CONNECTED, true);
                    if(firstTimeConnected) {
                        sharedPreferencesEditor.putBoolean(Constants.PARCEL_FIRST_TIME_CONNECTED, false);
                        sendData(BleAdapterService.MEASUREMENT_SERVICE_UUID, BleAdapterService.MEASUREMENT_CHARACTERISTIC_UUID, "#RESET#");
                    }

                    sharedPreferencesEditor.putBoolean(Constants.PARCEL_CULTURE_CHANGE_NOTIFIED, false);
                    sharedPreferencesEditor.apply();
                    notifyCultureChange();

                    sendData(BleAdapterService.MEASUREMENT_SERVICE_UUID, BleAdapterService.MEASUREMENT_CHARACTERISTIC_UUID, "#READY#");
                    break;
                case BleAdapterService.NOTIFICATION_OR_INDICATION_RECEIVED:
                    bundle = msg.getData();
                    characteristic_uuid = bundle.getString(BleAdapterService.PARCEL_CHARACTERISTIC_UUID);
                    b = bundle.getByteArray(BleAdapterService.PARCEL_VALUE);
                    assert characteristic_uuid != null;
                    if (characteristic_uuid.equalsIgnoreCase((BleAdapterService.MEASUREMENT_CHARACTERISTIC_UUID))) {
                        readData(b);
                        showResults();
                        sendData(BleAdapterService.MEASUREMENT_SERVICE_UUID, BleAdapterService.MEASUREMENT_CHARACTERISTIC_UUID, "#OK#");
                        notifyCultureChange();
                    }
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_peripheral_control);

        device_address = MainActivity.deviceAddress;
        MainActivity.measurementDB = db;

        setGraphics();

        // connect to the Bluetooth adapter service
        Intent gattServiceIntent = new Intent(this, BleAdapterService.class);
        bindService(gattServiceIntent, service_connection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        db.close();
        unbindService(service_connection);
        bluetooth_le_adapter = null;
        super.onDestroy();
    }

    public void onReconnect(View view) {
        view.setClickable(false);
        if(!bluetooth_le_adapter.isConnected()) {
            onConnect();
        } else {
            sendData(BleAdapterService.MEASUREMENT_SERVICE_UUID, BleAdapterService.MEASUREMENT_CHARACTERISTIC_UUID, "#READY#");
        }
    }

    public void onConnect() {
        if (bluetooth_le_adapter != null) {
            if (!bluetooth_le_adapter.connect(device_address)) {
                showMsg("Failed to connect. Out of reach");
            }
        } else {
            showMsg("Turn on Bluetooth and Enable Location");
        }
    }

    private void setGraphics() {
        // set analysisView
        analyzeData();

        // set chart
        final GestureDetector gestureDetector = new GestureDetector(new MyGestureDetector());

        LineChartView chart = findViewById(R.id.chart);
        chart.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (gestureDetector.onTouchEvent(event)) {
                    return true;
                }
                return false;
            }
        });

        periodLengthSeekBar = this.findViewById(R.id.seekBar);
        periodLengthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                drawChart();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        drawChart();
    }

    private void showMsg(final String msg) {
        Log.d(Constants.TAG, msg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.msgTextView)).setText(msg);
            }
        });
    }

    public void openSettings(View view) {
        Intent intent = new Intent(PeripheralControlActivity.this, SettingsActivity.class);
        startActivity(intent);
    }

    public void onBackPressed() {
        Log.d(Constants.TAG, "onBackPressed");
        back_requested = true;
        if (bluetooth_le_adapter.isConnected()) {
            try {
                bluetooth_le_adapter.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            finish();
        }
    }

    private void readData(byte[] b) {
        try {
            ByteBuffer buffer = ByteBuffer.wrap(b, 0, 4).order(ByteOrder.LITTLE_ENDIAN);
            double airPressure = buffer.getInt() / 10000.0; // u hPa
            if(airPressure < 500 || airPressure > 1500) {
                return;
            }

            buffer = ByteBuffer.wrap(b, 4, 4).order(ByteOrder.LITTLE_ENDIAN);
            double airTemperature = buffer.getInt() / 100.0;
            if(airTemperature > 60 || airTemperature < -50) {
                return;
            }

            buffer = ByteBuffer.wrap(b, 8, 4).order(ByteOrder.LITTLE_ENDIAN);
            double airHumidity = buffer.getInt() / 1024.0;

            buffer = ByteBuffer.wrap(b, 12, 4).order(ByteOrder.LITTLE_ENDIAN);
            double ambientLight = buffer.getInt();
            if(ambientLight < 135) {
                ambientLight /= 300.0;
            } else {
                ambientLight = 300 * Math.exp(0.00144 * ambientLight);
            }

            double coefficient = Constants.SUNSHINE_COEFFICIENT;
            if(sharedPreferences.getBoolean(Constants.PARCEL_ARTIFICIAL_LIGHT_SOURCE, false)) {
                coefficient = Constants.HPS_COEFFICIENT;
            }
            ambientLight *= coefficient * Constants.PAR_TO_DLI_CONST * Constants.LUX_TO_FOOT_CANDEL;
            if(ambientLight > 300 || ambientLight < -100) {
                return;
            }

            buffer = ByteBuffer.wrap(b, 16, 4).order(ByteOrder.LITTLE_ENDIAN);
            double soilHumidity = (double) buffer.getInt() * 100 / Constants.MAX_SIGNAL;
            if(soilHumidity > 100 || soilHumidity < -100) {
                return;
            }

            buffer = ByteBuffer.wrap(b, 20, 4).order(ByteOrder.LITTLE_ENDIAN);
            double soilPH = 7 - (3.5 * buffer.getInt() / (double) Constants.MAX_SIGNAL);
            if(soilPH > 30 || soilPH < -20) {
                return;
            }

            Measurement measurement = new Measurement(airPressure, airTemperature, airHumidity, ambientLight, soilHumidity, soilPH);
            db.addMeasurement(measurement);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendData(String serviceUUID, String characteristicUUID, String data) {
        sendData(serviceUUID, characteristicUUID, data.getBytes());
    }

    private void sendData(String serviceUUID, String characteristicUUID, byte[] data) {
        byte[] b = {0x00};
        for(int i = 0; i < data.length; i++) {
            b[0] = data[i];
            bluetooth_le_adapter.writeCharacteristic(serviceUUID, characteristicUUID, b);
            try {
                TimeUnit.MILLISECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void notifyCultureChange() {
        boolean notified = sharedPreferences.getBoolean(Constants.PARCEL_CULTURE_CHANGE_NOTIFIED, true);
        if(notified) {
            return;
        }

        sharedPreferencesEditor.putBoolean(Constants.PARCEL_CULTURE_CHANGE_NOTIFIED, true);
        sharedPreferencesEditor.apply();


        // pošalji kritičnu vrijednost za vlažnost tla
        String lowHumidityValue = String.valueOf(((Double) (MainActivity.culture.getLowSoilHumidity() * Constants.MAX_SIGNAL / 100)).intValue());
        while(lowHumidityValue.length() < 4) {
            lowHumidityValue = "0" + lowHumidityValue;
        }
        sendData(BleAdapterService.MEASUREMENT_SERVICE_UUID, BleAdapterService.MEASUREMENT_CHARACTERISTIC_UUID, "#SH" + lowHumidityValue + "#");
    }

    private void showResults() {
        analyzeData();
        drawChart();
    }

    private void analyzeData() {
        Culture culture = MainActivity.culture;

        // soil humidity
        int count = 1;
        ArrayList<Measurement> m = db.getLatestMeasurements(count);
        TextView view = findViewById(R.id.soilHumidityView);
        String msg ="Soil humidity is OK";
        if(culture.getLowSoilHumidity() == null && culture.getHighSoilHumidity() == null) {
            msg = "Not available";
        } else if(m.size() < count) {
            msg ="Not enough data";
        } else {
            double avg = avg(m, "soilHumidity");
            if(culture.getLowSoilHumidity() != null && avg < culture.getLowSoilHumidity()) {
                msg = "Soil is too dry";
            } else if(culture.getHighSoilHumidity() != null && avg > culture.getHighSoilHumidity()) {
                msg = "Soil is too humid";
            }
        }
        setText(view, msg);

        // air temperature
        count = 3 * Measurement.getMeasurementsPerDay();
        m = db.getLatestMeasurements(count);
        view = findViewById(R.id.airTemperatureView);
        msg = "Air temperature is OK";
        if(culture.getLowAirTemperature() == null && culture.getHighAirTemperature() == null) {
            msg = "Not available";
        } else if(m.size() < count) {
            msg ="Not enough data";
        } else {
            double avg = avg(m, "airTemperature");
            if(culture.getLowAirTemperature() != null && avg < culture.getLowAirTemperature()) {
                msg = "It is too cold";
            } else if(culture.getHighAirTemperature() != null && avg > culture.getHighAirTemperature()) {
                msg = "It is too hot";
            }
        }
        setText(view, msg);

        // ambient light
        count = 3 * Measurement.getMeasurementsPerDay();
        m = db.getLatestMeasurements(count);
        view = findViewById(R.id.ambientLightView);
        msg = "Just enough sunshine";
        if(culture.getLowAmbientLight() == null && culture.getHighAmbientLight() == null) {
            msg = "Not available";
        } else if(m.size() < count) {
            msg ="Not enough data";
        } else {
            double avg = avg(m, "ambientLight");
            if(culture.getLowAmbientLight() != null && avg < culture.getLowAmbientLight()) {
                msg = "Too little sunshine";
            } else if(culture.getHighAmbientLight() != null && avg > culture.getHighAmbientLight()) {
                msg = "Too much sunshine";
            }
        }
        setText(view, msg);


        // air humidity
        count = 3 * Measurement.getMeasurementsPerDay();
        m = db.getLatestMeasurements(count);
        view = findViewById(R.id.airHumidityView);
        msg = "Air humidity is OK";
        if(culture.getLowAirHumidity() == null && culture.getHighAirHumidity() == null) {
            msg = "Not available";
        } else if(m.size() < count) {
            msg ="Not enough data";
        } else {
            double avg = avg(m, "airHumidity");
            if(culture.getLowAirHumidity() != null && avg < culture.getLowAirHumidity()) {
                msg = "Air is too dry";
            } else if(culture.getHighAirHumidity() != null && avg > culture.getHighAirHumidity()) {
                msg = "Air is too humid";
            }
        }
        setText(view, msg);

        // soil pH
        count = 3;
        m = db.getLatestMeasurements(count);
        view = findViewById(R.id.soilPHView);
        msg = "Good soil acidity";
        if(culture.getLowSoilPH() == null && culture.getHighSoilPH() == null) {
            msg = "Not available";
        } else if(m.size() < count) {
            msg ="Not enough data";
        } else {
            double avg = avg(m, "soilPH");
            if(culture.getLowSoilPH() != null && avg < culture.getLowSoilPH()) {
                msg = "Too high soil acidity";
            } else if(culture.getHighSoilPH() != null && avg > culture.getHighSoilPH()) {
                msg = "Too low soil acidity";
            }
        }
        setText(view, msg);
    }

    private double avg(ArrayList<Measurement> measurements, String param) {
        if(measurements.size() == 0) {
            return 0.0;
        }

        Double avg = 0.0;
        for(Measurement m : measurements) {
            if(m == null) {
                continue;
            }

            switch (param) {
                case "soilHumidity":
                    avg += m.getSoilHumidity();
                    break;
                case "airHumidity":
                    avg += m.getAirHumidity();
                    break;
                case "airTemperature":
                    avg += m.getAirTemperature();
                    break;
                case "soilPH":
                    avg += m.getSoilPH();
                    break;
                case "airPressure":
                    avg += m.getAirPressure();
                    break;
                case "ambientLight":
                    avg += m.getAmbientLight();
            }
        }

        return avg / (double) measurements.size();
    }

    private void setText(TextView view, String msg) {
        view.setText(msg);
    }

    private void drawChart() {
        // set data
        int days = periodLengthSeekBar.getProgress();

        if(days == 0) {
            days = 1;
        }

        LineChartView chart = findViewById(R.id.chart);

        String yAxisName = " ";
        int numberOfNodes = 24;
        if(days < 3) {
            numberOfNodes = 8;
        }
        else if(days < 7) {
            numberOfNodes = 12;
        } else if(days < 14) {
            numberOfNodes = 16;
        }

        String[] xAxisData = getXAxisData(days, numberOfNodes);
        double[] yAxisData = getYAxisData(days, numberOfNodes);


        // draw chart
        ArrayList<PointValue> yAxisValues = new ArrayList<>();
        ArrayList<AxisValue> xAxisValues = new ArrayList<>();

        Line line = new Line(yAxisValues).setColor(getResources().getColor(R.color.colorDisconnected));

        for (int i = 0; i < xAxisData.length; i++) {
            xAxisValues.add(i, new AxisValue(i).setLabel(xAxisData[i]));
        }

        for (int i = 0; i < yAxisData.length; i++) {
            yAxisValues.add(new PointValue(i, (float) yAxisData[i]));
        }

        ArrayList<Line> lines = new ArrayList<>();
        lines.add(line);

        LineChartData data = new LineChartData();
        data.setLines(lines);

        Axis xAxis = new Axis();
        xAxis.setValues(xAxisValues);
        xAxis.setTextSize(16);
        xAxis.setTextColor(Color.BLACK);
        data.setAxisXBottom(xAxis);

        Axis yAxis = new Axis();
        yAxis.setName(yAxisName);
        yAxis.setTextColor(Color.BLACK);
        yAxis.setTextSize(16);
        data.setAxisYLeft(yAxis);

        chart.setLineChartData(data);
        Viewport viewport = new Viewport(chart.getMaximumViewport());

        switch (chartOrder[position-1]) {
            case "soilHumidity":
            case "airHumidity":
                viewport.bottom = 0;
                viewport.top = 105;
                break;
            case "airTemperature":
                viewport.bottom = -20;
                viewport.top = 45;
                break;
            case "soilPH":
                viewport.bottom = 0;
                viewport.top = 15;
                break;
            case "airPressure":
                viewport.bottom = 880;
                viewport.top = 1120;
                break;
            case "ambientLight":
                viewport.bottom = 0;
                viewport.top = 45;
                break;
        }

        chart.setMaximumViewport(viewport);
        chart.setCurrentViewport(viewport);
    }


    private String[] getXAxisData(int days, int numberOfNodes) {
        String[] s = new String[numberOfNodes];
        for(int i = 0; i < s.length; i++) {
            int h = (numberOfNodes - i) * 24 * days / numberOfNodes;
            if(h/24 == 0) {
                s[i] = h + "h";
            } else if(h%24 == 0) {
                s[i] = h/24 + "d";
            } else {
                s[i] = h/24 + "d" + h%24 + "h";
            }
        }

        return s;
    }

    private double[] getYAxisData(int days, int numberOfNodes) {
        ArrayList<Measurement> m = db.getLatestMeasurements(days * Measurement.getMeasurementsPerDay());
        while (m.size() < days * Measurement.getMeasurementsPerDay()) {
            m.add(null);
        }

        int count = m.size() / numberOfNodes;
        double[] yAxisData = new double[numberOfNodes];

        for(int i = 0; i < numberOfNodes; i++) {
            yAxisData[yAxisData.length - 1 - i] = avg(new ArrayList<>(m.subList(i*count, (i+1)*count)), chartOrder[position - 1]);
        }

        return yAxisData;
    }
}