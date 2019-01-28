package com.example.james.tweetmefit;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Random;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import static android.content.ContentValues.TAG;
import static java.net.URLEncoder.encode;

public class twitterAuth extends Activity {

    String json;
    SharedPreferences.Editor edit;
    private encryptions en = new encryptions();

    public twitterAuth() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public String requestToken(String baseURL, String[] oauthParams, SharedPreferences prefs) throws IOException {
        StringBuffer dataBack = new StringBuffer();
        String signatureBaseString = "POST&" +
                URLEncoder.encode(baseURL, "UTF-8") +
                "&" + "oauth_callback%3D" + URLEncoder.encode(URLEncoder.encode("tweetmefit://callback", "UTF-8")) +
                "%26oauth_consumer_key%3D" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY"), "null")), "UTF-8") +
                "%26oauth_nonce%3D" + URLEncoder.encode(oauthParams[1], "UTF-8") +
                "%26oauth_signature_method%3D" + URLEncoder.encode("HMAC-SHA1", "UTF-8") +
                "%26oauth_timestamp%3D" + URLEncoder.encode(oauthParams[0], "UTF-8") +
                "%26oauth_version%3D" + URLEncoder.encode("1.0", "UTF-8");

        System.out.println("Signature Base: " + signatureBaseString);

        //Our signing key is (notice the dangling ampersand at the end):
        String signingKey = encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_SECRET"), "null")) + "&";
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
        String oAuthParameters = "OAuth " +
                "oauth_callback=\"" + URLEncoder.encode("tweetmefit://callback", "UTF-8") + "\""
                + ", oauth_consumer_key=\"" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY"), "null")), "UTF-8") + "\""
                + ", oauth_nonce=\"" + URLEncoder.encode(String.valueOf(oauthParams[1]), "UTF-8") + "\""
                + ", oauth_signature=\"" + URLEncoder.encode(signature, "UTF-8") + "\""
                + ", oauth_signature_method=\"" + URLEncoder.encode("HMAC-SHA1", "UTF-8") + "\""
                + ", oauth_timestamp=\"" + URLEncoder.encode(oauthParams[0], "UTF-8") + "\""
                + ", oauth_version=\"" + URLEncoder.encode("1.0", "UTF-8") + "\"";
        System.out.println("oAuth Parameters are " + oAuthParameters);

        //When Twitter.com receives our request, it will respond with an oauth_token, oauth_token_secret
        //SO LET ME SEE IF I AM LUCKY!!!
        URL url = new URL(baseURL);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            //Set the oAuth params in header
            conn.addRequestProperty("Authorization", oAuthParameters);
            //Shall I set something in body?
            String bodyParams =
                    "oauth_callback=" + URLEncoder.encode("tweetmefit://callback", "UTF-8")
                            + "&oauth_consumer_key=" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY"), "null")), "UTF-8")
                            + "&oauth_nonce=" + URLEncoder.encode(oauthParams[1], "UTF-8")
                            + "&oauth_signature=" + URLEncoder.encode(signature, "UTF-8")
                            + "&oauth_signature_method=" + URLEncoder.encode("HMAC-SHA1", "UTF-8")
                            + "&oauth_timestamp=" + URLEncoder.encode(oauthParams[0], "UTF-8")
                            + "&oauth_version=" + URLEncoder.encode("1.0", "UTF-8");
            System.out.println("Parameters for body are " + oAuthParameters);
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            writer.write(bodyParams);
            writer.flush();
            conn.connect();

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

        Log.e("reqToken", "True");

        return getAuth(dataBack.toString(), prefs);
    }


