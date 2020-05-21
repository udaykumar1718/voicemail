package com.example.abanoub.voicebasedemailsystem;

/**
 * Created by Abanoub on 2018-02-12.
 */

public class UserEmail {
    public String email;
    public String profilePicture;

    public String pushID;


    public UserEmail() {
    }

    public UserEmail(String email, String pushID) {
        this.email = email;
        this.pushID = pushID;
    }

    public UserEmail(String email, String profilePicture, String pushID) {
        this.email = email;
        this.profilePicture = profilePicture;
        this.pushID = pushID;
    }
}
