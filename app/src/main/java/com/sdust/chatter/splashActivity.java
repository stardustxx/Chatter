package com.sdust.chatter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import com.parse.Parse;
import com.parse.ParseUser;


public class splashActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        Parse.initialize(this, "9ogtClP8xsagmHwDmKDLdGg5ePhMO6CLRxfLBVBr", "qaEYIw54lqUrB7xdvp2OJNkB5znLlKFI75mxv5tt");
        // Check if there is user logged in already
        if (ParseUser.getCurrentUser() == null) {
            startActivity(new Intent(splashActivity.this, LoginActivity.class));
        }
        else {
            startActivity(new Intent(splashActivity.this, MainActivity.class));
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_splash, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
