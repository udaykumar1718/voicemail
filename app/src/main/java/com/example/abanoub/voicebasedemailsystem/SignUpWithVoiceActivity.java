package com.example.abanoub.voicebasedemailsystem;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.Locale;

public class SignUpWithVoiceActivity extends Activity {

    NewUser newUser;
    EditText secretAnswerED;
    AutoCompleteTextView fullNameED, usernameED, passwordED, confirmPasswordED;
    TextView secretQuestion, Gotologin;
    Button signup_btn;

    String fullNameString, usernameString, passwordString, confirmPasswordString, secretAnswerString;
    Boolean isFullName = false, isUsername = false, isPassword = false, isConfimPassword = false, isSecretAnswer = false;
    boolean needToConfirmPassword = false;

    FirebaseAuth firebaseAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference personalDataReference, usersReference;

    //Handler work every x time
    Handler handler = new Handler();
    int delay = 2000; //1 second=1000 milisecond
    Runnable runnable;

    static String fullnameSpeech = "Please enter your fullname";
    static String usernameSpeech = "Please enter your username";
    static String passSpeech = "Please enter your password";
    static String confirmPassSpeech = "Please enter your password again to be confirmed";
    static String secretQuestionSpeech = "Your secret question is: What is the name of the city you born?";
    static String secretAnswerSpeech = "Please enter your secret answer";
    static String passError = "Password must be at least six character, Please enter your password again";
    static String confirmPassError = "confirm password again, Please enter your password again";
    static String signupSuccess = "Account created successfully and you are in the inbox page";

