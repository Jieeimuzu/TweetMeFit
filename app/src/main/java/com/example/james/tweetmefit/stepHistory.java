package com.example.james.tweetmefit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Set;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class stepHistory extends AppCompatActivity {

    SharedPreferences Prefs;
    SharedPreferences.Editor edit;
    ArrayList<String> arr = new ArrayList<String>();
    String count = null;
    private BottomNavigationView bottomNavigation;
    encryptions en = new encryptions();
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_history);
        bottomNavigation = findViewById(R.id.navigation);

        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.getGlobHome:
                        Intent intent3 = new Intent(stepHistory.this, MainActivity.class);
                        startActivity(intent3);
                        break;
                    case R.id.getMe:
                        Intent intent2 = new Intent(stepHistory.this, userHome.class);
                        startActivity(intent2);
                        break;
                    case R.id.stepHistory:
                        break;
                    case R.id.tweetMe:
                        Intent intent = new Intent(stepHistory.this, tweetText.class);
                        startActivity(intent);
                        break;

                }
                return true;
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        ListView lv = findViewById(R.id.stepHistory);

        bottomNavigation.setSelectedItemId(R.id.stepHistory);

        Prefs = getSharedPreferences(en.encrypt("Steps"), MODE_PRIVATE);
        Map<String, ?> entries;
        Set<String> keys;
        entries = Prefs.getAll();
        keys = entries.keySet();
        for (String key : keys) {
            count = Integer.toString(Prefs.getInt(key, 0));
            if (count.length() > 3) {
                count = count.substring(0, count.length() - 3) + "," + count.substring(count.length() - 3, count.length());
            }

            arr.add(key + "                    " + count);

        }

        arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                arr);

        lv.setAdapter(arrayAdapter);
    }

    @Override
    public void onStop() {
        super.onStop();
        arrayAdapter.clear();
    }
}
