package com.thad.brainbraille;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.util.Log;

import com.thad.brainbraille.UsbSerial.*;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private final String TAG = MainActivity.class.getSimpleName();
    private static UsbSerialPort sPort = null;

    private UsbManager mUsbManager;

    private TextView mTitleTextView;
    private TextView mMainTextView;

    UsbDevice device;
    UsbDeviceConnection connection;

    private static final String ACTION_USB_PERMISSION =
            "com.thad.brainbraille.USB_PERMISSION";

    protected void initializeConnection(UsbManager usbManager){
        Log.d(TAG, "Initializing...");
        mTitleTextView.setText("Initializing...");

        HashMap<String, UsbDevice> usbDevices = usbManager.getDeviceList();
        mMainTextView.setText(usbDevices.toString());

        if (!usbDevices.isEmpty()) {
            boolean keep = true;
            for (Map.Entry<String, UsbDevice> entry : usbDevices.entrySet()) {
                device = entry.getValue();
                int deviceVID = device.getVendorId();
                if (deviceVID == 0x2341)//Arduino Vendor ID
                {
                    PendingIntent pi = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    usbManager.requestPermission(device, pi);
                    keep = false;
                } else {
                    connection = null;
                    device = null;
                }

                if (!keep)
                    break;
            }
        }

    }


    protected void resumeConnection(){
        Log.d(TAG, "Resumed, port = " + sPort);
        if (sPort == null){
            mTitleTextView.setText("No serial device.");
        } else {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            UsbDeviceConnection connection = usbManager.openDevice(sPort.getDriver().getDevice());

            if (connection == null) {
                mTitleTextView.setText("Opening device failed");
                return;
            }

            try {
                sPort.open(connection);
                sPort.setParameters(38400, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            } catch (IOException e){
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                mTitleTextView.setText("Error opening device: " + e.getMessage());

                try {
                    sPort.close(); // try closing port if error caught
                } catch (IOException e2){
                    // ignore
                }

                sPort = null;
                return;
            }

            if (sPort == null){
                mTitleTextView.setText("No serial device.");
            } else {
                mTitleTextView.setText("Serial device: " + sPort.getClass().getSimpleName());
            }
        }
    }

    public void playSounds(){

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mTitleTextView = (TextView) findViewById(R.id.demoTitle);
        mMainTextView = (TextView) findViewById(R.id.mainText);

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        initializeConnection(mUsbManager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Start sounds!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                playSounds();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume(){
        super.onResume();
        //resumeConnection();
    }

}