    //Text to speech API
    TextToSpeech txtToSpeech;
    //speech to text API
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up_with_voice);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();

        fullNameED = findViewById(R.id.fullName);
        usernameED = findViewById(R.id.username);
        passwordED = findViewById(R.id.password);
        confirmPasswordED = findViewById(R.id.confirmPassword);
        secretQuestion = findViewById(R.id.secretQuestion);
        secretAnswerED = findViewById(R.id.secretAnswerED);
        signup_btn = (Button) findViewById(R.id.signup_btn);
        Gotologin = (TextView) findViewById(R.id.login_link);

        txtToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    txtToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        Gotologin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignUpWithVoiceActivity.this, SignInWithVoiceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //it reuses an existing activity by bringing it to the front of the stack
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        //start handler as activity become visible
        handler.postDelayed(new Runnable() {
            public void run() {
                //do something
                getDataUsingVoice();
                runnable = this;
            }
        }, delay);

        super.onResume();
    }

    public void getDataUsingVoice() {
        if (fullNameString == null) {
            txtToSpeech.speak(fullnameSpeech, TextToSpeech.QUEUE_FLUSH, null);
            //make app wait for 2 seconds then resume executing
            try {
                Thread.sleep(1900);
            } catch (InterruptedException ex) {
                Log.d("exception", ex.toString());
            }
            promptSpeechInput();
            isFullName = true;
            isUsername = false;
            isPassword = false;
            isConfimPassword = false;
            isSecretAnswer = false;
        } else if (usernameString == null) {
            txtToSpeech.speak(usernameSpeech, TextToSpeech.QUEUE_FLUSH, null);
            while (txtToSpeech.isSpeaking()) {
            }
            promptSpeechInput();
            isFullName = false;
            isUsername = true;
            isPassword = false;
            isConfimPassword = false;
            isSecretAnswer = false;
        } else if (passwordString == null) {
            txtToSpeech.speak(passSpeech, TextToSpeech.QUEUE_FLUSH, null);
            while (txtToSpeech.isSpeaking()) {
            }
            promptSpeechInput();
            isFullName = false;
            isUsername = false;
            isPassword = true;
            isConfimPassword = false;
            isSecretAnswer = false;
        } else if (confirmPasswordString == null) {
            txtToSpeech.speak(confirmPassSpeech, TextToSpeech.QUEUE_FLUSH, null);
            while (txtToSpeech.isSpeaking()) {
            }
            promptSpeechInput();
            isFullName = false;
            isUsername = false;
            isPassword = false;
            isConfimPassword = true;
            isSecretAnswer = false;
        } else if (secretAnswerString == null) {
            txtToSpeech.speak(secretQuestionSpeech, TextToSpeech.QUEUE_FLUSH, null);
            while (txtToSpeech.isSpeaking()) {
            }
            txtToSpeech.speak(secretAnswerSpeech, TextToSpeech.QUEUE_FLUSH, null);
            while (txtToSpeech.isSpeaking()) {
            }
            promptSpeechInput();
            isFullName = false;
            isUsername = false;
            isPassword = false;
            isConfimPassword = false;
            isSecretAnswer = true;
        } else {
            if (passwordString.length() < 6) {
                passwordString = null;
                txtToSpeech.speak(passError, TextToSpeech.QUEUE_FLUSH, null);
                while (txtToSpeech.isSpeaking()) {
                }
                promptSpeechInput();
                isFullName = false;
                isUsername = false;
                isPassword = true;
                isConfimPassword = false;
                isSecretAnswer = false;
                needToConfirmPassword = true;
            } else if (needToConfirmPassword) {
                confirmPasswordString = null;
                txtToSpeech.speak(confirmPassError, TextToSpeech.QUEUE_FLUSH, null);
                while (txtToSpeech.isSpeaking()) {
                }
                promptSpeechInput();
                isFullName = false;
                isUsername = false;
                isPassword = false;
                isConfimPassword = true;
                isSecretAnswer = false;
                needToConfirmPassword = false;
            } else
                signUp();
        }
    }

    private void signUp() {
        if (Utilities.isNetworkAvailable(SignUpWithVoiceActivity.this)) {
            if (usernameString.endsWith("@vmail.com")) {
            } else
                usernameString = usernameString + "@vmail.com";

            if (TextUtils.isEmpty(usernameED.getText()) || TextUtils.isEmpty(passwordED.getText())
                    || TextUtils.isEmpty(fullNameED.getText()) || TextUtils.isEmpty(confirmPasswordED.getText())
                    || TextUtils.isEmpty(secretAnswerED.getText()))
                Toast.makeText(SignUpWithVoiceActivity.this, R.string.fields_cannot_be_empty, Toast.LENGTH_SHORT).show();
            else {
                if (passwordED.getText().toString().equals(confirmPasswordED.getText().toString())) {
                    firebaseAuth.createUserWithEmailAndPassword(usernameString, passwordString)
                            .addOnCompleteListener(SignUpWithVoiceActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign up success, update UI with the signed-in user's information
                                        personalDataReference = firebaseDatabase.getReference()
                                                .child(Utilities.getModifiedCurrentEmail()).child("PersonalData");
                                        usersReference = firebaseDatabase.getReference().child("Users");

                                        newUser = new NewUser(fullNameString, usernameString, passwordString
                                                , secretQuestion.getText().toString(), secretAnswerString
                                                , personalDataReference.push().getKey());

                                        insertToDatabase();
                                    } else {
                                        // If sign up fails, display a message to the user.
                                        txtToSpeech.speak(getResources().getString(R.string.authentication_failed), TextToSpeech.QUEUE_FLUSH, null);
                                        while (txtToSpeech.isSpeaking()) {
                                        }
                                        setDataToNull();
                                        handler.postDelayed(runnable, delay);
                                    }
                                }
                            });
                } else {
                    txtToSpeech.speak(getResources().getString(R.string.passwords_donot_match), TextToSpeech.QUEUE_FLUSH, null);
                    while (txtToSpeech.isSpeaking()) {
                    }
                    passwordED.setText("");
                    confirmPasswordED.setText("");
                    passwordString = null;
                    confirmPasswordString = null;
                    isPassword = false;
                    isConfimPassword = false;
                    handler.postDelayed(runnable, delay);
                }
            }
        } else
            txtToSpeech.speak(getResources().getString(R.string.check_internet_connection), TextToSpeech.QUEUE_FLUSH, null);
    }

    private void insertToDatabase() {

        personalDataReference.child(newUser.pushID).setValue(newUser);

        UserEmail userEmail = new UserEmail(newUser.Email, usersReference.push().getKey());
        usersReference.child(userEmail.pushID).setValue(userEmail);

        txtToSpeech.speak(signupSuccess, TextToSpeech.QUEUE_FLUSH, null);

        startActivity(new Intent(SignUpWithVoiceActivity.this, MainActivity.class));
    }

    private void setDataToNull() {
        fullNameED.setText("");
        usernameED.setText("");
        passwordED.setText("");
        confirmPasswordED.setText("");
        secretAnswerED.setText("");
        fullNameString = null;
        usernameString = null;
        passwordString = null;
        confirmPasswordString = null;
        secretAnswerString = null;
        isFullName = false;
        isUsername = false;
        isPassword = false;
        isConfimPassword = false;
        isSecretAnswer = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable); //stop handler when activity not visible
    }

    @Override
    protected void onStop() {
        super.onStop();
        //called to stop handler if user login successfully
        handler.removeCallbacksAndMessages(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //called to stop handler if user login successfully
        handler.removeCallbacksAndMessages(null);

        handler = null;
        runnable = null;
        txtToSpeech = null;
    }

    //speech to text
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    //remove all space between characters and set EditTexts
                    if (isFullName) {
                        fullNameString = result.get(0).replaceAll("\\s", "");
                        Log.i("fullname we get ", fullNameString);
                        fullNameED.setText(fullNameString);
                    } else if (isUsername) {
                        usernameString = result.get(0).replaceAll("\\s", "").toLowerCase();
                        Log.i("username we get ", usernameString);
                        usernameED.setText(usernameString);
                    } else if (isPassword) {
                        passwordString = result.get(0).replaceAll("\\s+", "").toLowerCase();
                        Log.i("password we get ", passwordString);
                        passwordED.setText(passwordString);
                    } else if (isConfimPassword) {
                        confirmPasswordString = result.get(0).replaceAll("\\s+", "").toLowerCase();
                        Log.i("confirmPassword we get ", confirmPasswordString);
                        confirmPasswordED.setText(confirmPasswordString);
                    } else if (isSecretAnswer) {
                        secretAnswerString = result.get(0).replaceAll("\\s+", "").toLowerCase();
                        Log.i("secretAnswer we get ", secretAnswerString);
                        secretAnswerED.setText(secretAnswerString);
                    }
                }
                break;
            }
        }
    }
}