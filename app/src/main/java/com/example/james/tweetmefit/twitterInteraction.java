package com.example.james.tweetmefit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class twitterInteraction {
    private encryptions en = new encryptions();

    public boolean statusUpdate(final String baseURL, String params, final String data, SharedPreferences prefs, final Context context) throws UnsupportedEncodingException {
        int msg = 0;
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 32) {
            sb.append(Integer.toHexString(random.nextInt()));
        }
        String url;
        long timeStamp = System.currentTimeMillis() / 1000;
        String signatureBaseString = null;
        if (params != null) {
            signatureBaseString = "POST&" +
                    URLEncoder.encode(baseURL, "UTF-8") + "&" + URLEncoder.encode(params, "UTF-8") +
                    "%26oauth_consumer_key%3D" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY"), null)), "UTF-8") +
                    "%26oauth_nonce%3D" + URLEncoder.encode(sb.toString(), "UTF-8") +
                    "%26oauth_signature_method%3D" + URLEncoder.encode("HMAC-SHA1", "UTF-8") +
                    "%26oauth_timestamp%3D" + URLEncoder.encode(Long.toString(timeStamp), "UTF-8") +
                    "%26oauth_token%3D" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN"), null)), "UTF-8") +
                    "%26oauth_version%3D" + URLEncoder.encode("1.0", "UTF-8") +
                    "%26status%3D" + URLEncoder.encode(URLEncoder.encode(data, "UTF-8"), "UTF-8");
            url = baseURL + "?" + params + "&status=" + URLEncoder.encode(URLEncoder.encode(data, "UTF-8"), "UTF-8");

        } else {
            signatureBaseString = "POST&" +
                    URLEncoder.encode(baseURL, "UTF-8") + "&" +
                    "oauth_consumer_key%3D" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY"), null)), "UTF-8") +
                    "%26oauth_nonce%3D" + URLEncoder.encode(sb.toString(), "UTF-8") +
                    "%26oauth_signature_method%3D" + URLEncoder.encode("HMAC-SHA1", "UTF-8") +
                    "%26oauth_timestamp%3D" + URLEncoder.encode(Long.toString(timeStamp), "UTF-8") +
                    "%26oauth_token%3D" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN"), null)), "UTF-8") +
                    "%26oauth_version%3D" + URLEncoder.encode("1.0", "UTF-8") +
                    "%26status%3D" + URLEncoder.encode(URLEncoder.encode(data, "UTF-8"), "UTF-8");
            url = baseURL + "?status=" + URLEncoder.encode(URLEncoder.encode(data, "UTF-8"), "UTF-8");
        }


        Log.e("Signature Base: ", signatureBaseString);

        //Our signing key is (notice the dangling ampersand at the end):
        String signingKey = encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_SECRET"), null)) + "&"
                + encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN_SECRET"), null));
        String signature = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(signingKey.getBytes(),
                    "HmacSHA1");
            mac.init(secret);
            byte[] digest = mac.doFinal(signatureBaseString.getBytes());
            //We then use the composite signing key to create an oauth_signature from the signature base string
            signature = new String(Base64.encode(digest, Base64.DEFAULT)).replaceAll(System.getProperty("line.separator"), "");
            System.out.println("The resultant oauth_signature is: " + signature);
        } catch (Exception ex1) {
            ex1.printStackTrace();
            System.exit(0);
        }

        //Now we just generate an HTTP header called "Authorization" with the relevant OAuth parameters for the request:
        final String oAuthParameters = "OAuth "
                + "oauth_consumer_key=\"" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY"), null)), "UTF-8") + "\""
                + ", oauth_nonce=\"" + URLEncoder.encode(String.valueOf(sb.toString()), "UTF-8") + "\""
                + ", oauth_signature=\"" + URLEncoder.encode(signature, "UTF-8") + "\""
                + ", oauth_signature_method=\"" + URLEncoder.encode("HMAC-SHA1", "UTF-8") + "\""
                + ", oauth_timestamp=\"" + URLEncoder.encode(Long.toString(timeStamp), "UTF-8") + "\""
                + ", oauth_token=\"" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN"), null)), "UTF-8") + "\""
                + ", oauth_version=\"" + URLEncoder.encode("1.0", "UTF-8") + "\"";
        Log.e("oAuth Parameters are ", oAuthParameters);

        Log.e("oAuthParameters Base: ", oAuthParameters);


        RequestQueue queue = Volley.newRequestQueue(context);
        StringRequest stringRequest = new StringRequest
                (Request.Method.POST, url, new Response.Listener<String>() {
                    //JsonObjectRequest stringRequest = new JsonObjectRequest (Request.Method.GET, "https://api.twitter.com/1.1/statuses/home_timeline.json?count=80",null,   new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e("The Respons updatee ", response);


                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Log.e("The Error Response ", baseURL + bodyParams );
                        Intent intent = new Intent(context, tweetText.class);
                        intent.putExtra("textData", data);
                        Toast.makeText(context, "Failed to post status", Toast.LENGTH_LONG).show();
                        context.startActivity(intent);

                    }
                }
                ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                //set request headers
                params.put("Content-type", "application/json");
                params.put("Accept-encoding", "application/json");
                params.put("User-agent", "tweetMeFit' HTTP Client");
                params.put("Connection", "Keep-Alive");
                params.put("Expect", "100-Continue");
                params.put("Host", "api.twitter.com");
                params.put("Authorization", oAuthParameters);
                Log.e("Headers", "hit");
                //Set the oAuth params in header
                return params;
            }
        };
        queue.add(stringRequest);
        return true;

    }


    public String standardOauth(String baseURL, SharedPreferences prefs, final Context context) throws UnsupportedEncodingException {
        int msg = 0;
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 32) {
            sb.append(Integer.toHexString(random.nextInt()));
        }

        long timeStamp = System.currentTimeMillis() / 1000;

        String signatureBaseString = "POST&" +
                URLEncoder.encode(baseURL, "UTF-8") + "&" +
                "oauth_consumer_key%3D" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY"), null)), "UTF-8") +
                "%26oauth_nonce%3D" + URLEncoder.encode(sb.toString(), "UTF-8") +
                "%26oauth_signature_method%3D" + URLEncoder.encode("HMAC-SHA1", "UTF-8") +
                "%26oauth_timestamp%3D" + URLEncoder.encode(Long.toString(timeStamp), "UTF-8") +
                "%26oauth_token%3D" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN"), null)), "UTF-8") +
                "%26oauth_version%3D" + URLEncoder.encode("1.0", "UTF-8");


        System.out.println("Signature Base: " + signatureBaseString);

        //Our signing key is (notice the dangling ampersand at the end):
        String signingKey = encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_SECRET"), null)) + "&"
                + encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN_SECRET"), null));
        String signature = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(signingKey.getBytes(),
                    "HmacSHA1");
            mac.init(secret);
            byte[] digest = mac.doFinal(signatureBaseString.getBytes());
            //We then use the composite signing key to create an oauth_signature from the signature base string
            signature = new String(Base64.encode(digest, Base64.DEFAULT)).replaceAll(System.getProperty("line.separator"), "");
            System.out.println("The resultant oauth_signature is: " + signature);
        } catch (Exception ex1) {
            ex1.printStackTrace();
            System.exit(0);
        }

        //Now we just generate an HTTP header called "Authorization" with the relevant OAuth parameters for the request:
        final String oAuthParameters = "OAuth "
                + "oauth_consumer_key=\"" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY"), null)), "UTF-8") + "\""
                + ", oauth_nonce=\"" + URLEncoder.encode(String.valueOf(sb.toString()), "UTF-8") + "\""
                + ", oauth_signature=\"" + URLEncoder.encode(signature, "UTF-8") + "\""
                + ", oauth_signature_method=\"" + URLEncoder.encode("HMAC-SHA1", "UTF-8") + "\""
                + ", oauth_timestamp=\"" + URLEncoder.encode(Long.toString(timeStamp), "UTF-8") + "\""
                + ", oauth_token=\"" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN"), null)), "UTF-8") + "\""
                + ", oauth_version=\"" + URLEncoder.encode("1.0", "UTF-8") + "\"";
        Log.e("oAuth Parameters are ", oAuthParameters);

        return oAuthParameters;
    }

