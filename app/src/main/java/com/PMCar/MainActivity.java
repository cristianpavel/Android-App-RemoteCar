package com.PMCar;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.UUID;

import static android.widget.Toast.makeText;


public class MainActivity extends AppCompatActivity {


    private Handler mTimerHandler;
    private Button mConnect;
    private Button mDisconnect;
    private Button mRemoteControl;
    private BluetoothAdapter mBTAdapter;

    private final String TAG = MainActivity.class.getSimpleName();
    private Handler mHandler; // Our main handler that will receive callback notifications
    public static ConnectedThread mConnectedThread; // bluetooth background worker thread to send and receive data
    private BluetoothSocket mBTSocket = null; // bi-directional client-to-client data path

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); // "random" unique identifier


    // #defines for identifying shared types between calling functions
    private final static int MESSAGE_READ = 2; // used in bluetooth handler to identify message update
    private final static int CONNECTED = 3; // used in bluetooth handler to identify message status
    private final static int NOT_CONNECTED = 3;
    private final static String HC_05_MAC = "98:D3:51:FD:96:00";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDisconnect =(Button)findViewById(R.id.dc);
        mConnect = (Button)findViewById(R.id.connect);
        mRemoteControl = (Button)findViewById(R.id.remote);
        mTimerHandler = new Handler();
        mBTAdapter = BluetoothAdapter.getDefaultAdapter(); // get a handle on the bluetooth radio


        mHandler = new Handler() {
            public void handleMessage(android.os.Message msg){
                if(msg.what == MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }

                    if (readMessage.charAt(0) == '0') {
                        Joystick.mCarMessage.setText("Obstacle ahead. Brakes activated.");
                        mTimerHandler.removeCallbacks(mClearText);
                        mTimerHandler.postDelayed(mClearText, 2000);
                    }


                }
            }
        };
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            makeText(this, "BT Not Supported", Toast.LENGTH_SHORT).show();
            finish();
        }



        mConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!mBTAdapter.isEnabled()) {
                    Toast.makeText(getBaseContext(), "Bluetooth not on", Toast.LENGTH_SHORT).show();
                    return;
                }
                // Get the device MAC address, which is the last 17 chars in the View

                if (mBTSocket != null && mBTSocket.isConnected()) {
                    Toast.makeText(getBaseContext(), "Already connected.", Toast.LENGTH_LONG).show();
                    return;
                }

                final String address = HC_05_MAC;
                final String name = "HC-05";

                // Spawn a new thread to avoid blocking the GUI one
                new Thread()
                {
                    public void run() {
                        boolean fail = false;

                        BluetoothDevice device = mBTAdapter.getRemoteDevice(address);


                        try {
                            mBTSocket = createBluetoothSocket(device);
                        } catch (Exception e) {
                            fail = true;
                            mHandler.obtainMessage(NOT_CONNECTED, -1, -1)
                                    .sendToTarget();
                            toastGUI("Something wrong. Not connected.");

                        }
                        // Establish the Bluetooth socket connection.
                        try {
                            mBTSocket.connect();
                        } catch (Exception e) {
                            try {
                                fail = true;
                                mBTSocket.close();
                                mHandler.obtainMessage(NOT_CONNECTED, -1, -1)
                                        .sendToTarget();
                                toastGUI("Something wrong. Not connected.");
                            } catch (IOException e2) {
                                //insert code to deal with this
                            }
                        }
                        if(fail == false) {
                            mConnectedThread = new ConnectedThread(mBTSocket);
                            mConnectedThread.start();

                            mHandler.obtainMessage(CONNECTED, 1, -1, name)
                                    .sendToTarget();
                            toastGUI("Connected. Press remote control for playing.");

                        }
                    }
                }.start();

            }
        });




        mDisconnect.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (mBTAdapter == null ||
                        mBTSocket == null)
                    return;
                try {
                    mBTAdapter.startDiscovery();
                    mBTSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

               Toast.makeText(getApplicationContext(),"Bluetooth turned Off", Toast.LENGTH_SHORT).show();

            }
        });

        mRemoteControl.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mBTSocket == null || !mBTSocket.isConnected()) {
                    Toast.makeText(getApplicationContext(), "Not connected to HC-05",
                            Toast.LENGTH_LONG).show();
                    return;
                }

                Intent intent = new Intent(v.getContext(), Joystick.class);
                startActivity(intent);


            }
        });



    };

    private Runnable mClearText = new Runnable() {
        @Override
        public void run() {

            Joystick.mCarMessage.setText("All things operational.");

        }
    };


    private void toastGUI(String s) {

        final String text = s;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();

            }
        });

    }


    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {
        try {
            final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", UUID.class);
            return (BluetoothSocket) m.invoke(device, BTMODULEUUID);
        } catch (Exception e) {
            Log.d(TAG, "Could not create Insecure RFComm Connection",e);
        }
        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    public class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()
            // Keep listening to the InputStream until an exception occurs
            while (true) {
                try {

                    if (!mmSocket.isConnected()) {
                        mmSocket.close();
                        return;
                    }
                    // Read from the InputStream
                    buffer = new byte[1024];
                    bytes = mmInStream.read(buffer); // record how many bytes we actually read
                    mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget(); // Send the obtained bytes to the UI activity

                } catch (IOException e) {
                    e.printStackTrace();
                    cancel();
                    break;
                }
            }
        }

        /* Call this from the main activity to send data to the remote device */
        public void write(String input) {
            byte[] bytes = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) { }
        }

        /* Call this from the main activity to shutdown the connection */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }




}


