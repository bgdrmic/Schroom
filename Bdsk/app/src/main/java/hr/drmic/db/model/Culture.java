package hr.drmic.db.model;

import java.util.ArrayList;

public class Culture {

    private Integer id;
    private String name;
    private Double lowAirTemperature = null, highAirTemperature = null, lowAirHumidity = null, highAirHumidity = null, lowAmbientLight = null, highAmbientLight = null;
    private Double lowSoilHumidity = null, highSoilHumidity = null, lowSoilPH = null, highSoilPH = null;
    private ArrayList<String> tips = new ArrayList<>();

    private static final int NUMBER_OF_PARAMS = 11;

    public Culture(String name, Double lowAirTemperature, Double highAirTemperature, Double lowAirHumidity, Double highAirHumidity, Double lowAmbientLight, Double highAmbientLight, Double lowSoilHumidity, Double highSoilHumidity, Double lowSoilPH, Double highSoilPH) {
        this.name = name;
        this.lowAirTemperature = lowAirTemperature;
        this.highAirTemperature = highAirTemperature;
        this.lowAirHumidity = lowAirHumidity;
        this.highAirHumidity = highAirHumidity;
        this.lowAmbientLight = lowAmbientLight;
        this.highAmbientLight = highAmbientLight;
        this.lowSoilHumidity = lowSoilHumidity;
        this.highSoilHumidity = highSoilHumidity;
        this.lowSoilPH = lowSoilPH;
        this.highSoilPH = highSoilPH;
    }

    public Culture(Integer id, String name, Double lowAirTemperature, Double highAirTemperature, Double lowAirHumidity, Double highAirHumidity, Double lowAmbientLight, Double highAmbientLight, Double lowSoilHumidity, Double highSoilHumidity, Double lowSoilPH, Double highSoilPH) {
        this(name, lowAirTemperature, highAirTemperature, lowAirHumidity, highAirHumidity, lowAmbientLight, highAmbientLight, lowSoilHumidity, highSoilHumidity, lowSoilPH, highSoilPH);
        this.id = id;
    }

    /**
     * Converts one database entry to an instance of Culture class.
     * Does NOT contain an ID. Parameters are separated by tab. All parameters are required in a fixed order.
     * If some parameter is unknown NULL should be written instead.
     * @param entry - One line which represents data about a culture.
     */
    public Culture(String entry) {
        String[] fields = entry.split("\\t");
        if(fields.length < NUMBER_OF_PARAMS) {
            throw new IllegalArgumentException();
        }

        if(!fields[0].toUpperCase().equals("NULL")) {
            this.name = fields[0].trim();
        }

        if(!fields[1].toUpperCase().equals("NULL")) {
            this.lowAirTemperature = Double.parseDouble(fields[1].trim());
        }
        if(!fields[2].toUpperCase().equals("NULL")) {
            this.highAirTemperature = Double.parseDouble(fields[2].trim());
        }

        if(!fields[3].toUpperCase().equals("NULL")) {
            this.lowAirHumidity = Double.parseDouble(fields[3].trim());
        }
        if(!fields[4].toUpperCase().equals("NULL")) {
            this.highAirHumidity = Double.parseDouble(fields[4].trim());
        }

        if(!fields[5].toUpperCase().equals("NULL")) {
            this.lowAmbientLight = Double.parseDouble(fields[5].trim());
        }
        if(!fields[6].toUpperCase().equals("NULL")) {
            this.highAmbientLight = Double.parseDouble(fields[6].trim());
        }

        if(!fields[7].toUpperCase().equals("NULL")) {
            this.lowSoilHumidity = Double.parseDouble(fields[7].trim());
        }
        if(!fields[8].toUpperCase().equals("NULL")) {
            this.highSoilHumidity = Double.parseDouble(fields[8].trim());
        }

        if(!fields[9].toUpperCase().equals("NULL")) {
            this.lowSoilPH = Double.parseDouble(fields[9].trim());
        }
        if(!fields[10].toUpperCase().equals("NULL")) {
            this.highSoilPH = Double.parseDouble(fields[10].trim());
        }

        for(int i = NUMBER_OF_PARAMS; i < fields.length; i++) {
            addTip(fields[i]);
        }
    }

    public ArrayList<String> getTips() {
        return tips;
    }

    public void addTip(String tip) {
        tips.add(tip);
    }


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLowAirTemperature() {
        return lowAirTemperature;
    }

    public void setLowAirTemperature(Double lowAirTemperature) {
        this.lowAirTemperature = lowAirTemperature;
    }

    public Double getHighAirTemperature() {
        return highAirTemperature;
    }

    public void setHighAirTemperature(Double highAirTemperature) {
        this.highAirTemperature = highAirTemperature;
    }

    public Double getLowAirHumidity() {
        return lowAirHumidity;
    }

    public void setLowAirHumidity(Double lowAirHumidity) {
        this.lowAirHumidity = lowAirHumidity;
    }

    public Double getHighAirHumidity() {
        return highAirHumidity;
    }

    public void setHighAirHumidity(Double highAirHumidity) {
        this.highAirHumidity = highAirHumidity;
    }

    public Double getLowAmbientLight() {
        return lowAmbientLight;
    }

    public void setLowAmbientLight(Double lowAmbientLight) {
        this.lowAmbientLight = lowAmbientLight;
    }

    public Double getHighAmbientLight() {
        return highAmbientLight;
    }

    public void setHighAmbientLight(Double highAmbientLight) {
        this.highAmbientLight = highAmbientLight;
    }

    public Double getLowSoilHumidity() {
        return lowSoilHumidity;
    }

    public void setLowSoilHumidity(Double lowSoilHumidity) {
        this.lowSoilHumidity = lowSoilHumidity;
    }

    public Double getHighSoilHumidity() {
        return highSoilHumidity;
    }

    public void setHighSoilHumidity(Double highSoilHumidity) {
        this.highSoilHumidity = highSoilHumidity;
    }

    public Double getLowSoilPH() {
        return lowSoilPH;
    }

    public void setLowSoilPH(Double lowSoilPH) {
        this.lowSoilPH = lowSoilPH;
    }

    public Double getHighSoilPH() {
        return highSoilPH;
    }

    public void setHighSoilPH(Double highSoilPH) {
        this.highSoilPH = highSoilPH;
    }

    public static int getNumberOfParams() {
        return NUMBER_OF_PARAMS;
    }
}
