package com.sdust.chatter;

import android.content.Context;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Observable;

/**
 * Created by Eric on 4/25/2015.
 */
public class Post extends Observable{
    private RecyclerView recyclerView;
    public itemViewAdapter itemViewAdapter;
    public itemViewProfileAdapter itemViewProfileAdapter;
    private Context context;
    private ParseUser parseUser;

    private int aroundMeNumber = 0;
    private Date lastPositionDate;
    private boolean needToLoad = false;

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
        parseUserQuery.whereWithinKilometers("Location", userLocation, 10.0);
        parseUserQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(final List<ParseUser> nearUsers, ParseException e) {
                if (e == null) {
                    Toast.makeText(context, "People around me: " + Integer.toString(nearUsers.size()), Toast.LENGTH_SHORT).show();
                    Log.d("around", Integer.toString(nearUsers.size()));
                    Log.d("result", nearUsers.toString());
                    aroundMeNumber = nearUsers.size() - 1;

                    // Grabbing posts that are created by the people that are near the current user
                    ParseQuery<ParseObject> query = new ParseQuery<ParseObject>("Post");
                    String[] nearUsersUsername = new String[nearUsers.size() + 1];
                    // Grabbing usernames from the near people result into array
                    for (int i = 0; i < nearUsers.size(); i++) {
                        nearUsersUsername[i] = nearUsers.get(i).getUsername();
                    }
                    nearUsersUsername[nearUsers.size()] = ParseUser.getCurrentUser().getUsername();
                    Log.d("array content", nearUsersUsername.toString());
                    query.whereContainedIn("User", Arrays.asList(nearUsersUsername));
                    query.orderByDescending("createdAt");
                    query.setLimit(10);
                    if (addingNewOnes) {
                        query.whereLessThan("createdAt", lastPositionDate);
                    }
                    query.findInBackground(new FindCallback<ParseObject>() {
                        @Override
                        public void done(List<ParseObject> posts, ParseException e) {
                            Log.d("Done", "Retrieved: " + posts.size());
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
                    Log.d("Error", "um");
                }
            }
        });
    }

    public void searchPost(final boolean addingNewOnes){
        // Grabbing posts that are created by the people that are near the current user
        Log.d("searching", "true");
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
                Log.d("Done", "Retrieved: " + posts.size());
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
                    itemViewProfileAdapter = new itemViewProfileAdapter(context, posts);
                    recyclerView.setAdapter(itemViewProfileAdapter);
//                                itemViewProfileAdapter.setClickListener(MainActivity.this);                // Set on click listener on the post, may be used in the future
                    lastPositionDate = posts.get(posts.size() - 1).getCreatedAt();      // Save the last item's created-at date
                    setChanged();
                    notifyObservers();
                }
            }
        });
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
