package com.cordovapluginfastcam;


import org.json.JSONException;
import org.json.JSONObject;

public class GPSPosition {
    public double time = 0.0f;
    public double lat = 0.0f;
    public double lon = 0.0f;
    public boolean fixed = false;
    public int quality = 0;
    public float dir = 0.0f;
    public double altitude = 0.0f;
    public float velocity = 0.0f;
    public double geoidSeparator = 0.0d;
    public double interpolatedGeoid = 0.0d;
    public double origAltitude = 0.0d;

    public void updatefix() {
        fixed = quality > 0;
    }

    public String toString() {
        return String.format("POSITION: lat: %d, lon: %d, time: %f, Q: %d, dir: %f, alt: %f, vel: %f", lat, lon, time, quality, dir, altitude, velocity);
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("altitude", this.altitude);
            json.put("origAltitude", this.origAltitude);
            json.put("interpolatedGeoid", this.interpolatedGeoid);
            json.put("geoidH", this.geoidSeparator);
            json.put("dir",  this.dir);
            json.put("fixed", this.fixed);
            json.put("lat", this.lat);
            json.put("lon", this.lon);
            json.put("quality", this.quality);
            json.put("time", this.time);
            json.put("velocity", this.velocity);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return json;
    }

}
