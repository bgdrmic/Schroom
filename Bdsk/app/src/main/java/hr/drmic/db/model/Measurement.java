package hr.drmic.db.model;

import android.content.SharedPreferences;

import com.bluetooth.bdsk.Constants;
import com.bluetooth.bdsk.ui.MainActivity;

import java.util.Date;

public class Measurement {

    private static Date FIRST_MEASUREMENT_TIME = null;

    // interval between measurements in milliseconds(x * 60 * 1000 = x minutes).
    private static final int TIME_INTERVAL = 30 * 60 * 1000;

    // ordinal number of this measurement
    private static Integer current_time;

    private Integer id, time;
    private Double airPressure, airTemperature, airHumidity, ambientLight, soilHumidity, soilPH;

    public Measurement(Integer id, Integer time, Double airPressure, Double airTemperature, Double airHumidity, Double ambientLight, Double soilHumidity, Double soilPH) {
        this.id = id;
        this.time = time;
        this.airTemperature = airTemperature;
        this.airPressure = airPressure;
        this.airHumidity = airHumidity;
        this.ambientLight = ambientLight;
        this.soilHumidity = soilHumidity;
        this.soilPH = soilPH;
    }

    public Measurement(Integer id, Double airPressure, Double airTemperature, Double airHumidity, Double ambientLight, Double soilHumidity, Double soilPH) {
        this(id, current_time++, airPressure, airTemperature, airHumidity, ambientLight, soilHumidity, soilPH);

        MainActivity.sharedPreferencesEditor.putInt(Constants.PARCEL_LAST_MEASUREMENT_TIME, current_time);
        MainActivity.sharedPreferencesEditor.apply();
    }

    public Measurement(Double airPressure, Double airTemperature, Double airHumidity, Double ambientLight, Double soilHumidity, Double soilPH) {
        this(null, airPressure, airTemperature, airHumidity, ambientLight, soilHumidity, soilPH);
    }

    public static Date getFirstMeasurementTime() {
        return FIRST_MEASUREMENT_TIME;
    }

    public static int getTimeInterval() {
        return TIME_INTERVAL;
    }

    public static int getMeasurementsPerDay() {
        return 24 * 60 * 60 * 1000 / TIME_INTERVAL;
    }

    public static int getMeasurementsPerHour() { return getMeasurementsPerDay() / 24; }

    public static Integer getCurrentTime() {
        return current_time;
    }

    public static void setCurrentTime(Integer currentTime) {
        Measurement.current_time = currentTime;
        FIRST_MEASUREMENT_TIME = new Date(System.currentTimeMillis() - currentTime * TIME_INTERVAL);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Double getAirPressure() {
        return airPressure;
    }

    public void setAirPressure(Double airPressure) {
        this.airPressure = airPressure;
    }

    public Double getAirTemperature() {
        return airTemperature;
    }

    public void setAirTemperature(Double airTemperature) {
        this.airTemperature = airTemperature;
    }

    public Double getAirHumidity() {
        return airHumidity;
    }

    public void setAirHumidity(Double airHumidity) {
        this.airHumidity = airHumidity;
    }

    public Double getAmbientLight() {
        return ambientLight;
    }

    public void setAmbientLight(Double ambientLight) {
        this.ambientLight = ambientLight;
    }

    public Double getSoilHumidity() {
        return soilHumidity;
    }

    public void setSoilHumidity(Double soilHumidity) {
        this.soilHumidity = soilHumidity;
    }

    public Double getSoilPH() {
        return soilPH;
    }

    public void setSoilPH(Double soilPH) {
        this.soilPH = soilPH;
    }


    @Override
    public String toString() {
        return "Measurement{" +
                "time=" + time +
                ", airPressure=" + airPressure +
                ", airTemperature=" + airTemperature +
                ", airHumidity=" + airHumidity +
                ", ambientLight=" + ambientLight +
                ", soilHumidity=" + soilHumidity +
                ", soilPH=" + soilPH +
                '}';
    }
}