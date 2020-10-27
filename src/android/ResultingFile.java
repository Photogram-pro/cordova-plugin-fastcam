package com.cordovapluginfastcam;

import org.json.JSONException;
import org.json.JSONObject;

public class ResultingFile {
    public enum ResultingFileTypes  {
        VIDEO,
        IMAGE
    }

    private ResultingFileTypes fileType;
    private String filePath;
    private NMEA.GPSPosition position;
    /**
     * MS Timestamp when the
     * file was created. For
     * videos, it's the end
     * timestamp
     */
    private long timestamp;
    public ResultingFile(String filePath, ResultingFileTypes type, long timestamp, NMEA.GPSPosition position) {
        this.filePath = filePath;
        this.timestamp = timestamp;
        this.fileType = type;
        this.position = position;
    }

    public  String toJSON() {
        JSONObject json = new JSONObject();
        try {
            json.put("filePath", this.filePath);
            json.put("fileType", this.fileType);
            json.put("timestamp", this.timestamp);
            json.put("position", this.position.toJson());

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }
}
