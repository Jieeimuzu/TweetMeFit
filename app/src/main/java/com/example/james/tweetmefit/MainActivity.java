package com.example.james.tweetmefit;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    twitterInteraction twitInt = new twitterInteraction();

    private SensorManager sensorManager;
    private Sensor stepSensor;
    public static TextView tv;


    public static boolean activityRunning;
    public int count;
    ListView list;
    String un[];
    String tweet[];
    String postDate[];
    String uri[];
    String tweetImgUrl[];
    long max_id = 0;
    int listPos;
    Boolean isActive = false;
    tweetRetreiveAdapter tweets;
    private static final String TAG = MainActivity.class.getSimpleName();
    private BottomNavigationView bottomNavigation;
    private tweetDBHelper tweetDB;

    public static SharedPreferences Prefs;
    SharedPreferences.Editor edit;

    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat mdformat = new SimpleDateFormat("dd/MM/yy ");
    String strDate = mdformat.format(calendar.getTime());
    encryptions en = new encryptions();

    Intent intent;


    //TODO: Finish Volley(?), Error Handing, Network Handling-50%
    //TODO: Post Photo (If possible video) If possible, Show video, update tweets periodically
    //TODO: Pull to refresh, sort Picasso Photos to match, sort step counter display,

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tv = findViewById(R.id.charCount);
        Log.e(TAG, strDate);

        list = findViewById(R.id.tweets);
        tweetDB = new tweetDBHelper(this, "tweets");
        tweets = new tweetRetreiveAdapter(getApplicationContext(), tweetDB);
        list.setAdapter(tweets);
        tv.setText(getSharedPreferences(en.encrypt("Steps"), MODE_PRIVATE).getInt(strDate, 0) + "Characters left");

    }


    int hit = 0;

    @Override
    protected void onResume() {
        super.onResume();

        bottomNavigation = findViewById(R.id.navigation);

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.getGlobHome:
                        break;
                    case R.id.getMe:
                        Intent intent2 = new Intent(MainActivity.this, userHome.class);
                        startActivity(intent2);
                        break;
                    case R.id.stepHistory:
                        Intent intent1 = new Intent(MainActivity.this, stepHistory.class);
                        startActivity(intent1);
                        break;
                    case R.id.tweetMe:
                        Intent intent = new Intent(MainActivity.this, tweetText.class);
                        startActivity(intent);

                        populateTweet();

                }
                return true;
            }
        });


        bottomNavigation.setSelectedItemId(R.id.getGlobHome);


        Log.e("ResumeHit", Integer.toString(hit));
        intent = new Intent(MainActivity.this, stepService.class);
        //Check if service is running
        if (!isMyServiceRunning(intent.getClass())) {
            startService(intent);
            Log.e(TAG, "Service Running");
        }

        list.setClickable(true);
        if (isNetworkAvailable() == true) {
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Log.e("Click item", String.valueOf(position) + list.getContentDescription());
                    //String o = (String) list.getItemAtPosition(position);
                    TextView tv = view.findViewById(R.id.tweetID);


                }
            });
            list.setOnScrollListener(new AbsListView.OnScrollListener() {
                long timer = 0;

                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    int threshold = 1;
                    int count = list.getCount();
                    if (scrollState == SCROLL_STATE_IDLE
                            && (list.getLastVisiblePosition() >= count - threshold) && timer == 0) {
                        timer = System.currentTimeMillis();
                        Log.e("List:", " Hit Bottom");

                        extendPopTweet();

                        //Code to ensure a user can't double request more data
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                while (timer + 1500 != System.currentTimeMillis()) {
                                }
                                timer = 0;
                            }
                        });
                        thread.start();
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                }
            });

            populateTweet();
            Log.e("Resumes", "MainActivity");
        } else {
            Toast.makeText(getApplicationContext(), "No Network Avaliable", Toast.LENGTH_LONG).show();

            tweets.notifyDataSetChanged();
            list.invalidate();
        }
        count = getSharedPreferences(en.encrypt("Steps"), MODE_PRIVATE).getInt(strDate, 0);


        activityRunning = true;
        tv.setText(count + " Characters left today");

        Log.e("Setting", "ListAdapter");

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
    }


    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("isMyServiceRunning?", true + "");
                return true;
            }
        }
        Log.i("isMyServiceRunning?", false + "");
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("Pause", "Hit");
    }

    int val;

    @Override
    public void onStop() {
        super.onStop();
        isActive = false;
    }

    @Override
    public void onStart() {
        super.onStart();
        isActive = true;

    }

    public void extendPopTweet() {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        JsonArrayRequest stringRequest = new JsonArrayRequest
                (Request.Method.GET, "https://api.twitter.com/1.1/statuses/home_timeline.json?tweet_mode=extended&max_id=" + String.valueOf(max_id), null, new Response.Listener<JSONArray>() {
                    //JsonObjectRequest stringRequest = new JsonObjectRequest (Request.Method.GET, "https://api.twitter.com/1.1/statuses/home_timeline.json?count=80",null,   new Response.Listener<String>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.e("The Response ", response.toString());

                        try {
                            JSONArray jsonArray = new JSONArray(response.toString());
                            JSONObject maxidJS = jsonArray.getJSONObject(jsonArray.length() - 1);
                            max_id = (long) maxidJS.get("id");
                            String tweetImgUrl = null;
                            String tweetText;
                            String Username;
                            String profilePic;
                            String userTag = null;
                            // loop through json array and add each tweet to item in arrayList
                            for (int i = 0; i < jsonArray.length(); i++) {
                                Log.e("Arr Len", Integer.toString(jsonArray.length()));
                                JSONObject json_message = jsonArray.getJSONObject(i);
                                JSONArray json = new JSONArray("[" + json_message.getString("user") + "]");
                                JSONArray imgJson = new JSONArray("[" + json_message.getString("entities") + "]");


                                if (imgJson.getJSONObject(0).has("media")) {

                                    Log.e("Media", Integer.toString(jsonArray.length()));
                                    imgJson = new JSONArray(imgJson.getJSONObject(0).getString("media"));
                                    if (imgJson.getJSONObject(0).has("media_url")) {
                                        tweetImgUrl = imgJson.getJSONObject(0).getString("media_url");
                                    }
                                }
                                if (json_message.has("retweeted_status")) {


                                    JSONArray rtText = new JSONArray("[" + json_message.getString("retweeted_status") + "]");
                                    tweetText = rtText.getJSONObject(0).getString("full_text");

                                    JSONArray user = new JSONArray("[" + rtText.getJSONObject(0).getString("user") + "]");
                                    Username = json.getJSONObject(0).getString("screen_name") + " RT @"
                                            + user.getJSONObject(0).getString("screen_name");


                                    imgJson = new JSONArray("[" + rtText.getJSONObject(0).getString("entities") + "]");
                                    if (imgJson.getJSONObject(0).has("media")) {
                                        Log.e("Media", Integer.toString(jsonArray.length()));
                                        imgJson = new JSONArray(imgJson.getJSONObject(0).getString("media"));
                                        tweetImgUrl = imgJson.getJSONObject(0).getString("media_url");
                                        userTag = "t By " + json.getJSONObject(0).getString("name");
                                    } else {
                                        tweetImgUrl = null;
                                    }

                                    tweetImgUrl = imgJson.getJSONObject(0).getString("media_url");

                                    profilePic = user.getJSONObject(0).getString("profile_image_url").replace("_normal", "");
                                } else {
                                    userTag = "Tweet By " + json.getJSONObject(0).getString("name");
                                    tweetText = json_message.getString("full_text");
                                    Username = json.getJSONObject(0).getString("screen_name");
                                    tweetImgUrl = null;
                                    profilePic = json.getJSONObject(0).getString("profile_image_url");
                                }

                                if (json_message != null) {

                                    tweetDB.addTweet(
                                            Long.toString(json_message.getLong("id")),
                                            Username,
                                            userTag,
                                            tweetText,
                                            json_message.getString("created_at").substring(0, json_message.getString("created_at").indexOf('+')),
                                            tweetImgUrl,
                                            profilePic);
                                }
                            }
                            tweets.appendDataTweetRetAdp(getApplicationContext(), tweetDB);
                            //list.setAdapter(tweets);

                            tweets.notifyDataSetChanged();
                            list.invalidateViews();
                            list.scrollBy(0, 0);


                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("JsonErr", e.toString());
                        }

                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("RespErr", error.toString());
                        Toast.makeText(getApplicationContext(), "Too many calls made to twiter in 15 minute window. Please try again in 15 minutes.", Toast.LENGTH_LONG).show();
                        tweets = new tweetRetreiveAdapter(getApplicationContext(), tweetDB);
                        list.setAdapter(tweets);
                    }
                }
                ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                //set request headers
                params.put("Content-type", "application/x-www-form-urlencoded");
                params.put("Accept-encoding", "application/json");
                params.put("User-agent", "tweetMeFit' HTTP Client");
                params.put("Connection", "Keep-Alive");
                //conn.addRequestProperty("Expect", "100-Continue");
                params.put("Host", "api.twitter.com");
                //Set the oAuth params in header
                try {
                    params.put("Authorization", twitInt.getHome2("https://api.twitter.com/1.1/statuses/home_timeline.json", "max_id=" + String.valueOf(max_id), getSharedPreferences(en.encrypt("Keys"), MODE_PRIVATE), getApplicationContext()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return params;
            }
        };
        queue.add(stringRequest);
    }

    public void populateTweet() {

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        JsonArrayRequest stringRequest = new JsonArrayRequest
                (Request.Method.GET, "https://api.twitter.com/1.1/statuses/home_timeline.json?tweet_mode=extended", null, new Response.Listener<JSONArray>() {
                    //JsonObjectRequest stringRequest = new JsonObjectRequest (Request.Method.GET, "https://api.twitter.com/1.1/statuses/home_timeline.json?count=80",null,   new Response.Listener<String>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.e("The Response ", response.toString());

                        try {
                            JSONArray jsonArray = new JSONArray(response.toString());
                            JSONObject maxidJS = jsonArray.getJSONObject(jsonArray.length() - 1);
                            max_id = (long) maxidJS.get("id");
                            if (jsonArray.getJSONObject(0).getLong("id") != tweetDB.getID()) {
                                String tweetImgUrl = null;
                                String tweetText;
                                String Username;
                                String profilePic;
                                String userTag = null;
                                // loop through json array and add each tweet to item in arrayList
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    Log.e("Arr Len", Integer.toString(jsonArray.length()));
                                    JSONObject json_message = jsonArray.getJSONObject(i);
                                    JSONArray json = new JSONArray("[" + json_message.getString("user") + "]");
                                    JSONArray imgJson = new JSONArray("[" + json_message.getString("entities") + "]");


                                    if (imgJson.getJSONObject(0).has("media")) {

                                        Log.e("Media", Integer.toString(jsonArray.length()));
                                        imgJson = new JSONArray(imgJson.getJSONObject(0).getString("media"));
                                        if (imgJson.getJSONObject(0).has("media_url")) {
                                            tweetImgUrl = imgJson.getJSONObject(0).getString("media_url");
                                        }
                                    }
                                    if (json_message.has("retweeted_status")) {


                                        JSONArray rtText = new JSONArray("[" + json_message.getString("retweeted_status") + "]");
                                        tweetText = rtText.getJSONObject(0).getString("full_text");

                                        JSONArray user = new JSONArray("[" + rtText.getJSONObject(0).getString("user") + "]");
                                        Username = json.getJSONObject(0).getString("screen_name") + " RT @"
                                                + user.getJSONObject(0).getString("screen_name");


                                        imgJson = new JSONArray("[" + rtText.getJSONObject(0).getString("entities") + "]");
                                        if (imgJson.getJSONObject(0).has("media")) {
                                            Log.e("Media", Integer.toString(jsonArray.length()));
                                            imgJson = new JSONArray(imgJson.getJSONObject(0).getString("media"));
                                            tweetImgUrl = imgJson.getJSONObject(0).getString("media_url");
                                            userTag = "Retweet By " + json.getJSONObject(0).getString("name");
                                        } else {
                                            tweetImgUrl = null;
                                        }

                                        tweetImgUrl = imgJson.getJSONObject(0).getString("media_url");

                                        profilePic = user.getJSONObject(0).getString("profile_image_url").replace("_normal", "");
                                    } else {
                                        userTag = "Tweet By " + json.getJSONObject(0).getString("name");
                                        tweetText = json_message.getString("full_text");
                                        Username = json.getJSONObject(0).getString("screen_name");
                                        tweetImgUrl = null;
                                        profilePic = json.getJSONObject(0).getString("profile_image_url");
                                    }

                                    if (json_message != null) {

                                        tweetDB.addTweet(
                                                Long.toString(json_message.getLong("id")),
                                                Username,
                                                userTag,
                                                tweetText,
                                                json_message.getString("created_at").substring(0, json_message.getString("created_at").indexOf('+')),
                                                tweetImgUrl,
                                                profilePic);
                                    }
                                }
                            } else {
                                Log.e("TopID", "Matches");
                            }
                            tweets = new tweetRetreiveAdapter(getApplicationContext(), tweetDB);
                            list.setAdapter(tweets);

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("JsonErr", e.toString());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("RespErr", error.toString());
                        Toast.makeText(getApplicationContext(), "Too many calls made to twiter in 15 minute window. Please try again in 15 minutes.", Toast.LENGTH_LONG).show();
                        tweets = new tweetRetreiveAdapter(getApplicationContext(), tweetDB);
                        list.setAdapter(tweets);
                    }
                }
                ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                //set headers
                //params.put("Content-type", "application/x-www-form-urlencoded");
                params.put("Accept-encoding", "application/json");
                params.put("User-agent", "tweetMeFit' HTTP Client");
                //params.put("Connection", "Keep-Alive");
                params.put("Expect", "100-Continue");
                params.put("Host", "api.twitter.com");
                //Set the oAuth params in header
                try {
                    params.put("Authorization", twitInt.getHome2("https://api.twitter.com/1.1/statuses/home_timeline.json", null, getSharedPreferences(en.encrypt("Keys"), MODE_PRIVATE), getApplicationContext()));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                return params;
            }
        };

        queue.add(stringRequest);

    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

}