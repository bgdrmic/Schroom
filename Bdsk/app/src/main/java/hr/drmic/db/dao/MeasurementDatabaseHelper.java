package hr.drmic.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;

import hr.drmic.db.model.Measurement;

public class MeasurementDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "MeasurementsDatabase";

    private static final String TABLE_NAME = "measurements";
    private static final String ID = "ID";
    private static final String TIME = "time";
    private static final String AIR_PRESSURE = "airPressure";
    private static final String AIR_TEMPERATURE = "airTemperature";
    private static final String AIR_HUMIDITY = "airHumidity";
    private static final String AMBIENT_LIGHT = "ambientLight";
    private static final String SOIL_HUMIDITY = "soilHumidity";
    private static final String SOIL_PH = "soilPH";

    public MeasurementDatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TIME + " INTEGER, " + AIR_PRESSURE + " REAL, " + AIR_TEMPERATURE + " REAL," + AIR_HUMIDITY + " REAL, "
                + AMBIENT_LIGHT + " REAL, " + SOIL_HUMIDITY + " REAL, " + SOIL_PH + " REAL)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addMeasurement(Measurement measurement) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(TIME, measurement.getTime());
        contentValues.put(AIR_PRESSURE, measurement.getAirPressure());
        contentValues.put(AIR_TEMPERATURE, measurement.getAirTemperature());
        contentValues.put(AIR_HUMIDITY, measurement.getAirHumidity());
        contentValues.put(AMBIENT_LIGHT, measurement.getAmbientLight());
        contentValues.put(SOIL_HUMIDITY, measurement.getSoilHumidity());
        contentValues.put(SOIL_PH, measurement.getSoilPH());

        Log.d(TAG, "addMeasurement: Adding another measurement to " + TABLE_NAME);
        db.insert(TABLE_NAME, null, contentValues);
    }

    public ArrayList<Measurement> getLatestMeasurements(int count){
        ArrayList<Measurement> measurements = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME + " WHERE " + TIME + " >=?  ORDER BY " + TIME + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, new String[]{String.valueOf(Measurement.getCurrentTime() - count)});

        if(cursor.moveToFirst()) {
            do {
                measurements.add(readMeasurement(cursor));
            } while(cursor.moveToNext());
        }

        return measurements;
    }

    private Measurement readMeasurement(Cursor cursor) {
        return new Measurement(cursor.getInt(0), cursor.getInt(1), cursor.getDouble(2), cursor.getDouble(3),
                cursor.getDouble(4), cursor.getDouble(5), cursor.getDouble(6), cursor.getDouble(7));
    }

    public void reset() {
        String clearDBQuery = "DELETE FROM " + TABLE_NAME;
        getWritableDatabase().execSQL(clearDBQuery);
        Measurement.setCurrentTime(0);
    }
}