/*
    public String getHome(String baseURL, String data, SharedPreferences prefs) throws UnsupportedEncodingException, MalformedURLException {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 32) {
            sb.append(Integer.toHexString(random.nextInt()));
        }

        long timeStamp = System.currentTimeMillis()/1000;
        String signatureBaseString = null;
        StringBuffer dataBack = new StringBuffer();
        if(data != null) {
            signatureBaseString = "GET&" +
                    URLEncoder.encode(baseURL, "UTF-8") + "&" + URLEncoder.encode(data.substring(1, data.length()), "UTF-8") +
                    "%26oauth_consumer_key%3D" + URLEncoder.encode(en.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY", null), "UTF-8") +
                    "%26oauth_nonce%3D" + URLEncoder.encode(sb.toString(), "UTF-8") +
                    "%26oauth_signature_method%3D" + URLEncoder.encode("HMAC-SHA1", "UTF-8") +
                    "%26oauth_timestamp%3D" + URLEncoder.encode(Long.toString(timeStamp), "UTF-8") +
                    "%26oauth_token%3D" + URLEncoder.encode(en.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN", null), "UTF-8") +
                    "%26oauth_version%3D" + URLEncoder.encode("1.0", "UTF-8");
        }
        else
        {
            signatureBaseString = "GET&" +
                    URLEncoder.encode(baseURL, "UTF-8") + "&" +
                    "oauth_consumer_key%3D" + URLEncoder.encode(en.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY", null), "UTF-8") +
                    "%26oauth_nonce%3D" + URLEncoder.encode(sb.toString(), "UTF-8") +
                    "%26oauth_signature_method%3D" + URLEncoder.encode("HMAC-SHA1", "UTF-8") +
                    "%26oauth_timestamp%3D" + URLEncoder.encode(Long.toString(timeStamp), "UTF-8") +
                    "%26oauth_token%3D" + URLEncoder.encode(en.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN", null), "UTF-8") +
                    "%26oauth_version%3D" + URLEncoder.encode("1.0", "UTF-8");
        }


        System.out.println("Signature Base: "+ signatureBaseString );

        //Our signing key is (notice the dangling ampersand at the end):
        String signingKey = en.decrypt(prefs.getString(en.encrypt("CONSUMER_SECRET", null) + "&" + en.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN_SECRET", null);
        String signature = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(signingKey.getBytes(),
                    "HmacSHA1");
            mac.init(secret);
            byte[] digest = mac.doFinal(signatureBaseString.getBytes());
            //We then use the composite signing key to create an oauth_signature from the signature base string
            signature = new String (Base64.encode(digest, Base64.DEFAULT)).replaceAll(System.getProperty("line.separator"), "");
            System.out.println("The resultant oauth_signature is: " +signature);
        } catch (Exception ex1) {
            ex1.printStackTrace();
            System.exit(0);
        }

        //Now we just generate an HTTP header called "Authorization" with the relevant OAuth parameters for the request:
        String oAuthParameters = "OAuth "
                + "oauth_consumer_key=\""+ URLEncoder.encode(en.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY", null), "UTF-8") + "\""
                + ", oauth_nonce=\""+ URLEncoder.encode(String.valueOf(sb.toString()), "UTF-8") + "\""
                + ", oauth_signature=\""+ URLEncoder.encode(signature, "UTF-8") + "\""
                + ", oauth_signature_method=\""+ URLEncoder.encode("HMAC-SHA1", "UTF-8") + "\""
                + ", oauth_timestamp=\""+ URLEncoder.encode(Long.toString(timeStamp), "UTF-8") + "\""
                + ", oauth_token=\""+ URLEncoder.encode(en.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN", null), "UTF-8") + "\""
                + ", oauth_version=\"" + URLEncoder.encode("1.0", "UTF-8")+ "\"";
        System.out.println("oAuth Parameters are " +oAuthParameters);

        //When Twitter.com receives our request, it will respond with an oauth_token, oauth_token_secret
        //SO LET ME SEE IF I AM LUCKY!!!
        URL url;
        if(data != null)
            url = new URL(baseURL + data);
        else
            url = new URL(baseURL);

        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            //conn.setDoInput(true);
            conn.addRequestProperty("Content-type", "application/x-www-form-urlencoded");
            conn.addRequestProperty("Accept-encoding", "application/json");
            conn.addRequestProperty("User-agent", "tweetMeFit' HTTP Client");
            conn.addRequestProperty("Connection", "Keep-Alive");
            //conn.addRequestProperty("Expect", "100-Continue");
            conn.addRequestProperty("Host", "api.twitter.com");
            //Set the oAuth params in header
            conn.addRequestProperty("Authorization", oAuthParameters);



            conn.connect();

            int msg = conn.getResponseCode();
            Log.e("ResCodeHome", Integer.toString(msg));



            // Get Response
            InputStream is = conn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = rd.readLine()) != null) {
                dataBack.append(line);
                dataBack.append('\r');
            }
            rd.close();
        } catch (Exception e) {
            System.err.println("And blasted!");
            e.printStackTrace();
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
         return dataBack.toString();


    }

*/

    public String getHome2(String baseURL, String data, SharedPreferences prefs, Context context) throws UnsupportedEncodingException {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 32) {
            sb.append(Integer.toHexString(random.nextInt()));
        }

        long timeStamp = System.currentTimeMillis() / 1000;
        String signatureBaseString = null;
        if (data != null) {
            signatureBaseString = "GET&" +
                    URLEncoder.encode(baseURL, "UTF-8") + "&" + URLEncoder.encode(data, "UTF-8") +
                    "%26oauth_consumer_key%3D" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY"), null)), "UTF-8") +
                    "%26oauth_nonce%3D" + URLEncoder.encode(sb.toString(), "UTF-8") +
                    "%26oauth_signature_method%3D" + URLEncoder.encode("HMAC-SHA1", "UTF-8") +
                    "%26oauth_timestamp%3D" + URLEncoder.encode(Long.toString(timeStamp), "UTF-8") +
                    "%26oauth_token%3D" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN"), null)), "UTF-8") +
                    "%26oauth_version%3D" + URLEncoder.encode("1.0", "UTF-8") +
                    "%26tweet_mode%3Dextended";
        } else {
            signatureBaseString = "GET&" +
                    URLEncoder.encode(baseURL, "UTF-8") + "&" +
                    "oauth_consumer_key%3D" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY"), null)), "UTF-8") +
                    "%26oauth_nonce%3D" + URLEncoder.encode(sb.toString(), "UTF-8") +
                    "%26oauth_signature_method%3D" + URLEncoder.encode("HMAC-SHA1", "UTF-8") +
                    "%26oauth_timestamp%3D" + URLEncoder.encode(Long.toString(timeStamp), "UTF-8") +
                    "%26oauth_token%3D" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN"), null)), "UTF-8") +
                    "%26oauth_version%3D" + URLEncoder.encode("1.0", "UTF-8") +
                    "%26tweet_mode%3Dextended";
        }


        System.out.println("Signature Base: " + signatureBaseString);

        //Our signing key is (notice the dangling ampersand at the end):
        String signingKey = encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_SECRET"), null)) + "&"
                + encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN_SECRET"), null));
        String signature = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(signingKey.getBytes(),
                    "HmacSHA1");
            mac.init(secret);
            byte[] digest = mac.doFinal(signatureBaseString.getBytes());
            //We then use the composite signing key to create an oauth_signature from the signature base string
            signature = new String(Base64.encode(digest, Base64.DEFAULT)).replaceAll(System.getProperty("line.separator"), "");
            System.out.println("The resultant oauth_signature is: " + signature);
        } catch (Exception ex1) {
            ex1.printStackTrace();
            System.exit(0);
        }

        //Now we just generate an HTTP header called "Authorization" with the relevant OAuth parameters for the request:
        final String oAuthParameters = "OAuth "
                + "oauth_consumer_key=\"" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY"), null)), "UTF-8") + "\""
                + ", oauth_nonce=\"" + URLEncoder.encode(String.valueOf(sb.toString()), "UTF-8") + "\""
                + ", oauth_signature=\"" + URLEncoder.encode(signature, "UTF-8") + "\""
                + ", oauth_signature_method=\"" + URLEncoder.encode("HMAC-SHA1", "UTF-8") + "\""
                + ", oauth_timestamp=\"" + URLEncoder.encode(Long.toString(timeStamp), "UTF-8") + "\""
                + ", oauth_token=\"" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN"), null)), "UTF-8") + "\""
                + ", oauth_version=\"" + URLEncoder.encode("1.0", "UTF-8") + "\"";
        Log.e("Val1", oAuthParameters);
        Log.e("Val1", signatureBaseString);
        return oAuthParameters;

    }
}
