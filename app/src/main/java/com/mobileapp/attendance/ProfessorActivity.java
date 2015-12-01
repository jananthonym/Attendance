package com.mobileapp.attendance;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobileapp.attendance.helper.DatabaseHelper;
import com.mobileapp.attendance.model.*;
import com.mobileapp.attendance.model.Class;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class ProfessorActivity extends AppCompatActivity {
    private static UUID MY_UUID = UUID.fromString("fb36491d-7c21-40ef-9f67-a63237b5bbea");
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1, sCount=0;
    private TextView number;
    private EditText course;
    private long classId;
    private String courseName;
    private DatabaseHelper db;
    private boolean mode=true, mode2=false;
    private ArrayAdapter mArrayAdapter;
    private boolean endAccept = false;

    private String TAG = ProfessorActivity.this.getClass().getSimpleName();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_professor);

        db = new DatabaseHelper(getApplicationContext());

        mArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);

        final ListView students = (ListView) findViewById(R.id.listView2);
        students.setAdapter(mArrayAdapter);

        //setup bluetooth
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }

        course = (EditText) findViewById(R.id.classTxt);
        course.setHint("Class Name");

        number = (TextView) findViewById(R.id.numberTxt);
        number.setText(sCount + " Signed In");

        final Button viewBtn = (Button) findViewById(R.id.viewBtn);
        viewBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mArrayAdapter.clear();
                List<Class> classes = db.getAllClasses();
                for(Class cla: classes)
                    mArrayAdapter.add(cla.getName());
                mode2=true;
            }
        });

        final Button attend = (Button) findViewById(R.id.attendBtn);
        attend.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                if (mode) {

                    if (course.getText().toString().isEmpty()) {
                        Toast.makeText(getApplicationContext(), "Please Enter Class Name", Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    mode = false;
                    mode2=false;
                    attend.setText("Stop Attendance");
                    viewBtn.setEnabled(false);
                    mArrayAdapter.clear();

                    //create new class
                    courseName = course.getText().toString() + getDateTime();
                    Class cla = new Class(courseName);
                    classId = db.createClass(cla);
                    mBluetoothAdapter.setName("MAE_"+courseName);

                    //make discoverable
                    Intent discoverableIntent = new
                            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 0);
                    startActivity(discoverableIntent);

                    //accept connections
                    AcceptThread acceptThread = new AcceptThread();
                    acceptThread.start();

                } else {
                    mode = true;
                    attend.setText("Start Attendance");
                    viewBtn.setEnabled(true);

                    //stop accepting
                    //acceptThread.cancel();
                    endAccept=true;
                    mBluetoothAdapter.setName(MainActivity.name);
                    //turn of discoverable
                    Intent discoverableIntent = new
                            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 1);
                    startActivity(discoverableIntent);
                }
            }
        });



        students.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                // ListView Clicked item index
                int itemPosition     = position;

                // ListView Clicked item value
                String  itemValue    = (String) students.getItemAtPosition(position);

                // Show Alert
                //Toast.makeText(getApplicationContext(),
                //        "Position :"+itemPosition+"  ListItem : " +itemValue , Toast.LENGTH_LONG)
                //        .show();

                if(mode2){
                    Intent intent = new Intent(ProfessorActivity.this, ClassActivity.class);
                    intent.putExtra("class", itemValue);
                    startActivity(intent);
                }
            }

        });
    }

    @Override
    protected void onPause(){
        super.onPause();
        db.closeDB();
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                " - MM/dd/yy HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    private class ManageThread extends Thread {
        BluetoothSocket mmSocket;
        InputStream inStream;
        OutputStream outStream;

        public ManageThread(BluetoothSocket socket){
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the input and output streams, using temp objects because
            // member streams are final
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
            final BluetoothDevice dev = mmSocket.getRemoteDevice();

            while(true) {
                try {
                    bytes = inStream.read(buffer);

                    if(bytes>0) {
                        //tell client name was recieved

                        //get student's name
                        final String name2 = new String(buffer, 0, bytes);
                        Log.i(TAG, "Client name: " + name2);


                        final Student stu = new Student(name2, dev.getAddress());
                        boolean add = true;
                        //check if phoen aplready signed in
                        List<Student> stud = db.getAllStudentsByClass(courseName);
                        for (Student s : stud)
                            if (s.getMac().equals(stu.getMac()))
                                add = false;

                        if (add) {
                            write("yes".getBytes());

                            db.createStudent(stu, classId);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    number.setText(++sCount + " Signed In");
                                    mArrayAdapter.add(stu.getName());

                                }
                            });
                        }else{
                            write("no".getBytes());

                        }
                        try {
                            mmSocket.close();
                        } catch (IOException e) {
                            break;
                        }
                        break;
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
        public void write(byte[] bytes) {
            try {
                outStream.write(bytes);
            } catch (IOException e) { }
        }
    }

    private class AcceptThread extends Thread {
        private final BluetoothServerSocket mmServerSocket;
        private InputStream inStream;


        public AcceptThread() {
            // Use a temporary object that is later assigned to mmServerSocket,
            // because mmServerSocket is final
            BluetoothServerSocket tmp = null;
            try {
                // MY_UUID is the app's UUID string, also used by the client code
                tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("MAE_" + courseName
                        + " - " + MainActivity.name, MY_UUID);
                Toast.makeText(getApplicationContext(), "Broadcasting as: MAE_"+courseName, Toast.LENGTH_LONG)
                        .show();
            } catch (IOException e) { }
            mmServerSocket = tmp;
        }

        public void run() {
            BluetoothSocket socket=null;

            // Keep listening until exception occurs or a socket is returned
            while (true) {
                if(endAccept){
                    endAccept=false;
                    break;
                }
                try {
                    socket = mmServerSocket.accept();

                } catch (IOException e) {
                }
                // If a connection was accepted
                if (socket != null) {
                    Log.i(TAG, "Someone COnnected");
                    // Do work to manage the connection (in a separate thread)
                    ManageThread m = new ManageThread(socket);
                    m.start();
                }



            }

        }

        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                mmServerSocket.close();
            } catch (IOException e) { }
        }
    }
}
