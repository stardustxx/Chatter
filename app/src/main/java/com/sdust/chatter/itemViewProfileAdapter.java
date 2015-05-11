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

import com.bumptech.glide.Glide;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.util.List;

public class itemViewProfileAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater inflater;
    List<ParseObject> feedItemList;
    Context context;
    ParseUser profileUser;
    int picWidth, picHeight;

//    public ClickListener clickListener;

    public itemViewProfileAdapter(Context context, List<ParseObject> feedItemList, ParseUser profileUser){
        inflater = LayoutInflater.from(context);
        this.feedItemList = feedItemList;
        this.context = context;
        this.profileUser = profileUser;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Getting display size
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        picWidth = size.x;
        picHeight = picWidth;

        if (viewType == 0){
            View item = inflater.inflate(R.layout.profileinfo, viewGroup, false);
            return new profileInfoViewHolder(item);
        }
        else {
            View item = inflater.inflate(R.layout.custom_profile_row, viewGroup, false);
            return new myViewHolder(item);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0){
            return 0;
        }
        else {
            return position;
        }
    }


    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof profileInfoViewHolder){
            String currentUsername = profileUser.getUsername();

            ((profileInfoViewHolder) viewHolder).userName.setText(currentUsername);

            //Find drawable for the scenery
//            Glide.with(context).load(R.drawable.scenery).into(((profileInfoViewHolder) viewHolder).profileBackground);
            // Need code for grabbing profile pic which I don't have right now
        }
        else if (viewHolder instanceof myViewHolder){
            ParseObject current = feedItemList.get(position - 1);
            ((myViewHolder) viewHolder).feedDesc.setText(current.get("Description").toString());
//            viewHolder.feedUser.setText(current.get("User").toString());
//            viewHolder.numberOfLikes.setText(Integer.toString(current.getInt("Likes")));
            ParseFile feedImage = (ParseFile) current.get("feedImage");
            Glide.with(context).load(feedImage.getUrl()).placeholder(R.drawable.logo).override(picWidth, picHeight).centerCrop().into(((myViewHolder) viewHolder).postImage);
        }
    }

    public void addItem(List<ParseObject> newItems){
        feedItemList.addAll(newItems);
        notifyItemInserted(feedItemList.size());
    }

    @Override
    public int getItemCount() {
        return feedItemList.size() + 1;
    }

    class profileInfoViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userDesc;
//        ImageView userPic;
//        ImageView profileBackground;

        public profileInfoViewHolder(View itemView) {
            super(itemView);
            userName = (TextView) itemView.findViewById(R.id.profileName);
//            userPic = (ImageView) itemView.findViewById(R.id.profileImage);
//            profileBackground = (ImageView) itemView.findViewById(R.id.profileBackground);
        }
    }

//    class myViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    class myViewHolder extends RecyclerView.ViewHolder {
        TextView feedDesc, feedUser, numberOfLikes;
        ImageView postImage;

        public myViewHolder(View itemView) {
            super(itemView);
//            itemView.setOnClickListener(this);
            feedDesc = (TextView) itemView.findViewById(R.id.feedDesc);
//            feedUser = (TextView) itemView.findViewById(R.id.nickname);
//            numberOfLikes = (TextView) itemView.findViewById(R.id.numberOfLikes);
            postImage = (ImageView) itemView.findViewById(R.id.feedImage);
        }

//        @Override
//        public void onClick(View v) {
//            if (clickListener != null){
////                clickListener.itemClicked(v, getPosition(), feedItemList.get(getPosition()).getObjectId());
//                clickListener.itemClicked(v, feedItemList.get(getPosition()).getObjectId());
//                updateLikes(feedItemList.get(getPosition()).getObjectId());             // Update likes from a user on a post
//            }
//        }

//        public void updateLikes(final String postID){
//            // We want the user to either like it once or unlike it
//            // First find in Likes every post a user has liked
//            final ParseQuery<ParseObject> queryLikes = ParseQuery.getQuery("Likes");
//            ParseUser user = ParseUser.getCurrentUser();
//
//            queryLikes.whereEqualTo("User", user);
//            queryLikes.whereEqualTo("Post", postID);
//            // Start the looking
//            queryLikes.findInBackground(new FindCallback<ParseObject>() {
//                @Override
//                public void done(List<ParseObject> parseObjects, ParseException e) {
//                    if (e == null){
//                        // If the parseObjects contains something meaning it found something
//                        // it should only have one data and delete that data, update UI as well
//                        if (parseObjects.size() != 0) {
//                            parseObjects.get(0).deleteInBackground(new DeleteCallback() {
//                                @Override
//                                public void done(ParseException e) {
//                                    updatePostLikes(postID);
//                                }
//                            });
//                        }
//                        // If it couldn't find anything
//                        // then add a row of data containing currentUser, and postID, update UI as well
//                        else {
//                            ParseObject likes = new ParseObject("Likes");
//                            likes.put("User", ParseUser.getCurrentUser());
//                            likes.put("Post", postID);
//                            likes.saveInBackground(new SaveCallback() {
//                                @Override
//                                public void done(ParseException e) {
//                                    updatePostLikes(postID);
//                                }
//                            });
//                        }
//                    }
//                    else {
//                        Log.d("error", e.toString());
//                    }
//                }
//            });
//        }
//
//        public void updatePostLikes(final String postID){
//            ParseQuery<ParseObject> query = ParseQuery.getQuery("Likes");
//            query.whereEqualTo("Post", postID);
//            query.findInBackground(new FindCallback<ParseObject>() {
//                @Override
//                public void done(List<ParseObject> parseObjects, ParseException e) {
//                    if (e == null){
//                        final int resultSize = parseObjects.size();
//                        numberOfLikes.setText(Integer.toString(resultSize));
//                        Log.d("resultsize", Integer.toString(resultSize));
//                        ParseQuery<ParseObject> postQuery = ParseQuery.getQuery("Post");
//                        postQuery.getInBackground(postID, new GetCallback<ParseObject>() {
//                            @Override
//                            public void done(ParseObject parseObject, ParseException e) {
//                                if (e == null){
//                                    parseObject.put("Likes", resultSize);
//                                    parseObject.saveInBackground();
////                                    numberOfLikes.setText(Integer.toString(resultSize));
//                                }
//                            }
//                        });
//                    }
//                }
//            });
//        }
    }

//    public interface ClickListener {
////        public void itemClicked(View view, int position, String objectId);
//        public void itemClicked(View view, String objectId);
//    }
//
//    public void setClickListener(ClickListener clickListener){
//        this.clickListener = clickListener;
//    }
}
