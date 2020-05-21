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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignUp3Activity extends AppCompatActivity {

    EditText phoneNumberED;
    Spinner secretQuestionSpinner;
    EditText secretAnswerED;
    Button finish_btn;
    NewUser newUser;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference personalDataReference;
    DatabaseReference usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up3);

        newUser = (NewUser) getIntent().getSerializableExtra("newUser");

        firebaseDatabase = FirebaseDatabase.getInstance();
        personalDataReference = firebaseDatabase.getReference().child(Utilities.getModifiedCurrentEmail()).child("PersonalData");
        usersReference = firebaseDatabase.getReference().child("Users");

        phoneNumberED = (EditText) findViewById(R.id.phoneNumberED);
        secretQuestionSpinner = (Spinner) findViewById(R.id.secretQuestionSpinner);
        secretAnswerED = (EditText) findViewById(R.id.secretAnswerED);
        finish_btn = (Button) findViewById(R.id.finish_btn);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,R.layout.spinner_item,getResources().getStringArray(R.array.secretQuestions));
        adapter.setDropDownViewResource(R.layout.spinner_item);
        secretQuestionSpinner.setAdapter(adapter);

        finish_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(phoneNumberED.getText()) || TextUtils.isEmpty(secretAnswerED.getText()))
                    Toast.makeText(SignUp3Activity.this, R.string.fields_cannot_be_empty, Toast.LENGTH_SHORT).show();
                else {
                    newUser.MobileNumber = phoneNumberED.getText().toString();
                    newUser.SecretQuestion = secretQuestionSpinner.getSelectedItem().toString();
                    newUser.SecretAnswer = secretAnswerED.getText().toString();

                    insertToDatabase();
                }
            }
        });
    }

    private void insertToDatabase() {
        newUser.pushID = personalDataReference.push().getKey();

        personalDataReference.child(newUser.pushID).setValue(newUser);

        UserEmail userEmail = new UserEmail(newUser.Email, usersReference.push().getKey());
        usersReference.child(userEmail.pushID).setValue(userEmail);

        Toast.makeText(SignUp3Activity.this, "Account created successfully", Toast.LENGTH_LONG).show();

        startActivity(new Intent(SignUp3Activity.this, MainActivity.class));
    }
}