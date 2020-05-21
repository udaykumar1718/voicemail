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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.Locale;

public class SignInWithVoiceActivity extends Activity {

    static String askToSignUpSpeech = "You are on sign in page.\nwould you like to sign up?";
    Button signin_btn;
    TextView GotoSignUp;
    FirebaseAuth firebaseAuth;
    AutoCompleteTextView username, password;
    String usernameString, passwordString, askToSignUpString;

    //Handler work every x time
    Handler handler = new Handler();
    int delay = 2000; //1 second=1000 milisecond
    Runnable runnable;
    Boolean isUsername = false, isPassword = false, isAskToSignUp = false;
    static String usernameSpeech = "Please enter your username";
    static String passSpeech = "Please enter your password";
    static String passError = "Password must be at least six character, Please enter your password again";
    static String loginFailedError = "Username or password is incorrect, please enter again";
    static String loginSuccess = "Login successfully and you are in the inbox page";

    //Text to speech API
    TextToSpeech txtToSpeech;
    //speech to text API
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        firebaseAuth = FirebaseAuth.getInstance();

        username = findViewById(R.id.usernameEdit);
        password = findViewById(R.id.passwordEdit);
        signin_btn = findViewById(R.id.signin_btn);
        GotoSignUp = findViewById(R.id.signup_link);

        signin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("response", "Login");
                txtToSpeech.speak(passSpeech, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        GotoSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInWithVoiceActivity.this, SignUpWithVoiceActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //it reuses an existing activity by bringing it to the front of the stack
                startActivity(intent);
            }
        });

        txtToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    txtToSpeech.setLanguage(Locale.UK);
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        //start handler as activity become visible
        handler.postDelayed(new Runnable() {
            public void run() {
                //do something
                getDataUsingVoice();
                runnable = this;
            }
        }, delay);
    }

    private void getDataUsingVoice() {
        if (askToSignUpString == null) {
            txtToSpeech.speak(askToSignUpSpeech, TextToSpeech.QUEUE_FLUSH, null);
            //make app wait for 2 seconds then resume executing
            try {
                Thread.sleep(2300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            promptSpeechInput();
            isAskToSignUp = true;
            isUsername = false;
            isPassword = false;
        } else if (usernameString == null) {
            txtToSpeech.speak(usernameSpeech, TextToSpeech.QUEUE_FLUSH, null);
            while (txtToSpeech.isSpeaking()) {
            }
            promptSpeechInput();
            isAskToSignUp = false;
            isUsername = true;
            isPassword = false;
        } else if (passwordString == null) {
            txtToSpeech.speak(passSpeech, TextToSpeech.QUEUE_FLUSH, null);
            while (txtToSpeech.isSpeaking()) {
            }
            promptSpeechInput();
            isAskToSignUp = false;
            isUsername = false;
            isPassword = true;
        } else {
            if (passwordString.length() < 6) {
                passwordString = null;
                txtToSpeech.speak(passError, TextToSpeech.QUEUE_FLUSH, null);
                while (txtToSpeech.isSpeaking()) {
                }
                promptSpeechInput();
                isPassword = true;
                isUsername = false;
            } else
                login();
        }
    }

    private void login() {
        if (Utilities.isNetworkAvailable(SignInWithVoiceActivity.this)) {
            if (usernameString.endsWith("@vmail.com")) {
            } else
                usernameString = usernameString + "@vmail.com";

            if (TextUtils.isEmpty(username.getText()) || TextUtils.isEmpty(password.getText()))
                Toast.makeText(SignInWithVoiceActivity.this, R.string.fields_cannot_be_empty, Toast.LENGTH_SHORT).show();
            else {
                firebaseAuth.signInWithEmailAndPassword(usernameString, passwordString)
                        .addOnCompleteListener(SignInWithVoiceActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d("response", "Succcccccccccccccccccccccessssssss");
                                    txtToSpeech.speak(loginSuccess, TextToSpeech.QUEUE_FLUSH, null);
                                    Intent intent = new Intent(SignInWithVoiceActivity.this, MainActivity.class);
                                    startActivity(intent);
                                } else {
                                    txtToSpeech.speak(loginFailedError, TextToSpeech.QUEUE_FLUSH, null);
                                    while (txtToSpeech.isSpeaking()) {
                                    }
                                    setDataToNull();
                                    handler.postDelayed(runnable, delay);
                                    Log.d("response", "Faileeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeed");
                                }
                            }
                        });
            }
        } else {
            txtToSpeech.speak(getResources().getString(R.string.check_internet_connection), TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void setDataToNull() {
        username.setText("");
        password.setText("");
        passwordString = null;
        usernameString = null;
        isUsername = false;
        isPassword = false;
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

                    //remove all space between characters & set EditTexts
                    if (isAskToSignUp) {
                        askToSignUpString = result.get(0).replaceAll("\\s", "");
                        if (askToSignUpString.equals("yes"))
                            startActivity(new Intent(SignInWithVoiceActivity.this, SignUpWithVoiceActivity.class));
                        else if (askToSignUpString.equals("no")) {
                        } else
                            askToSignUpString = null;
                    } else if (isUsername) {
                        usernameString = result.get(0).replaceAll("\\s", "").toLowerCase();
                        Log.i("username we get ", usernameString);
                        username.setText(usernameString);
                    } else if (isPassword) {
                        passwordString = result.get(0).replaceAll("\\s+", "").toLowerCase();
                        Log.i("password we get ", passwordString);
                        password.setText(passwordString);
                    }
                }
                break;
            }
        }
    }
}