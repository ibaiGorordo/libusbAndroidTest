package com.example.libusbAndroidTest;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.libusbAndroidTest.databinding.ActivityMainBinding;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'lib' library on application startup.
    static {
        System.loadLibrary("libusbAndroidTest");
    }

    private ActivityMainBinding binding;
    private UsbManager usbManager;

    private TextView tv;

    private static String deviceName;
    private static final String TAG = "libusbAndroidTest" ;
    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if(device != null){
                            connectDevice(device);
                        }
                    }
                    else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    protected void connectDevice(UsbDevice device)
    {
        UsbInterface intf = device.getInterface(0);
        UsbEndpoint endpoint = intf.getEndpoint(0);
        UsbDeviceConnection connection = usbManager.openDevice(device);
        connection.claimInterface(intf, true);
        int fileDescriptor = connection.getFileDescriptor();

        deviceName = initializeNativeDevice(fileDescriptor);

        // Example of a call to a native method
        tv.setText(deviceName);
    }

    protected void checkUsbDevices()
    {
        PendingIntent permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);

        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();
        for (UsbDevice device : deviceList.values()) {
            if(usbManager.hasPermission(device))
            {
                connectDevice(device);
            }
            else {
                usbManager.requestPermission(device, permissionIntent);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        tv = binding.sampleText;

        // Initialize UsbManager
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        // Initialize the receiver for getting the device permission
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);

        checkUsbDevices();
    }

    /**
     * A native method that is implemented by the 'lib' native library,
     * which is packaged with this application.
     */
    public native String initializeNativeDevice(int fileDescriptor);
}