package com.example.abanoub.voicebasedemailsystem;

import java.io.Serializable;

/**
 * Created by Abanoub on 2017-12-04.
 */

public class NewUser implements Serializable {
    public String FullName;
    public String Email;
    public String password;
    public String BirthDate;
    public String Gender;
    public String MobileNumber;
    public String SecretQuestion;
    public String SecretAnswer;
    public String pushID;
    public String profilePicture;

    public NewUser() {}

    public NewUser(String FullName, String Email, String password){
        this.FullName = FullName;
        this.Email = Email;
        this.password = password;
    }

    public NewUser(String FullName, String Email, String password, String SecretQuestion, String SecretAnswer, String pushID) {
        this.FullName = FullName;
        this.Email = Email;
        this.password = password;
        this.SecretQuestion = SecretQuestion;
        this.SecretAnswer = SecretAnswer;
        this.pushID = pushID;
    }

    public NewUser(String FullName, String Email, String password, String BirthDate, String Gender, String MobileNumber, String SecretQuestion, String SecretAnswer, String pushID) {
        this.FullName = FullName;
        this.Email = Email;
        this.password = password;
        this.BirthDate = BirthDate;
        this.Gender = Gender;
        this.MobileNumber = MobileNumber;
        this.SecretQuestion = SecretQuestion;
        this.SecretAnswer = SecretAnswer;
        this.pushID = pushID;
    }
}
