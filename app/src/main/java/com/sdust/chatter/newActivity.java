package com.sdust.chatter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class newActivity extends AppCompatActivity {

    public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CHOOSE_IMAGE_REQUEST = 200;

    Uri fileUri;
    TextView desLabel;
    EditText desBox;
    ImageView postImage;
    File mediaFile;
    byte[] imageByte;
    int picWidth, picHeight;
    Button cameraBtn, galleryBtn;

    ParseObject post = new ParseObject("Post");
    ParseFile feedImage;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inSampleSize = 4;                                   // Reduced size of the taken picture by 0.25
                    Bitmap image = BitmapFactory.decodeFile(mediaFile.getPath(), options);
                    Glide.with(newActivity.this).load(mediaFile.getPath()).override(picWidth, picHeight).centerCrop().crossFade().into(postImage);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream(); // Convert image to byte
                    image.compress(Bitmap.CompressFormat.JPEG, 80, stream);     // Compress image to lower quality
                    imageByte = stream.toByteArray();
                    feedImage = new ParseFile("Chatter.jpg", imageByte);
                    feedImage.saveInBackground();
                    Toast.makeText(newActivity.this, "Image captured", Toast.LENGTH_SHORT).show();
                    break;
                case Activity.RESULT_CANCELED:
                    Toast.makeText(newActivity.this, "Image cancelled", Toast.LENGTH_SHORT).show();
                    break;
                default:
                    Toast.makeText(newActivity.this, "Image captured failed", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
        else if (requestCode == CHOOSE_IMAGE_REQUEST){
            switch(resultCode){
                case Activity.RESULT_OK:
                    Uri uri = data.getData();
                    Glide.with(newActivity.this).load(uri).override(picWidth, picHeight).centerCrop().crossFade().into(postImage);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream);
                        imageByte = stream.toByteArray();
                        feedImage = new ParseFile("Chatter.jpg", imageByte);
                        feedImage.saveInBackground();
                        Toast.makeText(newActivity.this, "Image selected", Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception e){
//                        Log.d("error", e.toString());
                    }
                    break;
                case Activity.RESULT_CANCELED:
//                    Log.d("choose image", "canceled");
                    break;
                default:
//                    Log.d("choose image default", "nothing to see");
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new);

        Toolbar toolbar = (Toolbar) findViewById(R.id.appBar);
        toolbar.setTitle("New post");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        windowsSize();
        desBox = (EditText) findViewById(R.id.desBox);
        desBox.setBackground(null);
        postImage = (ImageView) findViewById(R.id.postImage);
        cameraBtn = (Button) findViewById(R.id.camera);
        galleryBtn = (Button) findViewById(R.id.gallery);

        cameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create Media File to save image
                mediaFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
                if (mediaFile == null) {
                    Toast.makeText(newActivity.this, "Error while creating media file.", Toast.LENGTH_LONG).show();
                }
                fileUri = Uri.fromFile(mediaFile);

                // Start camera intent
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  // Set the image file name
                intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);

                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE); // Put intent, and also checking which activity is calling for camera
            }
        });

        galleryBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                // show only images and nothing else
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                // Always shows the chooser if multiple options are available
                startActivityForResult(Intent.createChooser(intent, "Select image"), CHOOSE_IMAGE_REQUEST);
            }
        });
    }

    private void uploadPost(){
        if (postImage.getDrawable() == null){
            Toast.makeText(newActivity.this, "Please double check if you have taken a picture", Toast.LENGTH_SHORT).show();
        }
        else {
            post.put("feedImage", feedImage);
            post.put("Description", desBox.getText().toString());
            post.put("User", ParseUser.getCurrentUser().getUsername());
            post.put("Likes", 0);
            post.saveInBackground();
            Toast.makeText(newActivity.this, "Success", Toast.LENGTH_SHORT).show();
            newActivity.this.finish();
        }
    }

    /** Create a File for saving an image or video */
    private static File getOutputMediaFile(int type){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Chatter");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
//                Log.d("Chatter", "failed to create directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE){
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_"+ timeStamp + ".jpg");
        } else if(type == MEDIA_TYPE_VIDEO) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "VID_"+ timeStamp + ".mp4");
        } else {
            return null;
        }

        return mediaFile;
    }

    private void windowsSize(){
        // Getting display size
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        picWidth = size.x;
        picHeight = picWidth;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_new, menu);
        return true;
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
        if (id == R.id.postButton){
            uploadPost();
        }
//        else if (id == R.id.camera){
//            // Create Media File to save image
//            mediaFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
//            if (mediaFile == null) {
//                Toast.makeText(newActivity.this, "Error while creating media file.", Toast.LENGTH_LONG).show();
//            }
//            fileUri = Uri.fromFile(mediaFile);
//
//            // Start camera intent
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);  // Set the image file name
//            intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
//
//            startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE); // Put intent, and also checking which activity is calling for camera
//        }
//        else if (id == R.id.library){
//            Intent intent = new Intent();
//            // show only images and nothing else
//            intent.setType("image/*");
//            intent.setAction(Intent.ACTION_GET_CONTENT);
//            // Always shows the chooser if multiple options are available
//            startActivityForResult(Intent.createChooser(intent, "Select image"), CHOOSE_IMAGE_REQUEST);
//        }

        return super.onOptionsItemSelected(item);
    }
}
