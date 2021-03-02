package com.bluetooth.bdsk;

public class Constants {

    public static final String TAG = "SCHROOM";

    public static final String FIND = "Find Schroom devices";

    public static final String UNKNOWN_DEVICE = "unknown device";

    public static final String UNKNOWN_CULTURE = "unknown culture";

    public static final String STOP_SCANNING = "Stop scanning";

    public static final String SCANNING = "Scanning";

    public static final String PARCEL_CURRENT_DEVICE_ADDRESS = "currentDeviceAddress";

    public static final String PARCEL_SHARED_PREFS = "sharedPrefs";

    public static final String PARCEL_CURRENT_CULTURE = "currentCultureId";

    public static final String PARCEL_CULTURE_CHANGE_NOTIFIED = "cultureChangeNotified";

    public static final String PARCEL_FIRST_TIME_CONNECTED = "firstTimeConnected";

    public static final String PARCEL_LAST_MEASUREMENT_TIME = "lastMeasurementTime";

    public static final String PARCEL_ARTIFICIAL_LIGHT_SOURCE = "lightSource";

    public static final double HPS_COEFFICIENT = 0.13;
    public static final double SUNSHINE_COEFFICIENT = 0.2;
    public static final double PAR_TO_DLI_CONST = 0.0864;
    public static final double LUX_TO_FOOT_CANDEL = 0.0929030436;
    public static final int MAX_SIGNAL = 350;
}
