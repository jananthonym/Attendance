package com.mobileapp.attendance;

import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.mobileapp.attendance.helper.DatabaseHelper;
import com.mobileapp.attendance.model.*;
import com.mobileapp.attendance.model.Class;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.io.File;
import java.util.Date;
import jxl.*;
import jxl.write.*;

public class ClassActivity extends AppCompatActivity {
    private String name;
    private ArrayAdapter mArrayAdapter;
    private DatabaseHelper db;
    private String TAG = ClassActivity.this.getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class);

        db = new DatabaseHelper(getApplicationContext());


        Intent intent = getIntent();
        name = intent.getStringExtra("class");

        TextView title = (TextView) findViewById(R.id.classTxt);
        title.setText(name);

        mArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1);

        final ListView students = (ListView) findViewById(R.id.listView3);
        students.setAdapter(mArrayAdapter);

        List<Student> studs = db.getAllStudentsByClass(name);
        List<String> studName = new ArrayList<>();
        for(Student stu: studs)
            studName.add(stu.getName()+"\n"+stu.getMac());

        //sort alphabetically
        Collections.sort(studName, new Comparator<String>() {
            @Override
            public int compare(String text1, String text2) {
                return text1.compareToIgnoreCase(text2);
            }
        });

        for(String stu: studName)
            mArrayAdapter.add(stu);

        Button export = (Button) findViewById(R.id.export);
        export.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Export();
            }
        });

    }

    @Override
    protected void onPause(){
        super.onPause();
        db.closeDB();
    }

    private void Export(){
        String file = name.substring(0, name.indexOf(" -")) + getDateTime() + ".xls";
        File sheetFile = new File(Environment.getExternalStorageDirectory(), file);
        try{
            WritableWorkbook workbook = Workbook.createWorkbook(sheetFile);
            WritableSheet sheet = workbook.createSheet(file, 0);

            //get list of the same classes from other dates
            List<Class> classes = db.getAllClasses();
            List<Class> classes2 = new ArrayList<>();
            for(Class cla: classes)
                if(cla.getName().contains(name.substring(0, name.indexOf(" -"))))
                    classes2.add(cla);

            int i = -1;
            for(Class cla: classes2){
                Label label = new Label(++i, 0, cla.getName().substring(cla.getName().indexOf('-')+2));
                sheet.addCell(label);
                Log.i(TAG, "Class: "+cla.getName());
                List<Student> studs = db.getAllStudentsByClass(cla.getName());
                int j=1;
                for(Student stu:studs){
                    Log.i(TAG, "    Student: " + stu.getName());
                    Label na = new Label(i, j++, stu.getName());
                    sheet.addCell(na);
                }

            }

            workbook.write();
            workbook.close();
            Toast.makeText(getApplicationContext(), "Finished Exporting to: " + file , Toast.LENGTH_LONG)
                    .show();
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), "Failed to export" , Toast.LENGTH_LONG);
            e.printStackTrace();
        }

    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "_MM-dd-yy_HH-mm", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
}
