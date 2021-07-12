package com.rohith.autobotserver;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 100;

    public static FirebaseDatabase mDatabase;
    com.rohith.autobotserver.Navigation nav;

    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    SharedPreferences prefs;


    public BluetoothAdapter bluetoothAdapter;
    public InputStream btInStream;
    public OutputStream btOutStream;
    public BluetoothSocket btSocket;
    EditText macText, sendText;
    Button button, sButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this, // Activity
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        }

        mDatabase = FirebaseDatabase.getInstance();
        nav = new Navigation(this);
        macText = findViewById(R.id.macText);
        sendText = findViewById(R.id.sendText);
        button = findViewById(R.id.conButton);
        sButton = findViewById(R.id.sendButton);
        sButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    btOutStream.write(sendText.getText().toString().toCharArray()[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(MainActivity.this, "Bluetooth is not available!", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, /*REQUEST_ENABLE_BLUETOOTH*/1);
        }

        prefs = getPreferences(MODE_PRIVATE);
        String mac = prefs.getString("mac", null);
        if (mac != null) macText.setText(mac);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    String mac = macText.getText().toString();
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("mac", mac);
                    editor.apply();

                    BluetoothDevice dev = bluetoothAdapter.getRemoteDevice(mac);
                    btSocket = dev.createRfcommSocketToServiceRecord(MY_UUID);
                    System.out.println("Connecting..." + btSocket);
                    btSocket.connect();
                    System.out.println("Connected");
                    btInStream = btSocket.getInputStream();
                    btOutStream = btSocket.getOutputStream();

                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    Toast.makeText(MainActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        });
    }
}
