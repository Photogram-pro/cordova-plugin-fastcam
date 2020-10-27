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
    private GpsDataCallback callback;
    private BroadcastReceiver broadcastReceiver;

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

    public void configure(int baudRate) {
        if (baudRate == 0) {
            return;
        }
        this.baudRate = baudRate;
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
        if (this.callback != null) {
            this.callback.onData(this.currentPosition);
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
        try {
            return this.currentPosition.clone();
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void setDataCallback(GpsDataCallback cb) {
        this.callback = cb;
    }
}
