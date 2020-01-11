package com.example.finalplanitapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.finalplanitapp.dao.UserDAO;
import com.example.finalplanitapp.dao.Config;

public class MainActivity extends AppCompatActivity {

    EditText email, password;
    Button signInButton;
    DatabaseHelper db;

    public static String emailString, passwordString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView guestButton = (TextView) findViewById(R.id.continueAsGuest);
        Button registerButton = (Button)findViewById(R.id.registerButton);

        TextView t2 = (TextView) findViewById(R.id.continueAsGuest);
        t2.setMovementMethod(LinkMovementMethod.getInstance());

        guestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openCalendar();
            }
        });

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openRegistration();
            }
        });


        email = findViewById(R.id.email3);
        password = findViewById(R.id.password3);
        signInButton = findViewById (R.id.loginButton);
        db = new DatabaseHelper(this);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                emailString = email.getText().toString();
                passwordString = password.getText().toString();

                if (emailString.equals("") || passwordString.equals("")) {
                    Toast.makeText(getApplicationContext(), "Enter your email and password", Toast.LENGTH_SHORT).show();
                } else {
                    boolean newMailCheck = db.verifyNewMail(emailString);
                    if (newMailCheck) {
                        Toast.makeText(getApplicationContext(), "Please register", Toast.LENGTH_SHORT).show();
                        openRegistration();
                    } else {
                        boolean match = db.matching(emailString, passwordString);
                        if (match) {
                            Toast.makeText(getApplicationContext(), "Successful login!", Toast.LENGTH_SHORT).show();
                            openCalendar();
                        } else {
                            Toast.makeText(getApplicationContext(), "Incorrect password", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        });
    }

    public void openCalendar() {
        Intent intent = new Intent(this, DateActivity.class);
        startActivity(intent);
    }

    public void openRegistration() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        intent.putExtra("email", email.getText().toString());
        //Intent intent = new Intent(this, JourneyListActivity.class);
        startActivity(intent);
    }
}
