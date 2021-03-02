package com.bluetooth.bdsk.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bluetooth.bdsk.Constants;

import hr.drmic.db.dao.MeasurementDatabaseHelper;
import hr.drmic.db.model.Culture;
import hr.drmic.db.model.Measurement;

public class MainActivity extends AppCompatActivity {

    public static Culture culture = null;
    public static String deviceAddress = null;
    public static SharedPreferences sharedPreferences;
    public static SharedPreferences.Editor sharedPreferencesEditor;

    public static MeasurementDatabaseHelper measurementDB;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences(Constants.PARCEL_SHARED_PREFS, MODE_PRIVATE);
        sharedPreferencesEditor = sharedPreferences.edit();
        Measurement.setCurrentTime(sharedPreferences.getInt(Constants.PARCEL_LAST_MEASUREMENT_TIME, 0));

        startActivity(new Intent(MainActivity.this, BleChooserActivity.class));
        finish();
    }

}