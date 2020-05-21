package com.example.abanoub.voicebasedemailsystem;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Handler;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.Locale;

public class VoiceQuestionActivity extends Activity {

    //Handler work every x time
    Handler handler = new Handler();
    Runnable runnable;
    int delay = 400; //1 second=1000 milisecond

    //Text to speech API
    TextToSpeech txtToSpeech;

    //speech to text API
    Intent speechInputIntent;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    Button yes, no;
    boolean stopVoice = false, gotAnswer = false;
    private final int REQ_CODE_ENABLE_VOICE = 5; //used to enable voice when user move and get back to this activity

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.activity_voice_question);

        yes = (Button) findViewById(R.id.yes);
        no = (Button) findViewById(R.id.no);

        txtToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    txtToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(VoiceQuestionActivity.this, SignInWithVoiceActivity.class)
                        , REQ_CODE_ENABLE_VOICE);
                stopVoice();
                gotAnswer=true;
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(VoiceQuestionActivity.this, SignInActivity.class)
                        , REQ_CODE_ENABLE_VOICE);
                stopVoice();
                gotAnswer=true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!stopVoice && !gotAnswer) {
            /* New Handler to start the SignInActivity
             * and close this Splash-Screen after some seconds.*/
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                    if (txtToSpeech != null)
                        txtToSpeech.speak("Would you like to continue with voice?", TextToSpeech.QUEUE_FLUSH, null);

                    //make app wait for 2 seconds then resume executing
                    try {
                        Thread.sleep(2620);
                    } catch (InterruptedException ex) {
                        android.util.Log.d("exception", ex.toString());
                    }
                    promptSpeechInput();

                }
            }, delay);
        }
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
        speechInputIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechInputIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechInputIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        speechInputIntent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(speechInputIntent, REQ_CODE_SPEECH_INPUT);
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

                    if (result.get(0).contains("yes")) {
                        startActivityForResult(new Intent(VoiceQuestionActivity.this, SignInWithVoiceActivity.class)
                                , REQ_CODE_ENABLE_VOICE);
                        gotAnswer=true;
                    } else if (result.get(0).contains("no")) {
                        /* Create an Intent that will start the SignInActivity. */
                        startActivityForResult(new Intent(VoiceQuestionActivity.this, SignInActivity.class)
                                , REQ_CODE_ENABLE_VOICE);
                        gotAnswer=true;
                    }
                }
                break;
            }

            case REQ_CODE_ENABLE_VOICE: {
                // execute when 2nd activity press back button
                // when we opened 2nd activity we stopped voice so we need to enable it again
                stopVoice = false;
                gotAnswer=false;
            }
        }
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true); //exit app
        stopVoice();
    }

    private void stopVoice() {
        stopVoice = true;
        if (txtToSpeech.isSpeaking())
            txtToSpeech.stop();
        if (speechInputIntent != null)
            speechInputIntent = null;
    }
}