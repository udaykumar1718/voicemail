package com.example.abanoub.voicebasedemailsystem;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class ForgetPasswordActivity extends Activity {

    NewUser newUser;
    EditText username, secretAnswer, newPassword, confirmPassword;
    TextView secretQuestion;
    Button getSecretQuestion_btn, update_btn;
    LinearLayout secretQA_password_linear;
    boolean isEmailFound = false;

    FirebaseUser firebaseUser;
    FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference personalDataReference, usersReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forget_password);

        username = (EditText) findViewById(R.id.username);
        secretQA_password_linear = findViewById(R.id.secretQA_password_linear);
        secretQuestion = findViewById(R.id.secretQuestion);
        secretAnswer = findViewById(R.id.secretAnswerED);
        newPassword = (EditText) findViewById(R.id.newPassword);
        confirmPassword = (EditText) findViewById(R.id.confirmPassword);
        getSecretQuestion_btn = (Button) findViewById(R.id.getSecretQA_btn);
        update_btn = (Button) findViewById(R.id.update_btn);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        usersReference = firebaseDatabase.getReference().child("Users");

        getSecretQuestion_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Utilities.isNetworkAvailable(ForgetPasswordActivity.this)) {

                    usersReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ArrayList<UserEmail> userEmails = Utilities.getAllUsersEmails(dataSnapshot);
                            String currentEmail = username.getText().toString() + "@vmail.com";
                            for (int i = 0; i < userEmails.size(); i++) {
                                if (currentEmail.equals(userEmails.get(i).email)) {
                                    isEmailFound = true;
                                }
                            }
                            if (isEmailFound) {
                                secretQA_password_linear.setVisibility(View.VISIBLE);
                                update_btn.setVisibility(View.VISIBLE);
                                personalDataReference = firebaseDatabase.getReference()
                                        .child(currentEmail.replace(".", "_")).child("PersonalData");
                                personalDataReference.addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        newUser = Utilities.getPersonalData(dataSnapshot);
                                        secretQuestion.setText(newUser.SecretQuestion);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                        Log.e("onCancelled: ", databaseError.toString());
                                    }
                                });
                            } else {
                                secretQA_password_linear.setVisibility(View.INVISIBLE);
                                update_btn.setVisibility(View.INVISIBLE);
                                secretQuestion.setText("");
                                Toast.makeText(ForgetPasswordActivity.this, "Wrong email address", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Log.e("onCancelled: ", databaseError.toString());
                        }
                    });
                } else
                    Toast.makeText(ForgetPasswordActivity.this, R.string.check_internet_connection, Toast.LENGTH_SHORT).show();
            }
        });


        update_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Utilities.isNetworkAvailable(ForgetPasswordActivity.this)) {
                    if (TextUtils.isEmpty(secretAnswer.getText()) || TextUtils.isEmpty(newPassword.getText())
                            || TextUtils.isEmpty(confirmPassword.getText()))
                        Toast.makeText(ForgetPasswordActivity.this, R.string.fields_cannot_be_empty, Toast.LENGTH_SHORT).show();
                    else {
                        if (newUser != null) {
                            if (secretAnswer.getText().toString().equals(newUser.SecretAnswer)) {
                                if (newPassword.getText().toString().equals(confirmPassword.getText().toString())) {

                                    firebaseAuth.signInWithEmailAndPassword(username.getText() + "@vmail.com", newUser.password)
                                            .addOnCompleteListener(ForgetPasswordActivity.this, new OnCompleteListener<AuthResult>() {
                                                @Override
                                                public void onComplete(@NonNull Task<AuthResult> task) {
                                                    if (task.isSuccessful()) {

                                                        firebaseUser = Utilities.getCurrentUser();
                                                        if (firebaseUser != null) {
                                                            firebaseUser.updatePassword(newPassword.getText().toString())
                                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> task) {
                                                                            if (task.isSuccessful()) {
                                                                                // updating password success
                                                                                newUser.password = newPassword.getText().toString();
                                                                                personalDataReference.child(newUser.pushID).setValue(newUser);
                                                                                Toast.makeText(ForgetPasswordActivity.this, "Password updated successfully", Toast.LENGTH_SHORT).show();
                                                                                Intent intent = new Intent(ForgetPasswordActivity.this, MainActivity.class);
                                                                                startActivity(intent);
                                                                            } else {
                                                                                // If updating fails, display a message to the user.
                                                                                Toast.makeText(ForgetPasswordActivity.this, "Password updating failed", Toast.LENGTH_SHORT).show();
                                                                            }
                                                                        }
                                                                    });
                                                        }
                                                    } else
                                                        Toast.makeText(ForgetPasswordActivity.this, R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                } else
                                    Toast.makeText(ForgetPasswordActivity.this, R.string.passwords_donot_match, Toast.LENGTH_SHORT).show();
                            } else
                                Toast.makeText(ForgetPasswordActivity.this, "Wrong secret answer", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else
                    Toast.makeText(ForgetPasswordActivity.this, R.string.check_internet_connection, Toast.LENGTH_SHORT).show();
            }
        });

        username.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                isEmailFound = false;
                if (charSequence.toString().trim().length() > 0)
                    getSecretQuestion_btn.setEnabled(true);
                else
                    getSecretQuestion_btn.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        secretAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0 && TextUtils.isEmpty(newPassword.getText()) == false
                        && TextUtils.isEmpty(confirmPassword.getText()) == false)
                    update_btn.setEnabled(true);
                else
                    update_btn.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0 && TextUtils.isEmpty(secretAnswer.getText()) == false
                        && TextUtils.isEmpty(confirmPassword.getText()) == false)
                    update_btn.setEnabled(true);
                else
                    update_btn.setEnabled(false);

            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.toString().trim().length() > 0 && TextUtils.isEmpty(newPassword.getText()) == false
                        && TextUtils.isEmpty(secretAnswer.getText()) == false)
                    update_btn.setEnabled(true);
                else
                    update_btn.setEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }
}
