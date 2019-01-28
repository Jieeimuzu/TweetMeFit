package com.example.james.tweetmefit;

//please check the code

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.util.Random;


public class startPage extends AppCompatActivity {
    private static final String TAG = null;
    twitterAuth Tweet = new twitterAuth();
    SharedPreferences Prefs;
    SharedPreferences.Editor edit;
    Boolean loggedIn = false;
    private encryptions en = new encryptions();

    int REQUEST_IMAGE_CAPTURE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_page);
        if (isLocationPermissionGranted() && isStoragePermissionGranted()) {
            Prefs = getSharedPreferences(en.encrypt("Keys"), MODE_PRIVATE);
            edit = Prefs.edit();
            edit.putString(en.encrypt("CONSUMER_KEY"), en.encrypt("9sSVfs5tGO7QT4OLY5b0oh5b3"));
            edit.putString(en.encrypt("CONSUMER_SECRET"), en.encrypt("DFW1caHrGlEaaOwn6nSsUUIidwL32b5VOxmwWumLaVFDZLwToY"));
            edit.apply();

            try {
                if (Tweet.verifyUser(Prefs) || Prefs.contains(en.encrypt("ACCESS_TOKEN"))) {
                    Intent intent = new Intent(startPage.this, MainActivity.class);
                    startActivity(intent);
                } else {
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isLocationPermissionGranted() && isStoragePermissionGranted()) {
            try {
                if (Tweet.verifyUser(Prefs) || Prefs.contains(en.encrypt("ACCESS_TOKEN")))

                {
                    Intent intent = new Intent(startPage.this, MainActivity.class);
                    startActivity(intent);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }

    public boolean isLocationPermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
            return true;
        }
    }


    public void login(View view) {
        Intent intent = new Intent(startPage.this, MainActivity.class);
        try {
            if (!Tweet.verifyUser(Prefs))

            {
                LoginAsync Ls = new LoginAsync();
                Ls.execute();
            } else {
                startActivity(intent);
            }
        } catch (IOException e) {
            Log.e("IOexcep", e.toString());
        }
    }


    public class LoginAsync extends AsyncTask<String, String, String> {

        String html = null;

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... arg0) {

            Random random = new Random();
            StringBuilder sb = new StringBuilder();
            while (sb.length() < 32) {
                sb.append(Integer.toHexString(random.nextInt()));
            }

            long timeStamp = System.currentTimeMillis() / 1000;
            String[] params = {Long.toString(timeStamp), sb.toString()};

            try {
                Log.e("LogIng", "Hit");
                html = Tweet.requestToken("https://api.twitter.com/oauth/request_token", params, getSharedPreferences(en.encrypt("Keys"), MODE_PRIVATE));

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Intent intent = new Intent(startPage.this, webView.class);
            intent.putExtra("Html", html);
            startActivityForResult(intent, 302);

            return null;

        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {
        }

    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the work!
                    // display short notification stating permission granted


                } else {

                    // permission denied, you need to add code to deal with this!

                    Toast.makeText(startPage.this, "Please accept permissions before continuing", Toast.LENGTH_SHORT).show();

                }
                return;
            }
        }
    }

    public void cameraButton(View view) {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(startPage.this,
                    new String[]{Manifest.permission.CAMERA},
                    1);

        } else {

            REQUEST_IMAGE_CAPTURE = 1;
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {

                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

            Prefs = getSharedPreferences("data", MODE_PRIVATE);
            edit = Prefs.edit();
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");


            //profilePictureView.setImageBitmap(imageBitmap); // Set image to imageView

            String root = Environment.getDataDirectory().toString() +
                    "/data/com.example.student.stepper/Icons/";
            Log.e("RootLoc: ", root);

            File directory = new File(root);
            Random r = new Random();
            String imageFileName = "ProfilePicture" + (r.nextInt(1000));

            if (!directory.exists()) {

                directory.mkdir();
            }

            directory = new File(directory, imageFileName + ".png"
            );

            try {
                FileOutputStream output = new FileOutputStream(directory);

                imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, output);
                output.flush();
                output.close();

                edit.putString(en.encrypt("ProfilePictureFileLocation"), en.encrypt(directory.toString()));
                edit.apply();


            } catch (IOException e) {
                e.printStackTrace();
                Log.e("IO Exception", "Error saving profile pic from start page");
            }
        }
    }


}