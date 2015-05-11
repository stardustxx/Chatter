package com.sdust.chatter;

import android.content.Context;
import android.graphics.Point;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;

public class itemViewAdapter extends RecyclerView.Adapter<itemViewAdapter.myViewHolder> {

    private LayoutInflater inflater;
    List<ParseObject> feedItemList;
    Context context;
    int picWidth, picHeight;

    public ClickListener clickListener;

    public itemViewAdapter(Context context, List<ParseObject> feedItemList){
        inflater = LayoutInflater.from(context);
        this.feedItemList = feedItemList;
        this.context = context;
    }

    @Override
    public myViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View item = inflater.inflate(R.layout.custom_row, viewGroup, false);
        myViewHolder myViewHolder = new myViewHolder(item);

        // Getting display size
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        picWidth = size.x;
        picHeight = picWidth;

        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(myViewHolder viewHolder, int position) {
        ParseObject current = feedItemList.get(position);
        viewHolder.feedDesc.setText(current.get("Description").toString());
        viewHolder.feedUser.setText(current.get("User").toString());
        viewHolder.numberOfLikes.setText(Integer.toString(current.getInt("Likes")));
        viewHolder.updatePostLikes(current.getObjectId(), true);
        ParseFile feedImage = (ParseFile) current.get("feedImage");
        Glide.with(context).load(feedImage.getUrl()).placeholder(R.drawable.logo).override(picWidth, picHeight).centerCrop().crossFade().into(viewHolder.imageView);
    }

    public void addItem(List<ParseObject> newItems){
        feedItemList.addAll(newItems);
        notifyItemInserted(feedItemList.size());
    }

    @Override
    public int getItemCount() {
        return feedItemList.size();
    }

    class myViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView feedDesc, feedUser, numberOfLikes;
        ImageView imageView;
        ImageView heartImage;

        public myViewHolder(View itemView) {
            super(itemView);
            itemView.setOnClickListener(this);
            feedDesc = (TextView) itemView.findViewById(R.id.feedDesc);
            feedUser = (TextView) itemView.findViewById(R.id.nickname);
            numberOfLikes = (TextView) itemView.findViewById(R.id.numberOfLikes);
            imageView = (ImageView) itemView.findViewById(R.id.feedImage);
            heartImage = (ImageView) itemView.findViewById(R.id.heart);
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null){
//                clickListener.itemClicked(v, getPosition(), feedItemList.get(getPosition()).getObjectId());
                clickListener.itemClicked(v, feedItemList.get(getPosition()).getObjectId());
                updateLikes(feedItemList.get(getPosition()).getObjectId());             // Update likes from a user on a post
            }
        }

        public void updateLikes(final String postID){
            // We want the user to either like it once or unlike it
            // First find in Likes every post a user has liked
            final ParseQuery<ParseObject> queryLikes = ParseQuery.getQuery("Likes");
            ParseUser user = ParseUser.getCurrentUser();

            queryLikes.whereEqualTo("User", user);
            queryLikes.whereEqualTo("Post", postID);
            // Start the looking
            queryLikes.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> parseObjects, ParseException e) {
                    if (e == null){
                        // If the parseObjects contains something meaning it found something
                        // it should only have one data and delete that data, update UI as well
                        if (parseObjects.size() != 0) {
                            parseObjects.get(0).deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    updatePostLikes(postID, false);
                                    Glide.with(context).load(R.drawable.ic_favorite_outline_grey600_24dp).into(heartImage);
                                }
                            });
                        }
                        // If it couldn't find anything
                        // then add a row of data containing currentUser, and postID, update UI as well
                        else {
                            ParseObject likes = new ParseObject("Likes");
                            likes.put("User", ParseUser.getCurrentUser());
                            likes.put("Post", postID);
                            likes.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    updatePostLikes(postID, false);
                                    Glide.with(context).load(R.drawable.ic_favorite_grey600_24dp).into(heartImage);
                                }
                            });
                        }
                    }
                    else {
                        Toast.makeText(context, "Error in updating likes", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        public void updatePostLikes(final String postID, Boolean checking){
            if (!checking) {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Likes");
                query.whereEqualTo("Post", postID);
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e) {
                        if (e == null) {
                            final int resultSize = parseObjects.size();
                            numberOfLikes.setText(Integer.toString(resultSize));
                            Log.d("resultsize", Integer.toString(resultSize));
                            ParseQuery<ParseObject> postQuery = ParseQuery.getQuery("Post");
                            postQuery.getInBackground(postID, new GetCallback<ParseObject>() {
                                @Override
                                public void done(ParseObject parseObject, ParseException e) {
                                    if (e == null) {
                                        parseObject.put("Likes", resultSize);
                                        parseObject.saveInBackground();
                                    }
                                }
                            });
                        }
                    }
                });
            }
            else {
                ParseQuery<ParseObject> query = ParseQuery.getQuery("Likes");
                query.whereEqualTo("Post", postID);
                query.whereEqualTo("User", ParseUser.getCurrentUser());
                query.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> list, ParseException e) {
                        if (e == null){
                            if (list.size() == 0){
                                Glide.with(context).load(R.drawable.ic_favorite_outline_grey600_24dp).into(heartImage);
                            }
                            else {
                                Glide.with(context).load(R.drawable.ic_favorite_grey600_24dp).into(heartImage);
                            }
                        }
                        else {
                            Toast.makeText(context, "Error in updating post likes", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }

    public interface ClickListener {
//        public void itemClicked(View view, int position, String objectId);
        void itemClicked(View view, String objectId);
    }

    public void setClickListener(ClickListener clickListener){
        this.clickListener = clickListener;
    }
}
