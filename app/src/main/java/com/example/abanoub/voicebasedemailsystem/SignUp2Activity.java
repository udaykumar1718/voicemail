package com.example.abanoub.voicebasedemailsystem;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class SignUp2Activity extends AppCompatActivity {

    Spinner monthSpinner,daySpinner,yearSpinner;
    Spinner genderSpinner;
    Button next;
    NewUser newUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up2);

        newUser = (NewUser) getIntent().getSerializableExtra("newUser");

        monthSpinner = (Spinner) findViewById(R.id.monthSpinner);
        daySpinner = (Spinner) findViewById(R.id.daySpinner);
        yearSpinner = (Spinner) findViewById(R.id.yearSpinner);
        genderSpinner = (Spinner) findViewById(R.id.genderSpinner);
        next = (Button) findViewById(R.id.next);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,R.layout.spinner_item,getResources().getStringArray(R.array.months));
        adapter.setDropDownViewResource(R.layout.spinner_item);
        monthSpinner.setAdapter(adapter);

        adapter =new ArrayAdapter<>(
                this,R.layout.spinner_item,getResources().getStringArray(R.array.days));
        adapter.setDropDownViewResource(R.layout.spinner_item);
        daySpinner.setAdapter(adapter);

        adapter = new ArrayAdapter<>(
                this,R.layout.spinner_item,getResources().getStringArray(R.array.years));
        adapter.setDropDownViewResource(R.layout.spinner_item);
        yearSpinner.setAdapter(adapter);

        adapter = new ArrayAdapter<>(
                this,R.layout.spinner_item,getResources().getStringArray(R.array.gender));
        adapter.setDropDownViewResource(R.layout.spinner_item);
        genderSpinner.setAdapter(adapter);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    newUser.BirthDate = daySpinner.getSelectedItem().toString() + "-" + monthSpinner.getSelectedItem().toString()
                            + "-" + yearSpinner.getSelectedItem().toString();
                    newUser.Gender = genderSpinner.getSelectedItem().toString();
                    Intent intent = new Intent(SignUp2Activity.this, SignUp3Activity.class);
                    intent.putExtra("newUser", newUser);
                    startActivity(intent);
            }
        });
    }
}

