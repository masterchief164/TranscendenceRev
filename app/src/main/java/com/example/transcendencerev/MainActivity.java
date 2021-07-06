package com.example.transcendencerev;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private final static int CONNECTING_STATUS = 1;
    private final static int MESSAGE_READ = 2;
    public static int CONNECTED_STATUS = 0;
    public static Handler handler;
    public static BluetoothSocket mmSocket;
    public static ConnectedThread connectedThread;
    public static CreateConnectThread createConnectThread;
    private String deviceName = null;
    public BluetoothAdapter bluetoothAdapter = null;
    ProgressBar progressBar;
    Button buttonConnect, forward, backward, left, right, up, down, grip, release, ptUp, ptDown;


    static final int req = 123;


    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //                              UI Initialization
        final Toolbar toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.progressBar);
        buttonConnect = findViewById(R.id.bluetoothConnect);
        forward = findViewById(R.id.fwd);
        backward = findViewById(R.id.bwd);
        left = findViewById(R.id.left);
        right = findViewById(R.id.right);
        up = findViewById(R.id.climb);
        down = findViewById(R.id.descend);
        grip = findViewById(R.id.grip);
        release = findViewById(R.id.release);
        ptUp = findViewById(R.id.ptUp);
        ptDown = findViewById(R.id.ptDown);
        progressBar.setVisibility(View.GONE);
        forward.setEnabled(false);
        backward.setEnabled(false);
        left.setEnabled(false);
        right.setEnabled(false);
        up.setEnabled(false);
        down.setEnabled(false);
        grip.setEnabled(false);
        release.setEnabled(false);
        ptDown.setEnabled(false);
        ptUp.setEnabled(false);


        //                          Checking Permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) +
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) +
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH) +
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.BLUETOOTH_ADMIN) +
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.INTERNET) +
                ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.BLUETOOTH_ADMIN) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.BLUETOOTH) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this,
                            Manifest.permission.RECORD_AUDIO) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(this
                            , Manifest.permission.INTERNET)) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Grant Permissions");
                builder.setMessage("Grant Bluetooth and Location Access");
                builder.setPositiveButton("OK", (dialog, which) ->
                        ActivityCompat.requestPermissions(
                                MainActivity.this,
                                new String[]{
                                        Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.BLUETOOTH_ADMIN,
                                        Manifest.permission.BLUETOOTH,
                                        Manifest.permission.RECORD_AUDIO,
                                        Manifest.permission.INTERNET
                                }, req
                        ));
                builder.setNegativeButton("Cancel", null);
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            } else {
                ActivityCompat.requestPermissions(
                        MainActivity.this,
                        new String[]{
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.BLUETOOTH_ADMIN,
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.BLUETOOTH,
                                Manifest.permission.RECORD_AUDIO,
                                Manifest.permission.INTERNET
                        }, req
                );
            }
        }
        // If a bluetooth device has been selected from SelectDeviceActivity
        deviceName = getIntent().getStringExtra("deviceName");
        if (deviceName != null) {
            // Get the device address to make BT Connection
            String deviceAddress = getIntent().getStringExtra("deviceAddress");
            // Show progress and connection status
            toolbar.setSubtitle("Connecting to " + deviceName + "...");
            progressBar.setVisibility(View.VISIBLE);

            /*
            This is the most important piece of code. When "deviceName" is found
            the code will call a new thread to create a bluetooth connection to the
            selected device (see the thread code below)
             */
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            createConnectThread = new CreateConnectThread(bluetoothAdapter, deviceAddress);
            createConnectThread.start();
        }




        /*
        Second most important piece of Code. GUI Handler
         */
        handler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.arg1) {
                    case 1:
                        toolbar.setSubtitle("Connected to " + deviceName);
                        forward.setEnabled(true);
                        backward.setEnabled(true);
                        left.setEnabled(true);
                        right.setEnabled(true);
                        up.setEnabled(true);
                        down.setEnabled(true);
                        grip.setEnabled(true);
                        release.setEnabled(true);
                        ptUp.setEnabled(true);
                        ptDown.setEnabled(true);
                        buttonConnect.setText(R.string.disconnect);
                        progressBar.setVisibility(View.GONE);
                        break;
                    case -1:
                        toolbar.setSubtitle("Device fails to connect");
                        progressBar.setVisibility(View.GONE);
                        break;
                }
            }
        };

        // Select Bluetooth Device
        buttonConnect.setOnClickListener(view -> {
            // Move to adapter list
            if (CONNECTED_STATUS == 0) {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                checkBTState();
            } else {
                CONNECTED_STATUS = 0;
                connectedThread.cancel();
                toolbar.setSubtitle("Disconnected");
                buttonConnect.setText(R.string.connect);
                forward.setEnabled(false);
                backward.setEnabled(false);
                left.setEnabled(false);
                right.setEnabled(false);
                up.setEnabled(false);
                down.setEnabled(false);
                grip.setEnabled(false);
                release.setEnabled(false);
                ptUp.setEnabled(false);
                ptDown.setEnabled(false);
            }
        });

        forward.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null)
                            return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null)
                            return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return true; // before I had written false
            }

            final Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    connectedThread.write("1");
                    mHandler.postDelayed(this, 50);
                }
            };

        });

        backward.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null)
                            return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null)
                            return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return true; // before I had written false
            }

            final Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    connectedThread.write("2");
                    mHandler.postDelayed(this, 50);
                }
            };

        });

        left.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null)
                            return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null)
                            return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return true; // before I had written false
            }

            final Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    connectedThread.write("3");
                    mHandler.postDelayed(this, 50);
                }
            };

        });

        right.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null)
                            return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null)
                            return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return true; // before I had written false
            }

            final Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    connectedThread.write("4");
                    mHandler.postDelayed(this, 50);
                }
            };

        });

        up.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null)
                            return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null)
                            return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return true; // before I had written false
            }

            final Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    connectedThread.write("5");
                    mHandler.postDelayed(this, 50);
                }
            };

        });
        down.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null)
                            return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null)
                            return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return true; // before I had written false
            }

            final Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    connectedThread.write("6");
                    mHandler.postDelayed(this, 50);
                }
            };

        });
        grip.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null)
                            return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null)
                            return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return true; // before I had written false
            }

            final Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    connectedThread.write("7");
                    mHandler.postDelayed(this, 50);
                }
            };

        });
        release.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null)
                            return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null)
                            return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return true; // before I had written false
            }

            final Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    connectedThread.write("8");
                    mHandler.postDelayed(this, 50);
                }
            };

        });
        ptUp.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null)
                            return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null)
                            return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return true; // before I had written false
            }

            final Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    connectedThread.write("9");
                    mHandler.postDelayed(this, 50);
                }
            };

        });
        ptDown.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null)
                            return true;
                        mHandler = new Handler();
                        mHandler.postDelayed(mAction, 50);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null)
                            return true;
                        mHandler.removeCallbacks(mAction);
                        mHandler = null;
                        break;
                }
                return true; // before I had written false
            }

            final Runnable mAction = new Runnable() {
                @Override
                public void run() {
                    connectedThread.write("0");
                    mHandler.postDelayed(this, 50);
                }
            };

        });
    }


    /* ======================= Terminate Connection at BackPress ====================== */
    @Override
    public void onBackPressed() {
        // Terminate Bluetooth Connection and close app
        if (createConnectThread != null) {
            createConnectThread.cancel();
        }
        Intent a = new Intent(Intent.ACTION_MAIN);
        a.addCategory(Intent.CATEGORY_HOME);
        a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(a);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == req) {
            if ((grantResults.length > 0) &&
                    (grantResults[0] +
                            grantResults[1] +
                            grantResults[2] +
                            grantResults[3] +
                            grantResults[4] +
                            grantResults[5] +
                            grantResults[6]
                            == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(getApplicationContext(),
                        "permissions are granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void checkBTState() {
        // Check for Bluetooth support and then check to make sure it is turned on
        //          Emulator doesn't support Bluetooth and will return null
        if (bluetoothAdapter.isEnabled()) {
            Log.d(TAG, "...Bluetooth ON...");
            Intent intent = new Intent(MainActivity.this,
                    SelectDeviceActivity.class);
            startActivity(intent);
        } else {
            //Prompt user to turn on Bluetooth
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 1);
        }
    }

    /* ================== Thread to Create Bluetooth Connection ========================== */
    public static class CreateConnectThread extends Thread {


        public CreateConnectThread(BluetoothAdapter bluetoothAdapter, String address) {
            /*
            Use a temporary object that is later assigned to mmSocket
            because mmSocket is final.
             */
            BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(address);
            BluetoothSocket tmp = null;
            UUID uuid = bluetoothDevice.getUuids()[0].getUuid();

            try {
                /*
                Get a BluetoothSocket to connect with the given BluetoothDevice.
                Due to Android device varieties,the method below
                 may not work fo different devices.
                You should try using other methods i.e. :
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
                 */
                tmp = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);


            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.

                Log.d("Status", "Trying to connect");
                mmSocket.connect();
                Log.d("Status", "Device connected");
                CONNECTED_STATUS = 1;
                handler.obtainMessage(CONNECTING_STATUS, 1, -1).sendToTarget();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                    Log.d("Status", "Cannot connect to device");
                    handler.obtainMessage(CONNECTING_STATUS, -1, -1).sendToTarget();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }

            // The connection attempt succeeded. Perform work associated with
            // the connection in a separate thread.
            try {
                connectedThread = new ConnectedThread(mmSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            connectedThread.run();
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /* =============================== Thread for Data Transfer ===================== */
    public static class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) throws IOException {
            InputStream tmpIn;
            OutputStream tmpOut;

            // Get the input and output streams, using temp objects because
            // member streams are final
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes = 0; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {
                    /*
                    Read from the InputStream from Arduino
                    until termination character is reached.
                    Then send the whole String message to GUI Handler.
                     */
                    buffer[bytes] = (byte) mmInStream.read();
                    String readMessage;
                    if (buffer[bytes] == '\n') {
                        readMessage = new String(buffer, 0, bytes);
                        Log.e("Arduino Message1", readMessage);
                        handler.obtainMessage(MESSAGE_READ, readMessage).sendToTarget();
                        bytes = 0;
                    } else {
                        bytes++;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes(); //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Log.e("Send Error", "Unable to send message", e);
            }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException ignored) {
            }
        }
    }
}