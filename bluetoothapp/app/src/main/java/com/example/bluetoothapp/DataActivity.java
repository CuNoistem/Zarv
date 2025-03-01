package com.example.bluetoothapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import java.util.UUID;

public class DataActivity extends AppCompatActivity {

    private static final String TAG = "DataActivity";

    // UUIDs for HM-10 service and characteristic
    // Note: These UUIDs might need to be adjusted based on your specific HM-10 module
    private static final UUID HM10_SERVICE_UUID = UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    private static final UUID HM10_CHARACTERISTIC_UUID = UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");
    private static final UUID CLIENT_CHARACTERISTIC_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    private BluetoothAdapter bluetoothAdapter;
    private String deviceAddress;
    private BluetoothGatt bluetoothGatt;

    private TextView connectionStatusTextView;
    // Individual TextViews for each metric
    private TextView speedValueTextView;
    private TextView voltageValueTextView;
    private TextView currentValueTextView;
    private TextView powerValueTextView;
    private TextView carbonCreditsValueTextView;

    // Added the More Stats button
    private MaterialButton moreStatsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        // Get device address from intent
        deviceAddress = getIntent().getStringExtra("deviceAddress");
        if (deviceAddress == null) {
            Toast.makeText(this, "No device address provided", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize TextView references
        connectionStatusTextView = findViewById(R.id.connectionStatusTextView);
        speedValueTextView = findViewById(R.id.speedValueTextView);
        voltageValueTextView = findViewById(R.id.voltageValueTextView);
        currentValueTextView = findViewById(R.id.currentValueTextView);
        powerValueTextView = findViewById(R.id.powerValueTextView);
        carbonCreditsValueTextView = findViewById(R.id.carbonCreditsValueTextView);

        // Initialize the More Stats button
        moreStatsButton = findViewById(R.id.moreStatsButton);

        // Set click listener for the More Stats button
        moreStatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Navigate to the StatsActivity
                Intent statsIntent = new Intent(DataActivity.this, StatsActivity.class);

                // You can pass any data to the new activity if needed
                statsIntent.putExtra("deviceAddress", deviceAddress);

                // Add any real-time data you want to show in the stats activity
                statsIntent.putExtra("currentSpeed", speedValueTextView.getText().toString());
                statsIntent.putExtra("currentVoltage", voltageValueTextView.getText().toString());
                statsIntent.putExtra("currentAmperage", currentValueTextView.getText().toString());
                statsIntent.putExtra("currentPower", powerValueTextView.getText().toString());
                statsIntent.putExtra("carbonCredits", carbonCreditsValueTextView.getText().toString());

                startActivity(statsIntent);
            }
        });

        // Initialize Bluetooth
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();

        // Connect to the device
        connectionStatusTextView.setText("Connecting to " + deviceAddress + "...");
        connectToDevice();
    }

    private void connectToDevice() {
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(deviceAddress);
        bluetoothGatt = device.connectGatt(this, false, gattCallback);
    }

    // BluetoothGatt callback
    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    // Successfully connected, discover services
                    runOnUiThread(() -> connectionStatusTextView.setText("Connected, discovering services..."));
                    bluetoothGatt.discoverServices();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    // Disconnected
                    runOnUiThread(() -> {
                        connectionStatusTextView.setText("Disconnected");
                        Toast.makeText(DataActivity.this, "Device disconnected", Toast.LENGTH_SHORT).show();
                    });
                }
            } else {
                // Error in connecting
                runOnUiThread(() -> {
                    connectionStatusTextView.setText("Error connecting");
                    Toast.makeText(DataActivity.this, "Connection error: " + status, Toast.LENGTH_SHORT).show();
                });
                gatt.close();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Look for the HM-10 service
                BluetoothGattService hm10Service = gatt.getService(HM10_SERVICE_UUID);
                if (hm10Service != null) {
                    // Get the characteristic
                    BluetoothGattCharacteristic characteristic =
                            hm10Service.getCharacteristic(HM10_CHARACTERISTIC_UUID);
                    if (characteristic != null) {
                        // Enable notifications for the characteristic
                        boolean success = bluetoothGatt.setCharacteristicNotification(characteristic, true);
                        if (success) {
                            // Write to descriptor to enable notifications
                            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(CLIENT_CHARACTERISTIC_CONFIG);
                            if (descriptor != null) {
                                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                                bluetoothGatt.writeDescriptor(descriptor);
                                runOnUiThread(() -> connectionStatusTextView.setText("Connected and ready for data"));
                            }
                        }
                    }
                } else {
                    runOnUiThread(() -> {
                        connectionStatusTextView.setText("HM-10 service not found");
                        Toast.makeText(DataActivity.this, "HM-10 service not found on this device", Toast.LENGTH_LONG).show();
                    });
                }
            } else {
                runOnUiThread(() -> {
                    connectionStatusTextView.setText("Service discovery failed");
                    Toast.makeText(DataActivity.this, "Service discovery failed", Toast.LENGTH_SHORT).show();
                });
            }
        }

        private StringBuilder dataBuffer = new StringBuilder();

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (HM10_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                final byte[] data = characteristic.getValue();
                final String receivedData = new String(data).trim();

                Log.d(TAG, "Raw data received: " + receivedData);

                // Append to buffer
                dataBuffer.append(receivedData);

                // Check if we have a complete message (5 values)
                String bufferStr = dataBuffer.toString();
                String[] parts = bufferStr.split(",");

                // Process if we have a complete message
                if (parts.length >= 5) {
                    final String completeData = bufferStr;
                    dataBuffer = new StringBuilder(); // Clear buffer

                    runOnUiThread(() -> {
                        try {
                            String[] values = completeData.split(",");

                            // Update the individual TextViews
                            if (values.length >= 1) {
                                speedValueTextView.setText(values[0]);
                            }

                            if (values.length >= 2) {
                                voltageValueTextView.setText(values[1]);
                            }

                            if (values.length >= 3) {
                                currentValueTextView.setText(values[2]);
                            }

                            if (values.length >= 4) {
                                powerValueTextView.setText(values[3]);
                            }

                            if (values.length >= 5) {
                                carbonCreditsValueTextView.setText(values[4]);
                            }

                        } catch (Exception e) {
                            // Show error in connection status
                            connectionStatusTextView.setText("Error parsing data: " + e.getMessage());
                            Log.e(TAG, "Error parsing data: " + e.getMessage(), e);
                        }
                    });
                } else {
                    // Update UI to show we're waiting for more data
                    runOnUiThread(() -> {
                        connectionStatusTextView.setText("Receiving data... (" + parts.length + "/5 values)");

                        // Clear all values while waiting for complete data
                        speedValueTextView.setText("--");
                        voltageValueTextView.setText("--");
                        currentValueTextView.setText("--");
                        powerValueTextView.setText("--");
                        carbonCreditsValueTextView.setText("--");
                    });
                }
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }
}