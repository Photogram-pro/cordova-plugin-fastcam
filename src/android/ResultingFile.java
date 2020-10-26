package com.cordovapluginfastcam;

public class ResultingFile {
    public enum ResultingFileTypes  {
        VIDEO,
        IMAGE
    }

    private ResultingFileTypes fileType;
    private String filePath;
    /**
     * MS Timestamp when the
     * file was created. For
     * videos, it's the end
     * timestamp
     */
    private long timestamp;
    public ResultingFile(String filePath, ResultingFileTypes type, long timestamp) {
        this.filePath = filePath;
        this.timestamp = timestamp;
        this.fileType = type;
    }

    public  String toJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"filePath\": \"");
        sb.append(this.filePath);
        sb.append("\",");
        sb.append("\"fileType\": \"");
        sb.append(this.fileType);
        sb.append("\",");
        sb.append("\"timestamp\": \"");
        sb.append(this.timestamp);
        sb.append("\"");
        sb.append("}");

        return sb.toString();
    }
}
