package com.example.aidemo;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ConnectShare extends AppCompatActivity {
    private static final String TAG = "ConnectShare";
    private static BluetoothSocket mBTSocket;
    ProgressDialog progressDialog;
    Button captureBtn;
    Button inflateBtn;
    private int mMaxChars = 50000;//Default//change this to string..........
    private UUID mDeviceUUID;
    //private ReadInput mReadThread = null;
    private boolean mIsUserInitiatedDisconnect = false;
    private boolean mIsBluetoothConnecte;
    private BluetoothDevice mDevice;
    private boolean mIsBluetoothConnected = false;
    private OutputStream mOutputStream;
    ProgressDialog pd;
    boolean flag = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect_share);
        captureBtn = findViewById(R.id.connect_btn);
        inflateBtn = findViewById(R.id.inflate);

        pd = new ProgressDialog(ConnectShare.this);
        pd.setTitle("Waiting for response...");
        pd.setCancelable(false);

        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        mDevice = b.getParcelable(MainActivity.DEVICE_EXTRA);
        Log.d(TAG, "onCreate: DeviceUUID: " + MainActivity.DEVICE_UUID);
        mDeviceUUID = UUID.fromString(b.getString(MainActivity.DEVICE_UUID));
        Log.d(TAG, "onCreate: mDeviceUUID:" + mDeviceUUID);
        mMaxChars = b.getInt(MainActivity.BUFFER_SIZE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new ConnectBT().execute();
    }

    public void Capture(View view) {
        String cmd = "capture";
        final InputStream inputStream;
        try {
            inputStream = mBTSocket.getInputStream();
            mBTSocket.getOutputStream().write(cmd.getBytes());

            Toast.makeText(ConnectShare.this, "Result Captured", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Capture1: " + mBTSocket.getInputStream());

            Log.d(TAG, "Capture2: " + inputStream.available());

            pd.show();

            final Thread thread = new Thread() {
                @Override
                public void run() {
                    try {
                        while(flag) {

                            if (inputStream.available()!=0) {

                                BufferedInputStream bis = new BufferedInputStream(inputStream);
                                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                                int result = bis.read();

                                while (result != -1) {
                                    buf.write((byte) result);
                                    result = bis.read();
                                }

                                Log.d(TAG, "Capture3: " + buf);

                                flag = false;
                                pd.dismiss();
                            }

                            sleep(500);
                        }
                    } catch (InterruptedException | IOException e) {
                        e.printStackTrace();
                    }
                }
            };
            thread.start();


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void Inflate(View view) {
        String cmd = "inflate";
        try {
            mBTSocket.getOutputStream().write(cmd.getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private class ConnectBT extends AsyncTask<Void, Void, Void> {
        private boolean mConnectSuccessful = true;

        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(ConnectShare.this, "Hold on", "Connecting");
        }

        @Override
        protected Void doInBackground(Void... devices) {
            try {
                if (mBTSocket == null || !mIsBluetoothConnected) {
                    mBTSocket = mDevice.createInsecureRfcommSocketToServiceRecord(mDeviceUUID);
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    mBTSocket.connect();
                    if (mBTSocket.isConnected()) {
                        Log.d(TAG, "doInBackground: socket connected successfull");
                    }
                }
            } catch (IOException e) {

                Log.d(TAG, "doInBackground: socket connection failed: " + e);
                mConnectSuccessful = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if (!mConnectSuccessful) {
                Toast.makeText(getApplicationContext(), "Could not connect to device.Please turn on your Hardware", Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(getApplicationContext(), "Connected to device"
                        , Toast.LENGTH_SHORT).show();
                mIsBluetoothConnected = true;
                Toast.makeText(getApplicationContext(), "Connection Successful", Toast.LENGTH_SHORT).show();
            }
            progressDialog.dismiss();
        }
    }


}