    public Boolean verifyUser(SharedPreferences prefs) throws IOException {


        int msg = 404;
        try {
            Random random = new Random();
            StringBuilder sb = new StringBuilder();
            while (sb.length() < 32) {
                sb.append(Integer.toHexString(random.nextInt()));
            }
            String baseURL = "https://api.twitter.com/1.1/account/verify_credentials.json";
            long timeStamp = System.currentTimeMillis() / 1000;
            String signatureBaseString = null;
            StringBuffer dataBack = new StringBuffer();

            signatureBaseString = "GET&" +
                    URLEncoder.encode(baseURL, "UTF-8") + "&" +
                    "oauth_consumer_key%3D" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY"), "null")), "UTF-8") +
                    "%26oauth_nonce%3D" + URLEncoder.encode(sb.toString(), "UTF-8") +
                    "%26oauth_signature_method%3D" + URLEncoder.encode("HMAC-SHA1", "UTF-8") +
                    "%26oauth_timestamp%3D" + URLEncoder.encode(Long.toString(timeStamp), "UTF-8") +
                    "%26oauth_token%3D" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN"), "null")), "UTF-8") +
                    "%26oauth_version%3D" + URLEncoder.encode("1.0", "UTF-8");

            System.out.println("Signature Base: " + signatureBaseString);

            //Our signing key is (notice the dangling ampersand at the end):
            String signingKey = encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_SECRET"), "null")) + "&"
                    + encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN_SECRET"), "null"));
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
            String oAuthParameters = "OAuth "
                    + "oauth_consumer_key=\"" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY"), "null")), "UTF-8") + "\""
                    + ", oauth_nonce=\"" + URLEncoder.encode(String.valueOf(sb.toString()), "UTF-8") + "\""
                    + ", oauth_signature=\"" + URLEncoder.encode(signature, "UTF-8") + "\""
                    + ", oauth_signature_method=\"" + URLEncoder.encode("HMAC-SHA1", "UTF-8") + "\""
                    + ", oauth_timestamp=\"" + URLEncoder.encode(Long.toString(timeStamp), "UTF-8") + "\""
                    + ", oauth_token=\"" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN"), "null")), "UTF-8") + "\""
                    + ", oauth_version=\"" + URLEncoder.encode("1.0", "UTF-8") + "\"";
            System.out.println("oAuth Parameters are " + oAuthParameters);
            Log.e("Val1", oAuthParameters);
            Log.e("Val1", signatureBaseString);

            //When Twitter.com receives our request, it will respond with an oauth_token, oauth_token_secret
            //SO LET ME SEE IF I AM LUCKY!!!
            URL url;
            url = new URL(baseURL);

            HttpURLConnection conn = null;
            try {
                conn = (HttpURLConnection) url.openConnection();

                conn.setRequestMethod("GET");
                //conn.setDoInput(true);
                conn.addRequestProperty("Content-type", "application/x-www-form-urlencoded");
                conn.addRequestProperty("User-agent", "tweetMeFit' HTTP Client");
                conn.addRequestProperty("Connection", "Keep-Alive");
                conn.addRequestProperty("Expect", "100-Continue");
                conn.addRequestProperty("Host", "api.twitter.com");
                //Set the oAuth params in header
                conn.addRequestProperty("Authorization", oAuthParameters);


                conn.connect();

                msg = conn.getResponseCode();

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
            Log.e("Msg returned From", Integer.toString(msg));
        } catch (NullPointerException e) {
            Log.e("Error", e.toString());
        }
        return msg == 200;


    }


    public String getAuth(String js, SharedPreferences Prefs) throws IOException {
        String oauth_token = null;
        String oauth_token_secret = null;
        if (!Prefs.contains(en.encrypt("ACCESS_TOKEN"))) {
            oauth_token = js.substring(js.indexOf("oauth_token=") + 12, js.indexOf("&oauth_token_secret"));
            oauth_token_secret = js.substring(js.indexOf("auth_token_secret") + 18, js.indexOf("&oauth_callback_confirmed"));
        }
        Log.e(TAG, oauth_token + " : " + oauth_token_secret);
        edit = Prefs.edit();
        edit.putString(en.encrypt("ACCESS_TOKEN"), en.encrypt(oauth_token));
        edit.putString(en.encrypt("ACCESS_TOKEN_SECRET"), en.encrypt(oauth_token_secret));
        edit.apply();

        URL u = new URL("https://api.twitter.com/oauth/authorize?oauth_token=" + oauth_token);
        HttpURLConnection conn = (HttpURLConnection) u.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Host", " api.twitter.com");
        conn.setRequestProperty("User-Agent", " tweetMeFit' HTTP Client");
        //conn.setDoOutput(true);
        conn.connect();

        int status = conn.getResponseCode();
        String msg2 = conn.getResponseMessage();
        // switch statement to catch HTTP 200 and 201 errors
        switch (status) {
            case 200:
            case 201:
                // live connection to your REST service is established here using getInputStream() method
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                // create a new string builder to store json data returned from the REST service
                StringBuilder sb = new StringBuilder();
                String line;

                // loop through returned data line by line and append to stringbuilder 'sb' variable
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();

                // remember, you are storing the json as a stringy
                try {
                    json = sb.toString();
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing data " + e.toString());
                }
                // return JSON String containing data to Tweet activity (or whatever your activity is called!)


                return json;
        }
        // HTTP 200 and 201 error handling from switch statement


        return null;
    }


    public String accessToken(String baseURL, SharedPreferences prefs) throws IOException {


        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 32) {
            sb.append(Integer.toHexString(random.nextInt()));
        }

        long timeStamp = System.currentTimeMillis() / 1000;

        StringBuffer dataBack = new StringBuffer();
        String signatureBaseString = "POST&" +
                URLEncoder.encode(baseURL, "UTF-8") +
                "&" + "%26oauth_verifier%3D" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("Oauth_verifier"), "null")), "UTF-8") +
                "%26oauth_consumer_key%3D" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY"), "null")), "UTF-8") +
                "%26oauth_nonce%3D" + URLEncoder.encode(sb.toString(), "UTF-8") +
                "%26oauth_signature_method%3D" + URLEncoder.encode("HMAC-SHA1", "UTF-8") +
                "%26oauth_timestamp%3D" + URLEncoder.encode(Long.toString(timeStamp), "UTF-8") +
                "%26oauth_token%3D" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN"), "null")), "UTF-8") +
                "%26oauth_version%3D" + URLEncoder.encode("1.0", "UTF-8");

        System.out.println("Signature Base: " + signatureBaseString);

        //Our signing key is (notice the dangling ampersand at the end):
        String signingKey = encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_SECRET"), "null")) + "&";
        String signature = null;
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            SecretKeySpec secret = new SecretKeySpec(signingKey.getBytes(),
                    "HmacSHA1");
            mac.init(secret);
            byte[] digest = mac.doFinal(signatureBaseString.getBytes());
            //We then use the composite signing key to create an oauth_signature from the signature base string
            signature = new String(Base64.encode(digest, Base64.NO_WRAP));
            System.out.println("The resultant oauth_signature is: " + signature);
        } catch (Exception ex1) {
            ex1.printStackTrace();
            System.exit(0);
        }

        //Now we just generate an HTTP header called "Authorization" with the relevant OAuth parameters for the request:
        String oAuthParameters = "OAuth "
                + "oauth_consumer_key=\"" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY"), "null")), "UTF-8") + "\""
                + ", oauth_nonce=\"" + URLEncoder.encode(sb.toString(), "UTF-8") + "\""
                + ", oauth_signature=\"" + URLEncoder.encode(signature, "UTF-8") + "\""
                + ", oauth_signature_method=\"" + URLEncoder.encode("HMAC-SHA1", "UTF-8") + "\""
                + ", oauth_timestamp=\"" + URLEncoder.encode(Long.toString(timeStamp), "UTF-8") + "\""
                + ", oauth_token=\"" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN"), "null")), "UTF-8") + "\""
                + ", oauth_version=\"" + URLEncoder.encode("1.0", "UTF-8") + "\""
                + ", oauth_verifier=\"" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("Oauth_verifier"), "null")), "UTF-8") + "\"";
        System.out.println("oAuth Parameters are " + oAuthParameters);

        //When Twitter.com receives our request, it will respond with an oauth_token, oauth_token_secret
        //SO LET ME SEE IF I AM LUCKY!!!
        URL url = new URL(baseURL);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            //Set the oAuth params in header
            conn.addRequestProperty("Authorization", oAuthParameters);
            conn.setRequestProperty("Host", " api.twitter.com");
            conn.setRequestProperty("User-Agent", " tweetMeFit' HTTP Client");

            //Shall I set something in body?
            String bodyParams = "oauth_verifier=" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("Oauth_verifier"), "null")), "UTF-8")
                    + "oauth_consumer_key=" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("CONSUMER_KEY"), "UTF-8")), "UTF-8")
                    + "&oauth_nonce=" + URLEncoder.encode(sb.toString(), "UTF-8")
                    + "&oauth_signature=" + URLEncoder.encode(signature, "UTF-8")
                    + "&oauth_signature_method=" + URLEncoder.encode("HMAC-SHA1", "UTF-8")
                    + "&oauth_timestamp=" + URLEncoder.encode(Long.toString(timeStamp), "UTF-8")
                    + "&oauth_token=" + URLEncoder.encode(encryptions.decrypt(prefs.getString(en.encrypt("ACCESS_TOKEN"), "null")), "UTF-8")
                    + "&oauth_version=" + URLEncoder.encode("1.0", "UTF-8");
            System.out.println("Parameters for body are " + oAuthParameters);

            //OutputStream os = conn.getOutputStream();
            //OutputStreamWriter writer = new OutputStreamWriter(os, "UTF-8");

            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            writer.write(bodyParams);
            writer.flush();

            //this.sendData(conn, bodyParams);
            conn.connect();
            int msg = conn.getResponseCode();
            // Get Response
            InputStream is = conn.getInputStream();
            BufferedReader rd = new BufferedReader(new InputStreamReader(is));
            String line;

            while ((line = rd.readLine()) != null) {
                dataBack.append(line);
                dataBack.append('\r');
            }
            String data = dataBack.toString();
            SharedPreferences.Editor edit = prefs.edit();

            String oauth_token = data.substring(data.indexOf("oauth_token=") + 12, data.indexOf("&oauth_token_secret"));
            String oauth_token_secret = data.substring(data.indexOf("oauth_token_secret=") + 19, data.indexOf("&user_id="));

            edit.putString(en.encrypt("ACCESS_TOKEN"), en.encrypt(oauth_token));
            edit.putString(en.encrypt("ACCESS_TOKEN_SECRET"), en.encrypt(oauth_token_secret));
            edit.apply();
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

}
