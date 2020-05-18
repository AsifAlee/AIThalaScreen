package com.example.aidemo;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    BluetoothDevice[] pairedDevices;
    ListView lv;
    private UUID mDeviceUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private int mBufferSize = 50000; //Default
    public static final String DEVICE_EXTRA = "com.example.lightcontrol.SOCKET";
    public static final String DEVICE_UUID = "com.example.lightcontrol.uuid";
    private static final String TAG = "MainActivity";
    public static final String BUFFER_SIZE = "com.example.lightcontrol.buffersize";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        lv = findViewById(R.id.listview);

        if (!bluetoothAdapter.isEnabled()) {
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn, 0);
        }

        Set<BluetoothDevice> bt = bluetoothAdapter.getBondedDevices();
        pairedDevices = new BluetoothDevice[bt.size()];
        String[] devicesNameArray = new String[bt.size()];
        int index = 0;

        if (bt.size() > 0) {
            for (BluetoothDevice device : bt) {
                pairedDevices[index] = device;
                devicesNameArray[index] = device.getName();
                Log.d(TAG, "onCreate: name" + device.getName());
                Log.d(TAG, "onCreate: device: " + device.getAddress());
                index++;
            }
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, devicesNameArray);
            lv.setAdapter(arrayAdapter);


            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Intent intent = new Intent(MainActivity.this, ConnectShare.class);
                    intent.putExtra(DEVICE_EXTRA, pairedDevices[position]);
                    intent.putExtra(DEVICE_UUID, mDeviceUUID.toString());
                    intent.putExtra(BUFFER_SIZE, mBufferSize);
                    startActivity(intent);
                }
            });


        }
    }
}
