package com.example.abanoub.voicebasedemailsystem;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetailedActivity extends AppCompatActivity {

    TextView title, sender, receiver, date, body;
    ImageView star_btn;
    CircleImageView profile_image;
    LinearLayout replay, forward;
    FloatingActionButton fab;
    TextToSpeech textToSpeech;
    NewEmail clicked_email;
    String child, child2; //child2 made to tell us if user was on Trash or not
    boolean found = false;

    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        clicked_email = getIntent().getParcelableExtra("email");
        child = getIntent().getStringExtra("child");
        child2 = getIntent().getStringExtra("child2");

        sender = (TextView) findViewById(R.id.sender);
        receiver = (TextView) findViewById(R.id.receiver);
        star_btn = (ImageView) findViewById(R.id.star);

        if (child2 != null) {
            if (child2.equals("Trash"))
                star_btn.setVisibility(View.GONE);
        }

        firebaseDatabase = FirebaseDatabase.getInstance();
        DatabaseReference usersReference = firebaseDatabase.getReference().child("Users");
        usersReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                ArrayList<UserEmail> usersEmail_list = Utilities.getAllUsersEmails(dataSnapshot);
                setData(usersEmail_list);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        if (child2 == null)
            databaseReference = firebaseDatabase.getReference().child(Utilities.getModifiedCurrentEmail())
                    .child(child);
        else
            databaseReference = firebaseDatabase.getReference().child(Utilities.getModifiedCurrentEmail())
                    .child("Trash");

        if (clicked_email.isFavorite.equals("yes")) {
            found = true;
            star_btn.setImageResource(R.drawable.ic_star_24dp);
        }

        title = (TextView) findViewById(R.id.title);
        date = (TextView) findViewById(R.id.date);
        body = (TextView) findViewById(R.id.body);
        profile_image = (CircleImageView) findViewById(R.id.profile_image);
        replay = (LinearLayout) findViewById(R.id.replayLinear);
        forward = (LinearLayout) findViewById(R.id.forwardLinear);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        title.setText(clicked_email.title);
        date.setText(clicked_email.date);
        body.setText(clicked_email.body);

        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(Locale.UK);
                }
            }
        });

        star_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (found) {
                    clicked_email.isFavorite = "no";
                    databaseReference.child(clicked_email.pushID).setValue(clicked_email);
                    star_btn.setImageResource(R.drawable.ic_star_border_24dp);
                    Toast.makeText(DetailedActivity.this, "Deleted from favorites", Toast.LENGTH_SHORT).show();
                    found = false;

                } else {
                    clicked_email.isFavorite = "yes";
                    databaseReference.child(clicked_email.pushID).setValue(clicked_email);
                    star_btn.setImageResource(R.drawable.ic_star_24dp);
                    Toast.makeText(DetailedActivity.this, "Marked as favorite", Toast.LENGTH_SHORT).show();
                    found = true;
                }
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String toSpeak = null;
                if (child.equals("Inbox")) {
                    toSpeak = "Email from " + clicked_email.sender + ".\nTo me.\nAt " + clicked_email.date + ".\nIt says that:\n\n"
                            + body.getText().toString();
                } else if (child.equals("Sent"))
                    toSpeak = "Email from me.\nTo " + clicked_email.receiver + ".\nAt " + clicked_email.date + ".\nIt says that:\n\n"
                            + body.getText().toString();

                Toast.makeText(getApplicationContext(), toSpeak, Toast.LENGTH_LONG).show();
                textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null);
            }
        });

        replay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailedActivity.this, ComposeEmailActivity.class);
                intent.putExtra("email", clicked_email);
                intent.putExtra("replay", "replay");
                startActivity(intent);
            }
        });

        forward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(DetailedActivity.this, ComposeEmailActivity.class);
                intent.putExtra("email", clicked_email);
                startActivity(intent);
            }
        });
    }

    private void setData(ArrayList<UserEmail> usersEmail_list) {
        if (child.equals("Inbox")) {
            sender.setText(clicked_email.sender);
            receiver.setText("to me");

            for (int i = 0; i < usersEmail_list.size(); i++) {
                if (clicked_email.sender.equals(usersEmail_list.get(i).email) && usersEmail_list.get(i).profilePicture != null)
                    Glide.with(getApplicationContext())
                            .load(usersEmail_list.get(i).profilePicture)
                            .into(profile_image);
            }
        } else if (child.equals("Sent")) {
            sender.setText("Me");
            receiver.setText("to " + clicked_email.receiver);

            for (int i = 0; i < usersEmail_list.size(); i++) {
                if (clicked_email.receiver.equals(usersEmail_list.get(i).email) && usersEmail_list.get(i).profilePicture != null)
                    Glide.with(getApplicationContext())
                            .load(usersEmail_list.get(i).profilePicture)
                            .into(profile_image);
            }
        }
    }

    @Override
    public void onPause() {
        if (textToSpeech != null)
            textToSpeech.stop();

        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (child2 != null) {
            if (child2.equals("Trash")) {
                if (clicked_email.sender.equals(Utilities.getCurrentEmail()))
                    inflater.inflate(R.menu.trash_detailed_menu2, menu);
                else
                    inflater.inflate(R.menu.trash_detailed_menu1, menu);
            }
        } else
            inflater.inflate(R.menu.detailed_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.delete_menu:

                //delete email
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage("Are you sure that you want to delete this email ?");
                alertDialogBuilder.setPositiveButton("Delete",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                databaseReference.child(clicked_email.pushID).setValue(null);

                                if (child2 == null) {
                                    databaseReference = firebaseDatabase.getReference().child(Utilities.getModifiedCurrentEmail())
                                            .child("Trash");
                                    databaseReference.child(clicked_email.pushID).setValue(clicked_email);
                                }
                                Toast.makeText(DetailedActivity.this, "Deleted successfully", Toast.LENGTH_SHORT).show();
                                onBackPressed();
                            }
                        });
                alertDialogBuilder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                alertDialogBuilder.create().show();

                return true;

            case R.id.restore_menu:

                //restore email
                AlertDialog.Builder alertDialogBuilder2 = new AlertDialog.Builder(this);
                alertDialogBuilder2.setMessage("Are you sure that you want to restore this email ?");
                alertDialogBuilder2.setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                databaseReference.child(clicked_email.pushID).setValue(null);

                                if (clicked_email.sender.equals(Utilities.getCurrentEmail())) {

                                    databaseReference = firebaseDatabase.getReference().child(Utilities.getModifiedCurrentEmail())
                                            .child("Sent");
                                    databaseReference.child(clicked_email.pushID).setValue(clicked_email);
                                    Toast.makeText(DetailedActivity.this, "Restored to sent successfully", Toast.LENGTH_LONG).show();
                                } else {

                                    databaseReference = firebaseDatabase.getReference().child(Utilities.getModifiedCurrentEmail())
                                            .child("Inbox");
                                    databaseReference.child(clicked_email.pushID).setValue(clicked_email);
                                    Toast.makeText(DetailedActivity.this, "Restored to inbox successfully", Toast.LENGTH_LONG).show();
                                }
                                onBackPressed();
                            }
                        });
                alertDialogBuilder2.setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        });
                alertDialogBuilder2.create().show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
