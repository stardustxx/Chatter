package com.sdust.chatter;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.melnykov.fab.FloatingActionButton;
import com.parse.Parse;
import com.parse.ParseUser;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements itemViewAdapter.ClickListener, Observer {

    private RecyclerView recyclerView;
//    private RecyclerView.LayoutManager layoutManager;
    private LinearLayoutManager layoutManager;
    private itemViewAdapter itemViewAdapter;
    private Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;

    private static final String MY_FLURRY_APIKEY = "QNN2GXSTBN3G7M4CY7S8";

    private int pastVisibleItems, visibleItemCount, totalItemCount;

    private Post post;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Fresco.initialize(this);                            // Initialize Facebook Fresco
        setContentView(R.layout.activity_main);

        //Parse.enableLocalDatastore(this);
        //ParseCrashReporting.enable(this);
        Parse.initialize(this, "9ogtClP8xsagmHwDmKDLdGg5ePhMO6CLRxfLBVBr", "qaEYIw54lqUrB7xdvp2OJNkB5znLlKFI75mxv5tt");

        // configure Flurry
        FlurryAgent.setLogEnabled(false);

        // init Flurry
        FlurryAgent.init(this, MY_FLURRY_APIKEY);

        // Set up my own app bar
        // Also include these lines on any activity that you want the app bar to appear
        toolbar = (Toolbar) findViewById(R.id.appBar);
        setSupportActionBar(toolbar);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refreshlayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                post.updateLocation();
                post.grabPost(false);
//                swipeRefreshLayout.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Refreshing", Toast.LENGTH_SHORT).show();
            }
        });

        // Set up recyclerview
        recyclerView = (RecyclerView) findViewById(R.id.feedList);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);

        post = new Post(this, recyclerView, itemViewAdapter);                               // Set up Post class
        post.addObserver(this);

//        itemViewAdapter.setClickListener(MainActivity.this);
//        post.grabPost(false);
        post.updateLocation();                                                        // Update location and update feeds
        post.grabPost(false);
        swipeRefreshLayout.setRefreshing(true);

        // Set up FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(recyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, newActivity.class);
                startActivityForResult(intent, 0);
            }
        });

        // Set up recyclerview scroll listener
        recyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                visibleItemCount = layoutManager.getChildCount();
                totalItemCount = layoutManager.getItemCount();
                pastVisibleItems = layoutManager.findFirstVisibleItemPosition();

//                itemViewAdapter.setClickListener(MainActivity.this);

                if (pastVisibleItems + visibleItemCount >= totalItemCount && !post.isNeedToLoad()){
                    Toast.makeText(MainActivity.this, "Loading", Toast.LENGTH_SHORT).show();
                    post.setNeedToLoad(true);
                    post.grabPost(true);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        post.grabPost(false);
        Toast.makeText(MainActivity.this, "Refreshing", Toast.LENGTH_SHORT).show();
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStop() {
        super.onStop();
        post.locationManager.removeUpdates(post.locationListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
//        grabPost();
//        Toast.makeText(MainActivity.this, "Refreshing", Toast.LENGTH_SHORT).show();
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        if (id == R.id.profile) {
            startActivity(new Intent(MainActivity.this, profileActivity.class));
        }
        else if(id == R.id.refresh){
            post.updateLocation();
            post.grabPost(false);
            Toast.makeText(MainActivity.this, "Refreshing", Toast.LENGTH_SHORT).show();
        }
//        else if (id == R.id.currentUser){
//            if (ParseUser.getCurrentUser().getUsername() != null) {
//                Toast.makeText(MainActivity.this, ParseUser.getCurrentUser().getUsername(), Toast.LENGTH_SHORT).show();
//            }
//            else {
//                Toast.makeText(MainActivity.this, "null", Toast.LENGTH_SHORT).show();
//            }
//        }
//        else if (id == R.id.location){
//            updateLocation(false, false);
//        }
//        else if (id == R.id.around){
//            int aroundMeNumber;
//            aroundMeNumber = post.getAroundMeNumber();
//            Toast.makeText(MainActivity.this, "Number of People around: " + Integer.toString(aroundMeNumber), Toast.LENGTH_SHORT).show();
//        }
        else if (id == R.id.logOff){
            ParseUser.logOut();
//            post.locationManager.removeUpdates(post.locationListener);
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    // Using objectId to find the corresponding post at that position and update number of likes
    public void itemClicked(View view, String objectID) {
//        Toast.makeText(this, Integer.toString(position), Toast.LENGTH_SHORT).show();
//        Intent intent = new Intent(this, feedDetail.class);
//        startActivity(intent);
    }

    @Override
    public void update(Observable observable, Object data) {
        post.itemViewAdapter.setClickListener(MainActivity.this);
    }
}
