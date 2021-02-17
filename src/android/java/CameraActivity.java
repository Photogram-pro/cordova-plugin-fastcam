package com.cordovapluginfastcam;

import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.otaliastudios.cameraview.CameraListener;
import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.PictureResult;
import com.otaliastudios.cameraview.VideoResult;
import com.otaliastudios.cameraview.controls.Mode;

import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class CameraActivity extends Activity implements GpsDataCallback {
    private static final String TAG = "CameraActivity";
    /**
     * Will be set to the activity
     * result as the JSON can be
     * too large to pass trough
     * intent data, which is limited
     */
    private static String resultJson = "";
    /**
     * Photos are temporarily saved
     * on the phone and deleted after
     * DELETE_PHOTOS_AFTER_DAYS days
     */
    private static final long DELETE_PHOTOS_AFTER_DAYS = 10;
    private String dataFolderPath;
    private long startEventTimestamp = 0;
    private boolean isCapturing = false;
    private CameraMode mode = CameraMode.SINGLE_PHOTO;
    private ArrayList<ResultingFile> resultingFiles = new ArrayList<>();
    private PerformanceAnalysis videoDurationDeviationAnalyis = new PerformanceAnalysis("CameraView Duration");
    /**
     * The current position of a GPS
     * device connected via USB
     * will be saved in the very moment
     * the picture is being taken.
     * That's why it needs to be
     * stored.
     */
    private JSONObject currentPosition;
    /**
     * Before taking photos or videos,
     * this timestamp can be set from
     * the outside to mark an arbitrary
     * point in time of another system.
     * The camera class will, from that point
     * on, start a timer and use this
     * time as a frame of reference for
     * video and picture timestamps.
     * This can be used to sync with
     * external devices like GPS.
     */
    private long syncedClockOffsetMs = 0;
    private ScheduledThreadPoolExecutor pictureTakingLoop = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(this.getViewId("activity_camera", "layout"));
        this.dataFolderPath = this.getApplicationContext().getFilesDir().getAbsolutePath();

        Log.d(TAG, "Files dir: " + this.dataFolderPath);

        this.configureFromIntentData();
        this.setupDataFolder();
        this.checkPermissions();
        this.setupCamera();
        GpsCommunication gps = GpsCommunication.getInstance();
        gps.addEventListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.getCamera().open();
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.getCamera().close();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.getCamera().destroy();
    }

    private void configureFromIntentData() {
        Bundle data = getIntent().getExtras();
        this.mode = CameraMode.valueOf(data.getString("cameraMode"));
        long clockSyncTimestamp = data.getLong("clockSyncTimestamp", 0);

        if (clockSyncTimestamp != 0) {
            this.syncClock(clockSyncTimestamp);
        }
    }

    /**
     * Returns the current
     * time in milliseconds.
     * If the time was synced
     * from the outside, that
     * time offset will be used.
     * Otherwise, the device's
     * internal local time.
     */
    private long getCurrentTimeMs() {
        if (this.syncedClockOffsetMs == 0) {
            return System.currentTimeMillis();
        }
        return System.currentTimeMillis() + this.syncedClockOffsetMs;
    }

    private int getViewId(String name, String defType) {
        String appResourcesPackage = this.getPackageName();
        return getResources().getIdentifier(name, defType, appResourcesPackage);
    }

    private CameraView getCamera() {
        CameraView camera = findViewById(this.getViewId("camera", "id"));
        return camera;
    }

    /**
     * Creates a JSON array of
     * resulting files
     */
    private String resultingFilesToJSON() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        int size = this.resultingFiles.size();
        for (int i = 0; i < size; i += 1) {
            sb.append(this.resultingFiles.get(i).toJSON());
            if (i != (size - 1)) {
                sb.append(",");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    public static String getResultJson() {
        return resultJson;
    }

    /**
     * Trigger 'onActivityResult'
     * for the calling party
     */
    private void finishWithResult() {
        String resJSON = this.resultingFilesToJSON();
        resultJson = resJSON;
        setResult(RESULT_OK);
        finishAndRemoveTask();
    }

    private void setupCamera() {
        CameraView camera = getCamera();

        camera.addCameraListener(new CameraListener() {
            @Override
            public void onPictureTaken(PictureResult result) {
                long endEventTimestamp = getCurrentTimeMs();
                long pictureProcessingDuration = (endEventTimestamp - startEventTimestamp);
                Log.d(TAG, "Picture processing duration: " + pictureProcessingDuration);

                String filePath = getLocalFilePath("img_" + getCurrentTimeMs() + ".jpeg");
                result.toFile(new File(filePath), file -> {
                    resultingFiles.add(new ResultingFile(filePath, ResultingFile.ResultingFileTypes.IMAGE, startEventTimestamp, currentPosition));
                    if (mode == CameraMode.SINGLE_PHOTO) {
                        finishWithResult();
                    }
                });
            }

            @Override
            public void onVideoTaken(VideoResult result) {
                long endEventTimestamp = getCurrentTimeMs();
                long cameraViewExpectedDuration = (endEventTimestamp - startEventTimestamp);

                File file = result.getFile();

                MediaMetadataRetriever retriever = new MediaMetadataRetriever();

                retriever.setDataSource(file.getAbsolutePath());
                double actualDuration = Double.parseDouble(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION));

                double durationDiff = Math.abs(cameraViewExpectedDuration - actualDuration);
                videoDurationDeviationAnalyis.addValue(durationDiff);
                Log.d(TAG, "Measured duration difference (= deviation of the the end time): " + durationDiff);
                resultingFiles.add(new ResultingFile(file.getAbsolutePath(), ResultingFile.ResultingFileTypes.VIDEO, startEventTimestamp, currentPosition));
                finishWithResult();
            }

            @Override
            public void onVideoRecordingStart() {
                startEventTimestamp = getCurrentTimeMs();
                Log.d(TAG, "onVideoRecordingStart");
            }
        });
    }

    /**
     * First deletes the Media folder and then
     * re-creates it, empty
     * */
    private void setupDataFolder() {
        File folder = new File(dataFolderPath);
        if (folder.exists()) {
            // Delete folder
            long deleteAfterMs = DELETE_PHOTOS_AFTER_DAYS * 24 * 60 * 60 * 1000;
            FileUtils.deleteRecursive(folder, deleteAfterMs);
        }
        folder.mkdirs();
    }

    private void checkPermissions() {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }
    }

    private void updateCurrentPosition() {
        GpsCommunication gps = GpsCommunication.getInstance();
        if (gps != null) {
            GPSPosition pos = gps.getCurrentPosition();
            if (pos != null) {
                this.currentPosition = pos.toJson();
                Log.d(TAG, "currentPos: " + this.currentPosition.toString());
            }

        }
    }

    private String getLocalFilePath(String fileName) {
        File folder = new File(dataFolderPath);
        String filePath = folder + "/" + fileName;
        return filePath;
    }

    public void onCaptureVideo() {
        CameraView camera = getCamera();
        camera.setMode(Mode.VIDEO);

        if (this.isCapturing) {
            camera.stopVideo();
        } else {
            String filePath = getLocalFilePath("video_" + this.getCurrentTimeMs() + ".mp4");
            File outputFile = new File(filePath);
            this.updateCurrentPosition();
            camera.takeVideoSnapshot(outputFile);
            startEventTimestamp = this.getCurrentTimeMs();
        }

        this.isCapturing = !this.isCapturing;
    }

    public void onCaptureSingleImage() {
        CameraView camera = getCamera();
        camera.setMode(Mode.PICTURE);
        this.updateCurrentPosition();
        startEventTimestamp = this.getCurrentTimeMs();
        camera.takePictureSnapshot();
    }

    /**
     * Always take a picture when
     * a new coordinate arrives
     */
    public void onTogglePictureTakingLoop() {
        if (this.isCapturing) {
            Log.d(TAG, "FINISH WITH RESULT!");
            this.finishWithResult();
        }

        this.isCapturing = !this.isCapturing;
    }

    /**
     * Sync the clock from the
     * outside. Pass in the current
     * time in milliseconds of
     * another system, and the media
     * capture will return timestamps
     * relative to that timeframe
     */
    public void syncClock(long currentTimeMs) {
        this.syncedClockOffsetMs = currentTimeMs - System.currentTimeMillis();
    }

    public void onRecordButtonClick(View view) {
        switch (this.mode) {
            case PHOTO_SERIES:
                this.onTogglePictureTakingLoop();
                break;
            case SINGLE_PHOTO:
                this.onCaptureSingleImage();
                break;
            case VIDEO:
                this.onCaptureVideo();
                break;
        }
    }

    @Override
    public void onData(GPSPosition pos) {
        if (this.mode == CameraMode.PHOTO_SERIES && this.isCapturing) {
            this.currentPosition = pos.toJson();
            CameraView camera = getCamera();
            startEventTimestamp = this.getCurrentTimeMs();
            camera.takePictureSnapshot();
        }
    }
}