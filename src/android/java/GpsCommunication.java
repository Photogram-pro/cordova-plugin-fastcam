package com.cordovapluginfastcam;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * Communication with GPS
 * device via USB serial.
 * It's a singleton,
 * as only one instance
 * should exist per app
 */
public class GpsCommunication implements SerialInputOutputManager.Listener {
    private static GpsCommunication instance = null;
    private static final String TAG = "GpsCommunication";
    private static final String USB_PERMISSION = "com.photogram.cmaeratestapp.USB_PERMISSION";
    private UsbSerialPort port;
    private SerialInputOutputManager usbIoManager;
    private NMEA nmeaParser = new NMEA();
    private NMEA.GPSPosition currentPosition;
    private int baudRate = 115200;
    private ArrayList<GpsDataCallback> callbacks = new ArrayList<>();
    private BroadcastReceiver broadcastReceiver;
    private GeoidHeight geoidHeightCorrector;
    /**
     * Alt offset which can be configured
     * from the outside
     * Can be used to specify
     * a value which gets subtracted
     * from the actual height
     * value (in centimeter).
     * Useful if the GPS sensor
     * is mounted on a pole.
     */
    private double altOffset = 0;

    private Activity activity;


    public GpsCommunication(Activity activity) {
        this.activity = activity;
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getAction().equals(USB_PERMISSION)) {
                    initialize();
                }
            }
        };
        activity.registerReceiver(broadcastReceiver, new IntentFilter(USB_PERMISSION));

        try {
            this.geoidHeightCorrector = new GeoidHeight(this.activity.getAssets().open("grid_x.txt"), this.activity.getAssets().open("grid_y.txt"), this.activity.getAssets().open("grid_h.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static GpsCommunication getInstance(Activity activity) {
        if (GpsCommunication.instance == null) {
            GpsCommunication.instance = new GpsCommunication(activity);
        }
        return GpsCommunication.instance;
    }

    public static GpsCommunication getInstance() {
        return GpsCommunication.instance;
    }

    public void configure(int baudRate, double altOffset) {
        if (baudRate != 0) {
            this.baudRate = baudRate;
        }
        this.altOffset = altOffset;
    }

    public void initialize() {
        // Find all available drivers from attached devices.
        UsbManager manager = (UsbManager) this.activity.getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            Log.d(TAG, "No USB devices found!");
            return;
        }

        // Open a connection to the first available driver.
        UsbSerialDriver driver = availableDrivers.get(0);
        UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {
            Log.d(TAG, "Need to request permission!");
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this.activity, 0, new Intent(this.USB_PERMISSION), 0);
            // and a filter on the permission we ask
            IntentFilter filter = new IntentFilter();
            filter.addAction(this.USB_PERMISSION);
            manager.requestPermission(driver.getDevice(), pendingIntent);
            Log.d(TAG, "Permission requested.");
            return;
        }

         port = driver.getPorts().get(0); // Most devices have just one port (port 0)
        try {
            port.open(connection);
            port.setParameters(this.baudRate, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
        } catch (IOException e) {
            e.printStackTrace();
        }

        usbIoManager = new SerialInputOutputManager(port, this);
        Executors.newSingleThreadExecutor().submit(usbIoManager);
    }

    @Override
    public void onNewData(byte[] bytes) {
        String strData = new String(bytes, StandardCharsets.UTF_8);
        this.currentPosition = nmeaParser.parse(strData, "GGA");
        // Interpolate geoid height and add it
        if (this.currentPosition.altitude > 0) {
            double altOffsetInMeters = this.altOffset != 0 ? this.altOffset / 100 : 0;
            double geoidH = this.geoidHeightCorrector.getInterpolator().interpolateGeoidHeight(this.currentPosition.lat, this.currentPosition.lon);
            this.currentPosition.altitude = this.currentPosition.altitude + this.currentPosition.geoidSeparator - geoidH - altOffsetInMeters;
            this.currentPosition.interpolatedGeoid = geoidH;
        }
        for (GpsDataCallback cb : this.callbacks) {
            cb.onData(this.currentPosition);
        }
    }

    @Override
    public void onRunError(Exception e) {
        e.printStackTrace();
    }

    public void close() {
        if (port != null) {
            try {
                port.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public NMEA.GPSPosition getCurrentPosition() {
        return this.currentPosition;
    }

    public void addEventListener(GpsDataCallback cb) {
        this.callbacks.add(cb);
    }
}
