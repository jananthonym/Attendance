package com.mobileapp.attendance;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final EditText nme = (EditText) findViewById(R.id.name);
        nme.setHint("Last Name, First Name");
        name = nme.toString();

        final Button student = (Button) findViewById(R.id.studentBtn);
        student.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                if(nme.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please Enter Name", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                name = nme.getText().toString();

                Intent intent = new Intent(MainActivity.this, StudentActivity.class);
                intent.putExtra("name", name);
                startActivity(intent);

            }
        });
        final Button prof = (Button) findViewById(R.id.teacher);
        prof.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click

                if(nme.getText().toString().isEmpty()){
                    Toast.makeText(getApplicationContext(), "Please Enter Name", Toast.LENGTH_LONG)
                            .show();
                    return;
                }
                name = nme.getText().toString();

                Intent intent = new Intent(MainActivity.this, ProfessorActivity.class);
                startActivity(intent);

            }
        });
    }
}
