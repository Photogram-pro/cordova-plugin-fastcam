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
    private JSONObject position;
    /**
     * MS Timestamp when the
     * file was created. For
     * videos, it's the end
     * timestamp
     */
    private long timestamp;
    public ResultingFile(String filePath, ResultingFileTypes type, long timestamp, JSONObject position) {
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
            if (this.position != null) {
                json.put("position", this.position);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

        return json.toString();
    }
}
