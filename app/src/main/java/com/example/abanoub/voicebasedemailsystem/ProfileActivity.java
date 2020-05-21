package com.example.abanoub.voicebasedemailsystem;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    ArrayList<NewEmail> inboxEmails, sentEmails;
    int counter = 0;
    NewUser user;
    CircleImageView profile_image;
    TextView fullName, received, sent, favorites, email, phoneNumber, birthdate, changePassword;
    FloatingActionButton cameraFab, mic;
    private final int REQ_CODE_SPEECH_INPUT = 100;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference pesonalDataReference, inboxReference, sentReference, favoritesReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // setting navigation up button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        firebaseDatabase = FirebaseDatabase.getInstance();
        pesonalDataReference = firebaseDatabase.getReference().child(Utilities.getModifiedCurrentEmail()).child("PersonalData");
        inboxReference = firebaseDatabase.getReference().child(Utilities.getModifiedCurrentEmail()).child("Inbox");
        sentReference = firebaseDatabase.getReference().child(Utilities.getModifiedCurrentEmail()).child("Sent");

        profile_image = (CircleImageView) findViewById(R.id.profile_image);
        fullName = (TextView) findViewById(R.id.fullName);
        received = (TextView) findViewById(R.id.received);
        sent = (TextView) findViewById(R.id.sent);
        favorites = (TextView) findViewById(R.id.favorites);
        email = (TextView) findViewById(R.id.email);
        phoneNumber = (TextView) findViewById(R.id.phoneNumber);
        birthdate = (TextView) findViewById(R.id.birthdate);
        changePassword = (TextView) findViewById(R.id.changePassword);
        mic = (FloatingActionButton) findViewById(R.id.fab);
        cameraFab = (FloatingActionButton) findViewById(R.id.cameraFab);

        profile_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        cameraFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create and show the dialog and pass the user as an argument with static method.
                DialogFragment dialog = ChangeProfilePicDialogFragment.newInstance(user);
                dialog.show(getSupportFragmentManager(), "tag");
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create and show the dialog and pass the user as an argument with static method.
                DialogFragment dialog = ChangePasswordDialogFragment.newInstance(user);
                dialog.show(getSupportFragmentManager(), "tag");
            }
        });

        pesonalDataReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = Utilities.getPersonalData(dataSnapshot);
                setPersonalData(user);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        inboxReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                inboxEmails = Utilities.getAllEmails(dataSnapshot);
                received.setText(inboxEmails.size() + "");
                setFavoritesNumber();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        sentReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                sentEmails = Utilities.getAllEmails(dataSnapshot);
                sent.setText(sentEmails.size() + "");
                setFavoritesNumber();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Showing google speech input dialog
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                        getString(R.string.speech_prompt));
                try {
                    startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
                } catch (ActivityNotFoundException a) {
                    Toast.makeText(ProfileActivity.this, getString(R.string.speech_not_supported),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void setFavoritesNumber() {
        if (inboxEmails != null && sentEmails != null) {
            for (int i = 0; i < inboxEmails.size(); i++) {
                if (inboxEmails.get(i).isFavorite.equals("yes"))
                    counter++;
            }
            for (int i = 0; i < sentEmails.size(); i++) {
                if (sentEmails.get(i).isFavorite.equals("yes"))
                    counter++;
            }
            favorites.setText(counter + "");
        }
    }

    private void setPersonalData(NewUser user) {

        if (user.profilePicture != null)
            Glide.with(getApplicationContext())
                    .load(user.profilePicture)
                    .into(profile_image);

        fullName.setText(user.FullName);
        email.setText(user.Email);
        phoneNumber.setText(user.MobileNumber);
        birthdate.setText(user.BirthDate);
    }

    /**
     * Receiving speech input
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == ProfileActivity.this.RESULT_OK && null != data) {

                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    if (result.get(0).equals("compose email") || result.get(0).equals("compose an email")
                            || result.get(0).equals("compose new email") || result.get(0).equals("compose mail")
                            || result.get(0).equals("compose new mail") || result.get(0).equals("write email")
                            || result.get(0).equals("write an email") || result.get(0).equals("write new email")
                            || result.get(0).equals("write mail") || result.get(0).equals("write new mail")
                            || result.get(0).contains("compose") || result.get(0).contains("write")
                            || result.get(0).contains("new mail"))
                        startActivity(new Intent(ProfileActivity.this, ComposeEmailActivity.class));

                    else if (result.get(0).equals("sign out") || result.get(0).equals("log out")
                            || result.get(0).contains("sign out")) {
                        FirebaseAuth.getInstance().signOut();
                        startActivity(new Intent(ProfileActivity.this, SignInActivity.class));

                    } else if (result.get(0).equals("sent") || result.get(0).equals("open sent")
                            || result.get(0).equals("open sent emails") || result.get(0).equals("open sent page")
                            || result.get(0).equals("open sent mails") || result.get(0).equals("show me sent emails")
                            || result.get(0).equals("show me sent mails") || result.get(0).contains("sent")) {
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        intent.putExtra("fragment", "sent");
                        startActivity(intent);
                    } else if (result.get(0).equals("inbox") || result.get(0).equals("open inbox")
                            || result.get(0).equals("open my inbox") || result.get(0).equals("open received emails")
                            || result.get(0).equals("open inbox page") || result.get(0).equals("open received mails")
                            || result.get(0).contains("show me inbox") || result.get(0).contains("show me my inbox")
                            || result.get(0).equals("show me received emails") || result.get(0).equals("show me received mails")
                            || result.get(0).contains("inbox") || result.get(0).contains("received")) {
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        intent.putExtra("fragment", "inbox");
                        startActivity(intent);
                    } else if (result.get(0).equals("favorites") || result.get(0).equals("open favorites")
                            || result.get(0).equals("open my favorites") || result.get(0).equals("open favorite emails")
                            || result.get(0).equals("open favorites page") || result.get(0).equals("open favorite mails")
                            || result.get(0).equals("show me favorite emails") || result.get(0).equals("show me favorite mails")
                            || result.get(0).contains("favorite") || result.get(0).contains("favorites")
                            || result.get(0).contains("favourite") || result.get(0).contains("favourites")) {
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        intent.putExtra("fragment", "favorites");
                        startActivity(intent);
                    } else if (result.get(0).equals("trash") || result.get(0).equals("open trash")
                            || result.get(0).equals("open my trash") || result.get(0).equals("open trash emails")
                            || result.get(0).equals("open trash page") || result.get(0).equals("open trash mails")
                            || result.get(0).equals("show me trash emails") || result.get(0).equals("show me trash mails")
                            || result.get(0).equals("open my deleted emails") || result.get(0).contains("deleted")
                            || result.get(0).contains("trash") || result.get(0).contains("trashed")) {
                        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                        intent.putExtra("fragment", "trash");
                        startActivity(intent);
                    } else if (result.get(0).equals("exit") || result.get(0).equals("exit application")
                            || result.get(0).equals("exit from application") || result.get(0).equals("back")
                            || result.get(0).equals("go back"))
                        onBackPressed();

                    else if (result.get(0).equals("profile") || result.get(0).equals("open profile")
                            || result.get(0).equals("open my profile") || result.get(0).equals("show me my profile")
                            || result.get(0).equals("show profile") || result.get(0).contains("profile"))
                        Toast.makeText(ProfileActivity.this, "We already here", Toast.LENGTH_SHORT).show();

                    else
                        Toast.makeText(ProfileActivity.this, "Not recognized", Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Utilities.getCurrentUser() == null)
            startActivity(new Intent(this, SplashActivity.class));
    }
}