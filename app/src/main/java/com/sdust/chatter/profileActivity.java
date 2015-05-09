package com.sdust.chatter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseUser;

import java.util.Observable;
import java.util.Observer;


public class profileActivity extends AppCompatActivity implements Observer {

    private itemViewProfileAdapter itemViewProfileAdapter;
    private int pastVisibleItems, visibleItemCount, totalItemCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        Parse.initialize(this, "9ogtClP8xsagmHwDmKDLdGg5ePhMO6CLRxfLBVBr", "qaEYIw54lqUrB7xdvp2OJNkB5znLlKFI75mxv5tt");

        Toolbar toolbar;
        RecyclerView recyclerView;
        final LinearLayoutManager layoutManager;

        toolbar = (Toolbar) findViewById(R.id.appBar);
        toolbar.setTitle("Profile");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recyclerView = (RecyclerView) findViewById(R.id.profilePostList);
        layoutManager = new LinearLayoutManager(profileActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        final Post post = new Post(this, recyclerView, itemViewProfileAdapter, ParseUser.getCurrentUser());
//        post.addObserver(this);

        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = layoutManager.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                pastVisibleItems = layoutManager.findFirstVisibleItemPosition();
                Log.d("visibleItemCount", Integer.toString(visibleItemCount));
                Log.d("totalItemCount", Integer.toString(totalItemCount));
                Log.d("pastVisibleItem", Integer.toString(pastVisibleItems));

//                itemViewAdapter.setClickListener(MainActivity.this);

                if (pastVisibleItems + visibleItemCount >= totalItemCount && !post.isNeedToLoad()){
                    Log.d("layout manager status", "last row reached");
                    Toast.makeText(profileActivity.this, "Loading", Toast.LENGTH_SHORT).show();
                    post.setNeedToLoad(true);
                    post.searchPost(true);
                }
                Log.d("scrolling", "scrolling");
            }
        });

        post.searchPost(false);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);
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

    @Override
    public void update(Observable observable, Object data) {
        Log.d("Notified", "sup");
    }
}
