package hr.drmic.db.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;

import hr.drmic.db.model.Culture;

public class CultureDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "CultureDatabase";

    private static final String TABLE_NAME = "cultures";

    private static final String ID = "ID";
    private static final String NAME = "name";

    private static final String LOW_AIR_TEMPERATURE = "lowAirTemperature";
    private static final String HIGH_AIR_TEMPERATURE = "highAirTemperature";

    private static final String LOW_AIR_HUMIDITY = "lowAirHumidity";
    private static final String HIGH_AIR_HUMIDITY = "highAirHumidity";

    private static final String LOW_AMBIENT_LIGHT = "lowAmbientLight";
    private static final String HIGH_AMBIENT_LIGHT = "highAmbientLight";

    private static final String LOW_SOIL_HUMIDITY = "lowSoilHumidity";
    private static final String HIGH_SOIL_HUMIDITY = "highSoilHumidity";

    private static final String LOW_SOIL_PH = "lowSoilPH";
    private static final String HIGH_SOIL_PH = "highSoilPH";

    private Context context;

    public CultureDatabaseHelper(Context context) {
        super(context, TABLE_NAME, null, 1);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_NAME + " (" + ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NAME + " TEXT, " + LOW_AIR_TEMPERATURE + " REAL, " + HIGH_AIR_TEMPERATURE + " REAL, " + LOW_AIR_HUMIDITY + " REAL, " + HIGH_AIR_HUMIDITY + " REAL, "
                + LOW_AMBIENT_LIGHT + " REAL, " + HIGH_AMBIENT_LIGHT + " REAL, " + LOW_SOIL_HUMIDITY + " REAL, " + HIGH_SOIL_HUMIDITY + " REAL, "
                + LOW_SOIL_PH + " REAL, " + HIGH_SOIL_PH + " REAL)";
        db.execSQL(createTable);
        initializeDatabase(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    private void initializeDatabase(SQLiteDatabase db) {
        Log.d(TAG, "initializeDatabase: initializing database.");
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(context.getAssets().open("cultures.txt")));
            String line;

            while((line = reader.readLine()) != null) {
                try {
                    Culture culture = new Culture(line);
                    addCulture(culture, db);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            Log.d(TAG, "initializeDatabase: initialization was successful.");
        } catch (Exception e) {
            Log.d(TAG, "initializeDatabase: initialization failed.");
        }
    }

    public void addCulture(Culture culture, SQLiteDatabase db) {
        if(db == null) {
            db = this.getWritableDatabase();
        }

        ContentValues contentValues = prepareContentValues(culture);

        Log.d(TAG, "addData: Adding "  + culture.getName() + " to " + TABLE_NAME);
        db.insert(TABLE_NAME, null, contentValues);
    }

    public Culture getCulture(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[] {ID, NAME, LOW_AIR_TEMPERATURE, HIGH_AIR_TEMPERATURE, LOW_AIR_HUMIDITY, HIGH_AIR_HUMIDITY,
                        LOW_AMBIENT_LIGHT, HIGH_AMBIENT_LIGHT, LOW_SOIL_HUMIDITY, HIGH_SOIL_HUMIDITY, LOW_SOIL_PH, HIGH_SOIL_PH}, ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        if(cursor != null) {
            cursor.moveToFirst();
            return extractCulture(cursor);
        }

        return null;
    }

    public ArrayList<Culture> getAllCultures(){
        ArrayList<Culture> cultures = new ArrayList<>();

        String selectQuery = "SELECT * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if(cursor.moveToFirst()) {
            do {
                cultures.add(extractCulture(cursor));
            } while(cursor.moveToNext());
        }
        cursor.close();
        return cultures;
    }

    private ContentValues prepareContentValues(Culture c) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(NAME, c.getName());

        contentValues.put(LOW_AIR_TEMPERATURE, c.getLowAirTemperature());
        contentValues.put(HIGH_AIR_TEMPERATURE, c.getHighAirTemperature());

        contentValues.put(LOW_AIR_HUMIDITY, c.getLowAirHumidity());
        contentValues.put(HIGH_AIR_HUMIDITY, c.getHighAirHumidity());

        contentValues.put(LOW_AMBIENT_LIGHT, c.getLowAmbientLight());
        contentValues.put(HIGH_AMBIENT_LIGHT, c.getHighAmbientLight());

        contentValues.put(LOW_SOIL_HUMIDITY, c.getLowSoilHumidity());
        contentValues.put(HIGH_SOIL_HUMIDITY, c.getHighSoilHumidity());

        contentValues.put(LOW_SOIL_PH, c.getLowSoilPH());
        contentValues.put(HIGH_SOIL_PH, c.getHighSoilPH());

        return contentValues;
    }

    private Culture extractCulture(Cursor cursor) {
        return new Culture(cursor.getInt(0), cursor.getString(1), cursor.getDouble(2), cursor.getDouble(3),
                cursor.getDouble(4), cursor.getDouble(5), cursor.getDouble(6), cursor.getDouble(7),
                cursor.getDouble(8), cursor.getDouble(9), cursor.getDouble(10), cursor.getDouble(11));
    }
}
