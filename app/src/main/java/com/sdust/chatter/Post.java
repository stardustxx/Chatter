package com.sdust.chatter;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RefreshCallback;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Observable;

public class Post extends Observable{
    private RecyclerView recyclerView;
    public itemViewAdapter itemViewAdapter;
    public itemViewProfileAdapter itemViewProfileAdapter;
    private Context context;
    private ParseUser parseUser;
    public LocationManager locationManager;
    public LocationListener locationListener;

    private int aroundMeNumber = 0;
    private Date lastPositionDate;
    private boolean needToLoad = false;

    public Post(Context context){
        this.context = context;
    }

    public Post(Context context, RecyclerView recyclerView, itemViewAdapter itemViewAdapter){
        this.context = context;
        this.recyclerView = recyclerView;
        this.itemViewAdapter = itemViewAdapter;
    }

    public Post(Context context, RecyclerView recyclerView, itemViewAdapter itemViewAdapter, ParseUser parseUser){
        this.context = context;
        this.recyclerView = recyclerView;
        this.itemViewAdapter = itemViewAdapter;
    }

    public Post(Context context, RecyclerView recyclerView, itemViewProfileAdapter itemViewProfileAdapter, ParseUser parseUser){
        this.context = context;
        this.recyclerView = recyclerView;
        this.itemViewProfileAdapter = itemViewProfileAdapter;
        this.parseUser = parseUser;
    }

    public void grabPost(final boolean addingNewOnes){
        ParseGeoPoint userLocation = (ParseGeoPoint) ParseUser.getCurrentUser().get("Location");
        ParseQuery<ParseUser> parseUserQuery  = ParseUser.getQuery();
        parseUserQuery.whereWithinKilometers("Location", userLocation, 1000.0);
//        Log.d("location", userLocation.toString());
        parseUserQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(final List<ParseUser> nearUsers, ParseException e) {
                if (e == null) {
                    if (nearUsers.size() != 0){
                        aroundMeNumber = nearUsers.size() - 1;
                    }
                    Toast.makeText(context, "Number of people around: " + Integer.toString(aroundMeNumber), Toast.LENGTH_SHORT).show();

                    // Grabbing posts that are created by the people that are near the current user
                    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Post");
                    String[] nearUsersUsername = new String[nearUsers.size() + 1];
                    // Grabbing usernames from the near people result into array
                    for (int i = 0; i < nearUsers.size(); i++) {
                        nearUsersUsername[i] = nearUsers.get(i).getUsername();
                    }
                    nearUsersUsername[nearUsers.size()] = ParseUser.getCurrentUser().getUsername();
                    query.whereContainedIn("User", Arrays.asList(nearUsersUsername));
                    query.orderByDescending("createdAt");
                    query.setLimit(10);
                    if (addingNewOnes) {
                        query.whereLessThan("createdAt", lastPositionDate);
                    }
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> posts, ParseException e) {
                            if (posts.size() == 0){
                                // Doing nothing
                                Toast.makeText(context, "Nothing to show", Toast.LENGTH_SHORT).show();
                            }
                            else if (addingNewOnes && needToLoad){
                                needToLoad = false;
                                itemViewAdapter.addItem(posts);
                                DefaultItemAnimator animator = new DefaultItemAnimator();
                                recyclerView.setItemAnimator(animator);
                                lastPositionDate = posts.get(posts.size() - 1).getCreatedAt();      // Save the last item's created-at date
                            }
                            else {
                                itemViewAdapter = new itemViewAdapter(context, posts);
                                recyclerView.setAdapter(itemViewAdapter);
//                                itemViewAdapter.setClickListener(MainActivity.this);                // Set on click listener on the post, may be used in the future
                                lastPositionDate = posts.get(posts.size() - 1).getCreatedAt();      // Save the last item's created-at date
                                setChanged();
                                notifyObservers();
                            }
                        }
                    });
                } else {
                    Toast.makeText(context, "Error in updating post", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void searchPost(final boolean addingNewOnes){
        // Grabbing posts that are created by the people that are near the current user
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Post");
        query.whereEqualTo("User", parseUser.getUsername());
        query.orderByDescending("createdAt");
        query.setLimit(10);
        if (addingNewOnes) {
            query.whereLessThan("createdAt", lastPositionDate);
        }
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> posts, ParseException e) {
                if (posts.size() == 0){
                    // Doing nothing
                    Toast.makeText(context, "Nothing to show", Toast.LENGTH_SHORT).show();
                }
                else if (addingNewOnes && needToLoad){
                    needToLoad = false;
                    itemViewProfileAdapter.addItem(posts);
                    DefaultItemAnimator animator = new DefaultItemAnimator();
                    recyclerView.setItemAnimator(animator);
                    lastPositionDate = posts.get(posts.size() - 1).getCreatedAt();      // Save the last item's created-at date
                }
                else {
                    itemViewProfileAdapter = new itemViewProfileAdapter(context, posts, ParseUser.getCurrentUser());
                    recyclerView.setAdapter(itemViewProfileAdapter);
//                                itemViewProfileAdapter.setClickListener(MainActivity.this);                // Set on click listener on the post, may be used in the future
                    lastPositionDate = posts.get(posts.size() - 1).getCreatedAt();      // Save the last item's created-at date
                    setChanged();
                    notifyObservers();
                }
            }
        });
    }

    public void updateLocation(){
//        boolean gpsEnabled = false;
//        boolean networkEnabled = false;
//        final float[] distanceResult = new float[1];

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the network location provider.
                final double latitude = location.getLatitude();           // x
                final double longitude = location.getLongitude();         // y

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
//                            ParseGeoPoint serverUserLocation = (ParseGeoPoint) userFound.get("Location");
//                            Location.distanceBetween(serverUserLocation.getLatitude(), serverUserLocation.getLongitude(), latitude, longitude, distanceResult);
                            userFound.put("Location", currentLocation);
                            userFound.saveInBackground();
//                            Toast.makeText(MainActivity.this, currentLocation.toString(), Toast.LENGTH_SHORT).show();
                            ParseUser.getCurrentUser().refreshInBackground(new RefreshCallback() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
//                                    if (e == null){
//                                        if (updateAnyway){
//                                            post.grabPost(false);
//                                        }
//                                        else if (distanceResult[0] > 10000 && grabPostToo) {
//                                            post.grabPost(false);
//                                        }
//                                        swipeRefreshLayout.setRefreshing(false);
//                                    }
//                                    else {
//                                        Toast.makeText(MainActivity.this, "Error in updating user data", Toast.LENGTH_SHORT).show();
//                                    }
                                    if (e != null) {
                                        Toast.makeText(context, "Error in updating user data", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        else {
                            Toast.makeText(context, "Error in looking up user", Toast.LENGTH_SHORT).show();
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
        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)){
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 600000, 1000, locationListener);
        }
        else {
            Toast.makeText(context, "It appears you don't have location service turned on", Toast.LENGTH_SHORT).show();
            context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    public int getAroundMeNumber() {
        return aroundMeNumber;
    }

    public void setAroundMeNumber(int aroundMeNumber) {
        this.aroundMeNumber = aroundMeNumber;
    }

    public Date getLastPositionDate() {
        return lastPositionDate;
    }

    public void setLastPositionDate(Date lastPositionDate) {
        this.lastPositionDate = lastPositionDate;
    }

    public boolean isNeedToLoad() {
        return needToLoad;
    }

    public void setNeedToLoad(boolean needToLoad) {
        this.needToLoad = needToLoad;
    }
}
