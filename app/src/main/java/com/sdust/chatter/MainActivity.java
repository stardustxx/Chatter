package com.sdust.chatter;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RefreshCallback;

import java.util.List;
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
    private boolean loading = true;
//    private Date lastPositionDate;
//    private Boolean needToLoad = false;

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
                updateLocation(true, true);
                swipeRefreshLayout.setRefreshing(false);
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
        post.grabPost(false);
        updateLocation(true, false);

        // Set up FAB
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.attachToRecyclerView(recyclerView);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, newActivity.class);
                //startActivity(intent);
//                startActivityForResult(intent, 0, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());
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
                Log.d("visibleItemCount", Integer.toString(visibleItemCount));
                Log.d("totalItemCount", Integer.toString(totalItemCount));
                Log.d("pastVisibleItem", Integer.toString(pastVisibleItems));

//                itemViewAdapter.setClickListener(MainActivity.this);

                if (pastVisibleItems + visibleItemCount >= totalItemCount && !post.isNeedToLoad()){
                    Log.d("layout manager status", "last row reached");
                    Toast.makeText(MainActivity.this, "Loading", Toast.LENGTH_SHORT).show();
                    post.setNeedToLoad(true);
                    post.grabPost(true);
                }
                Log.d("scrolling", "scrolling");
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        post.grabPost(false);
        Toast.makeText(MainActivity.this, "Refreshing", Toast.LENGTH_SHORT).show();
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void updateLocation(final boolean grabPostToo, final boolean updateAnyway){
        boolean gpsEnabled = false;
        boolean networkEnabled = false;
        final float[] distanceResult = new float[1];

        // Acquire a reference to the system Location Manager
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                final double latitude = location.getLatitude();           // x
                final double longitude = location.getLongitude();         // y
                Log.d("position", Double.toString(latitude) + " " + Double.toString(longitude));

                final ParseGeoPoint currentLocation = new ParseGeoPoint(latitude, longitude);
                ParseUser user = ParseUser.getCurrentUser();
                ParseQuery<ParseUser> userQuery = ParseUser.getQuery();
                userQuery.whereEqualTo("objectId", user.getObjectId());
                userQuery.findInBackground(new FindCallback<ParseUser>() {
                    @Override
                    public void done(List<ParseUser> parseUsers, ParseException e) {
                        if (e == null) {
                            ParseUser userFound = parseUsers.get(0);
                            // Checking the distance between current position and server user position
                            ParseGeoPoint serverUserLocation = (ParseGeoPoint) userFound.get("Location");
                            Log.d("parse lat", Double.toString(serverUserLocation.getLatitude()));
                            Log.d("parse long", Double.toString(serverUserLocation.getLongitude()));
                            Location.distanceBetween(serverUserLocation.getLatitude(), serverUserLocation.getLongitude(), latitude, longitude, distanceResult);
                            Log.d("distance result", Float.toString(distanceResult[0]));
                            userFound.put("Location", currentLocation);
                            userFound.saveInBackground();
                            Toast.makeText(MainActivity.this, currentLocation.toString(), Toast.LENGTH_SHORT).show();
                            ParseUser.getCurrentUser().refreshInBackground(new RefreshCallback() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if (e != null) {
                                        Log.d("um", "um");
                                    }
                                }
                            });
//                            if (grabPostToo) {
//                                grabPost();
//                            }
                            if (updateAnyway){
                                post.grabPost(false);
                            }
                            else if (distanceResult[0] > 10000 && grabPostToo) {
                                post.grabPost(false);
                            }
                        } else {
                            Log.d("error", e.toString());
                        }
                    }
                });
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

//        try {
//            gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
//        }
//        catch (Exception e){
//            Log.d("GPS Enabled", "False");
//        }
//
//        try {
//            networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
//        }
//        catch (Exception e){
//            Log.d("Network Enabled", "False");
//        }
//
//        if (!gpsEnabled && !networkEnabled){
//            Toast.makeText(MainActivity.this, "Location Provider is not available", Toast.LENGTH_SHORT).show();
//        }

//        if (gpsEnabled){
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 600000, 1000, locationListener);
//        }
//        else if (networkEnabled){
//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 600000, 1000, locationListener);
//        }

        // Register the listener with the Location Manager to receive location updates
        // Can also set interval between each check
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 600000, 1000, locationListener);
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
        if (id == R.id.action_settings) {
            return true;
        }

        if (id == R.id.profile) {
            startActivity(new Intent(MainActivity.this, profileActivity.class));
        }
        else if (id == R.id.currentUser){
            if (ParseUser.getCurrentUser().getUsername() != null) {
                Toast.makeText(MainActivity.this, ParseUser.getCurrentUser().getUsername(), Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(MainActivity.this, "null", Toast.LENGTH_SHORT).show();
            }
        }
        else if (id == R.id.location){
            updateLocation(false, false);
        }
        else if (id == R.id.logOff){
            ParseUser.logOut();
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
        }
        else if (id == R.id.around){
            int aroundMeNumber;
            aroundMeNumber = post.getAroundMeNumber();
            Toast.makeText(MainActivity.this, "Number of People around: " + Integer.toString(aroundMeNumber), Toast.LENGTH_SHORT).show();
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
        Log.d("getting notified", "hiiii");
        post.itemViewAdapter.setClickListener(MainActivity.this);
    }
}
