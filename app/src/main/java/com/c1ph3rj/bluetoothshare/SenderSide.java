package com.c1ph3rj.bluetoothshare;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

public class SenderSide extends AppCompatActivity {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice mBluetoothDevice;
    private BluetoothSocket mBluetoothSocket;
    private OutputStream mOutputStream;

    private EditText mMessageEditText;
    private Button mSendButton;
    private ListView mDeviceListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sender_side);

        mMessageEditText = findViewById(R.id.message_edit_text);
        mSendButton = findViewById(R.id.send_button);
        mDeviceListView = findViewById(R.id.devices_list_view);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported", Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 0);
        }

        mSendButton.setOnClickListener(view -> sendMessage());

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            ArrayList<BluetoothDevice> deviceList = new ArrayList<>(pairedDevices);
            ArrayAdapter<BluetoothDevice> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, deviceList);
            mDeviceListView.setAdapter(adapter);
            mDeviceListView.setOnItemClickListener((adapterView, view, i, l) -> {
                mBluetoothDevice = (BluetoothDevice) adapterView.getItemAtPosition(i);
                connectToDevice();
            });
        }
    }

    private void connectToDevice() {
        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(MY_UUID);
            mBluetoothSocket.connect();
            mOutputStream = mBluetoothSocket.getOutputStream();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to connect", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void sendMessage() {
        if (mOutputStream == null) {
            Toast.makeText(this, "Please select the receiver device", Toast.LENGTH_SHORT).show();
            return;
        }

        String message = mMessageEditText.getText().toString().trim();
        if (message.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            mOutputStream.write(message.getBytes());
            mMessageEditText.setText("");
            Toast.makeText(this, "Message sent", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to send message", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBluetoothSocket != null) {
            try {
                mBluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}