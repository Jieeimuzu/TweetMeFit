package com.example.james.tweetmefit;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


public class tweetText extends AppCompatActivity {
    SharedPreferences Prefs;
    SharedPreferences.Editor edit;
    String locationProvider;
    LocationManager locationManager;
    Location lastKnownLocation;
    public static EditText et;
    public static TextInputLayout til;
    Button attachLoc;
    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat mdformat = new SimpleDateFormat("dd/MM/yy ");
    String strDate = mdformat.format(calendar.getTime());
    int count = 0;
    ImageView iv;
    private encryptions en = new encryptions();
    private Bitmap imageBitmap;
    twitterInteraction Tweet = new twitterInteraction();
    double Lat;
    double Long;
    Boolean withLoc;
    Boolean addLoc = false;

    public void addLoc(View view) {
        addLoc = true;
        Toast.makeText(getApplicationContext(), "Location Added", Toast.LENGTH_LONG).show();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Prefs = getSharedPreferences(en.encrypt("Steps"), MODE_PRIVATE);
        edit = Prefs.edit();
        setContentView(R.layout.activity_tweet_text);
        iv = findViewById(R.id.img);
        attachLoc = findViewById(R.id.locButton);
        et = this.findViewById(R.id.etTest);
        til = this.findViewById(R.id.TIL);
        count = Prefs.getInt(strDate, 0);
        locationProvider = LocationManager.GPS_PROVIDER;
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        if (count <= 280) {
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(count)});
            til.setCounterMaxLength(count);
            Log.e("Counter Length Updated:", Integer.toString(til.getCounterMaxLength()));

        } else {
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(280)});
            til.setCounterMaxLength(280);
            Log.e("Counter Length Updated:", Integer.toString(til.getCounterMaxLength()));

        }
        if (savedInstanceState != null) {
            Bundle Ex = getIntent().getExtras();

            et.setText(Ex.getString("textData"));
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        lastKnownLocation = locationManager.getLastKnownLocation(locationProvider);
        if (lastKnownLocation.getLatitude() != 0 || lastKnownLocation.getLongitude() != 0) {
            attachLoc.setVisibility(View.VISIBLE);
            Lat = lastKnownLocation.getLatitude();
            Long = lastKnownLocation.getLongitude();
            Log.e("Vals", Lat + " " + Long);
            withLoc = true;
        } else {
            attachLoc.setVisibility(View.INVISIBLE);
            withLoc = false;
        }
        et = this.findViewById(R.id.etTest);
        til = this.findViewById(R.id.TIL);
        count = Prefs.getInt(strDate, 0);
        if (count <= 280) {
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(count)});
            til.setCounterMaxLength(count);
            Log.e("Counter Length Updated:", Integer.toString(til.getCounterMaxLength()));


        } else {
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(280)});
            til.setCounterMaxLength(280);
            Log.e("Counter Length Updated:", Integer.toString(til.getCounterMaxLength()));

        }
        Log.e("OnResumeTweet", Integer.toString(count));

    }

    public void tweetMsg(View view) {

        statusUpdateAsync statusUpdate = new statusUpdateAsync();
        statusUpdate.execute();
    }

    public class statusUpdateAsync extends AsyncTask<String, String, String> {
        boolean passed = false;

        @Override
        protected String doInBackground(String... strings) {
            String data = et.getText().toString();
            try {
                if (withLoc && addLoc) {
                    Tweet.statusUpdate("https://api.twitter.com/1.1/statuses/update.json", "lat=" + Lat + "&long=" + Long, et.getText().toString(), getSharedPreferences(en.encrypt("Keys"), MODE_PRIVATE), getApplicationContext());
                } else {
                    Tweet.statusUpdate("https://api.twitter.com/1.1/statuses/update.json", null, et.getText().toString(), getSharedPreferences(en.encrypt("Keys"), MODE_PRIVATE), getApplicationContext());
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } 

            return null;
        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {
            if (passed = true) {
                count = count - et.length();
                Prefs = getSharedPreferences(en.encrypt("Steps"), MODE_PRIVATE);
                edit = Prefs.edit();
                edit.putInt(strDate, count);
                edit.apply();
                et.getText().clear();
                addLoc = false;
                finish();
            }

        }
    }


}
