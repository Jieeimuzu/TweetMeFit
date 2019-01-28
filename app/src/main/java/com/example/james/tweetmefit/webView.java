package com.example.james.tweetmefit;

import android.app.Activity;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.io.IOException;

import static android.content.ContentValues.TAG;

public class webView extends Activity {

    private WebView webview;
    private SharedPreferences Prefs;
    private SharedPreferences.Editor edit;
    boolean loadingFinished = true;
    boolean redirect = false;
    twitterAuth Tweet = new twitterAuth();
    encryptions en = new encryptions();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Prefs = getSharedPreferences(en.encrypt("Keys"), MODE_PRIVATE);
        final String mimeType = "text/html";
        final String encoding = "UTF-8";
        MainActivity mm = new MainActivity();
        String html = getIntent().getStringExtra("Html");


        webview = findViewById(R.id.twitLogin);
        webview.setWebViewClient(new WebViewClient());
        webview.getSettings().setJavaScriptEnabled(true);
        webview.setBackgroundColor(Color.rgb(25, 25, 0));
        webview.setOverScrollMode(WebView.OVER_SCROLL_NEVER);
        webview.loadUrl("https://api.twitter.com/oauth/authorize?oauth_token=" + encryptions.decrypt(Prefs.getString(en.encrypt("ACCESS_TOKEN"), "null")));

        //webview.loadDataWithBaseURL("", html, mimeType, encoding, "");
        //webview.lo

        webview.setWebViewClient(new WebViewClient() {


            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String urlNewString) {
                if (!loadingFinished) {
                    redirect = true;
                }

                loadingFinished = false;
                webview.loadUrl(urlNewString);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.e(TAG, url);
                Uri uri = getIntent().getData();
                //Log.e (TAG, url);

                if (url != null && url.startsWith("tweetmefit://callback")) {
                    String callback = url;
                    String oauth_token = callback.substring(callback.indexOf("oauth_token=") + 12, callback.indexOf("&oauth_verifier="));
                    String oauth_verifier = callback.substring(callback.indexOf("&oauth_verifier=") + 16, callback.length());
                    Prefs = getSharedPreferences(en.encrypt("Keys"), MODE_PRIVATE);
                    edit = Prefs.edit();
                    edit.putString(en.encrypt("Oauth_token"), en.encrypt(oauth_token));
                    edit.putString(en.encrypt("Oauth_verifier"), en.encrypt(oauth_verifier));
                    Log.e(TAG, oauth_token + " Tok:Ver " + oauth_verifier);
                    edit.apply();
                    getToken tk = new getToken();
                    tk.execute();
                    finish();

                }
            }
        });
    }

    public class getToken extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... strings) {
            try {
                String token = Tweet.accessToken("https://api.twitter.com/oauth/access_token", getSharedPreferences(en.encrypt("Keys"), MODE_PRIVATE));
                Log.e(TAG, token);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;

        }

        @Override
        protected void onPostExecute(String strFromDoInBg) {

            Prefs = getSharedPreferences(en.encrypt("Keys"), MODE_PRIVATE);
            edit = Prefs.edit();
            //edit.remove("Oauth_verifier");
            //edit.remove("Oauth_token");
            edit.putBoolean(en.encrypt("loggedin"), true);
            edit.apply();

        }


    }
}
