package com.example.bluetoothapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeviceScanActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private boolean scanning = false;
    private Handler handler = new Handler();
    private static final long SCAN_PERIOD = 10000; // 10 seconds

    private RecyclerView recyclerView;
    private DeviceAdapter deviceAdapter;
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private Map<String, BluetoothDevice> deviceMap = new HashMap<>();

    private Button scanToggleButton;
    private TextView scanStatusTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_scan);

        recyclerView = findViewById(R.id.recyclerView);
        scanToggleButton = findViewById(R.id.scanToggleButton);
        scanStatusTextView = findViewById(R.id.scanStatusTextView);

        // Set up RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        deviceAdapter = new DeviceAdapter(deviceList, new DeviceAdapter.OnDeviceClickListener() {
            @Override
            public void onDeviceClick(BluetoothDevice device) {
                // Stop scanning when a device is selected
                if (scanning) {
                    scanning = false;
                    bluetoothLeScanner.stopScan(scanCallback);
                    scanToggleButton.setText("Start Scan");
                }

                // Start the DataActivity with the selected device
                Intent intent = new Intent(DeviceScanActivity.this, DataActivity.class);
                intent.putExtra("deviceAddress", device.getAddress());
                startActivity(intent);
            }
        });
        recyclerView.setAdapter(deviceAdapter);

        // Initialize Bluetooth
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        scanToggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!scanning) {
                    // Start scanning
                    deviceList.clear();
                    deviceMap.clear();
                    deviceAdapter.notifyDataSetChanged();
                    startScan();
                } else {
                    // Stop scanning
                    scanning = false;
                    bluetoothLeScanner.stopScan(scanCallback);
                    scanToggleButton.setText("Start Scan");
                    scanStatusTextView.setText("Scan stopped");
                }
            }
        });

        // Start scanning automatically when the activity opens
        startScan();
    }

    private void startScan() {
        scanStatusTextView.setText("Scanning for devices...");
        scanToggleButton.setText("Stop Scan");

        // Stop scanning after SCAN_PERIOD
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (scanning) {
                    scanning = false;
                    bluetoothLeScanner.stopScan(scanCallback);
                    scanToggleButton.setText("Start Scan");
                    scanStatusTextView.setText("Scan completed");
                }
            }
        }, SCAN_PERIOD);

        scanning = true;
        ScanSettings settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        bluetoothLeScanner.startScan(null, settings, scanCallback);
    }

    // Device scan callback
    private ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            BluetoothDevice device = result.getDevice();
            if (device != null && device.getName() != null) {
                // Add device to the list if it's not already there
                if (!deviceMap.containsKey(device.getAddress())) {
                    deviceMap.put(device.getAddress(), device);
                    deviceList.clear();
                    deviceList.addAll(deviceMap.values());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deviceAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Toast.makeText(DeviceScanActivity.this, "Scan failed with error: " + errorCode, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (scanning) {
            bluetoothLeScanner.stopScan(scanCallback);
            scanning = false;
        }
    }
}