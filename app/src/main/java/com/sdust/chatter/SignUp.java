package com.sdust.chatter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;
import com.parse.SignUpCallback;


public class SignUp extends AppCompatActivity {
    EditText emailTxt, usernameTxt, passwordTxt, passwordConfirmTxt;
    Button registerBtn;
    ParseUser User = new ParseUser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        Toolbar toolbar = (Toolbar) findViewById(R.id.appBar);
        toolbar.setTitle("Sign Up");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        emailTxt = (EditText) findViewById(R.id.email);
        usernameTxt = (EditText) findViewById(R.id.username);
        passwordTxt = (EditText) findViewById(R.id.password);
        passwordConfirmTxt = (EditText) findViewById(R.id.passwordConfirm);
        registerBtn = (Button) findViewById(R.id.registerBtn);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailTxt.getText().toString();
                String username = usernameTxt.getText().toString();
                String password = passwordTxt.getText().toString();
                String passwordConfirm = passwordConfirmTxt.getText().toString();

                Log.d("email", email);
                Log.d("username", username);
                Log.d("password", password);
                Log.d("password confirm", passwordConfirm);

                if (username.equals("")){
                    Toast.makeText(SignUp.this, "Please fill out your username", Toast.LENGTH_SHORT).show();
                }
                else if (email.equals("")){
                    Toast.makeText(SignUp.this, "Please fill out your email", Toast.LENGTH_SHORT).show();
                }
                else if (password.equals("") || password.length() < 6){
                    Toast.makeText(SignUp.this, "Please check your password to be at least 6 letters", Toast.LENGTH_SHORT).show();
                }
                else if (!passwordConfirm.equals(password)){
                    Toast.makeText(SignUp.this, "Please confirm your password", Toast.LENGTH_SHORT).show();
                }
                else {
                    registration(username, email, password);
                }
            }
        });
    }

    private void registration(final String username, String email, final String password){
        ParseGeoPoint currentLocation = new ParseGeoPoint(49.2827, -123.1207);
        User.setUsername(username);
        User.setEmail(email);
        User.setPassword(password);
        User.put("Location", currentLocation);
        User.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    Toast.makeText(SignUp.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                    signInUser(username, password);
                    SignUp.this.finish();
                } else {
                    Toast.makeText(SignUp.this, "Something is wrong", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void signInUser(String username, String password){
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e == null) {
                    Toast.makeText(SignUp.this, "Successfully Signed In", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(SignUp.this, MainActivity.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(SignUp.this, "Please double check your username and password", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
}
