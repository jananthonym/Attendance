package com.mobileapp.attendance;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.mobileapp.attendance.model.Student;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class StudentActivity extends AppCompatActivity {
    private static UUID MY_UUID = UUID.fromString("fb36491d-7c21-40ef-9f67-a63237b5bbea");
    private int REQUEST_ENABLE_BT = 1;
    private Set<BluetoothDevice> pairedDevices;
    private List<BluetoothDevice> devices = new ArrayList<>();
    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter mArrayAdapter;
    private String TAG = StudentActivity.this.getClass().getSimpleName();
    private String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        mArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
        Intent intent = getIntent();
         name = intent.getStringExtra("name");
        mBluetoothAdapter.setName(name);


/*
        //Checked paired devices
        pairedDevices = mBluetoothAdapter.getBondedDevices();
        // If there are paired devices
        if (pairedDevices.size() > 0) {
            // Loop through paired devices
            for (BluetoothDevice device : pairedDevices) {
                // Add the name and address to an array adapter to show in a ListView
                if(device.getName().contains("MAE_")) {
                    devices.add(device);
                    mArrayAdapter.add(device.getName());
                }
            }
        }
*/
        final ListView classes = (ListView) findViewById(R.id.listView);
        classes.setAdapter(mArrayAdapter);

        // Register the BroadcastReceiver to look for devices
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy

        final Button search = (Button) findViewById(R.id.searchBtn);
        search.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                mArrayAdapter.clear();
                devices.clear();
                mBluetoothAdapter.startDiscovery();
            }
        });

        classes.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) classes.getItemAtPosition(position);

                // Show Alert
                //Toast.makeText(getApplicationContext(),
                //        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                //        .show();

                ConnectThread connect = new ConnectThread(devices.get(itemPosition));
                connect.start();

            }

        });

    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                if(device!=null) {
                    if(device.getName()!=null && device.getName().contains("MAE_")) {
                        devices.add(device);
                        mArrayAdapter.add(device.getName().substring(4));
                    }
                }
            }
        }
    };

    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket,
            // because mmSocket is final
            BluetoothSocket tmp = null;
            mmDevice = device;

            // Get a BluetoothSocket to connect with the given BluetoothDevice
            try {
                // MY_UUID is the app's UUID string, also used by the server code
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) { }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it will slow down the connection
            mBluetoothAdapter.cancelDiscovery();
            try {
                // Connect the device through the socket. This will block
                // until it succeeds or throws an exception
                mmSocket.connect();

            } catch (IOException connectException) {
                // Unable to connect; close the socket and get out
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Sign In Failed" , Toast.LENGTH_LONG).show();

                    }
                });
                try {
                    mmSocket.close();
                } catch (IOException closeException) { }
                return;
            }

            // Do work to manage the connection (in a separate thread)
            ManageThread m = new ManageThread(mmSocket);
            m.start();
        }

        /** Will cancel an in-progress connection, and close the socket */
        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) { }
        }
    }

    private class ManageThread extends Thread {
        BluetoothSocket mmSocket;
        InputStream inStream;
        OutputStream outStream;


        public ManageThread(BluetoothSocket socket){
            mmSocket=socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            inStream = tmpIn;
            outStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024];  // buffer store for the stream
            int bytes; // bytes returned from read()

            while(true) {
                try {
                    //send name until acknowledged
                    do{
                        write(name.getBytes());

                        bytes = inStream.read(buffer);
                    }while(bytes==0);
                    if(new String(buffer,0,bytes).contains("no")){
                        //couldn't sign in
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Device Already Signed in" , Toast.LENGTH_LONG).show();

                            }
                        });
                    }else{
                        //success
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Signed In" , Toast.LENGTH_LONG).show();

                            }
                        });
                    }

                    //close socket, done here
                    mmSocket.close();
                    break;
                } catch (IOException e) {
                    //break;
                }
            }
        }
        public void write(byte[] bytes) {
            try {
                outStream.write(bytes);
            } catch (IOException e) { }
        }
    }
}
