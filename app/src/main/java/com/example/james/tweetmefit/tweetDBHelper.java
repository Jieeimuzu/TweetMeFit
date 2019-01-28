package com.example.james.tweetmefit;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//http://demonuts.com/sqlite-android/

public class tweetDBHelper extends SQLiteOpenHelper {
    public static String DATABASE_NAME = "tweetsDB";
    private static final int DATABASE_VERSION = 1;
    private String TABLE_TWEETS = "tweets";
    private String TABLE_USER = "user";
    private static final String KEY_ID = "id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_TAG = "tag";
    private static final String KEY_TWEET = "Tweet";
    private static final String KEY_POST_DATE = "postDate";
    private static final String KEY_IMG_URL = "imageURL";
    private static final String KEY_TWEET_IMG = "tweetImg";
    private String TABLE = null;


    private final String CREATE_TWEET_TABLE = "CREATE TABLE "
            + TABLE_TWEETS + "(" + KEY_ID
            + " INTEGER PRIMARY KEY ,"
            + KEY_USERNAME + " TEXT,"
            + KEY_TAG + " TEXT,"
            + KEY_TWEET + " TEXT,"
            + KEY_POST_DATE + " TEXT,"
            + KEY_IMG_URL + " TEXT,"
            + KEY_TWEET_IMG + " TEXT);";

    private final String CREATE_USER_TABLE = "CREATE TABLE "
            + TABLE_USER + "(" + KEY_ID
            + " INTEGER PRIMARY KEY ,"
            + KEY_USERNAME + " TEXT,"
            + KEY_TAG + " TEXT,"
            + KEY_TWEET + " TEXT,"
            + KEY_POST_DATE + " TEXT,"
            + KEY_IMG_URL + " TEXT,"
            + KEY_TWEET_IMG + " TEXT);";


    public tweetDBHelper(Context context, String table) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        TABLE = table;
        Log.e("Table", CREATE_TWEET_TABLE);
    }

    public int getCount() {
        SQLiteDatabase db = this.getWritableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE;


        Cursor c = db.rawQuery(selectQuery, null);
        return c.getCount();
    }

    public String replaceEntity(String value) {
        String[] entities = {"&lt;", "&gt;", "&amp;", "&quot;", "&apos;", "&cent;", "&pound;", "&yen;", "&euro;", "&copy;", "&reg;"};
        String[] entitiesOutput = {"<", ">", "&", "\"", "\'", "¢", "£", "¥", "€", "©", "®"};
        for (int i = 0; i < entities.length; i++) {
            if (value.contains(entities[i])) ;
            {
                value = value.replace(entities[i], entitiesOutput[i]);
            }
        }
        return value;
    }

    public void addTweet(String TweetID, String username, String tag, String tweetText, String postDate, String imageURL, String tweetIMG) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Creating content values
        ContentValues values = new ContentValues();
        if (getCount() >= 200) {
            limitSize();
        }
        Cursor c = db.rawQuery("SELECT " + KEY_ID + "  FROM " + TABLE + " WHERE " + KEY_ID + "=" + TweetID, null);
        Cursor b = db.rawQuery("SELECT EXISTS(SELECT 1 FROM " + TABLE + " WHERE " + KEY_ID + " = " + TweetID + " );", null);
        b.moveToFirst();
        replaceEntity(tweetText);

        if (b.getString(0).equals("0") == true) {
            values.put(KEY_ID, TweetID);
            values.put(KEY_USERNAME, username);
            values.put(KEY_TAG, tag);
            values.put(KEY_TWEET, tweetText);
            values.put(KEY_POST_DATE, postDate);
            if (tweetIMG == null) {
                tweetIMG = "noIMG";
            }
            values.put(KEY_TWEET_IMG, tweetIMG);
            if (imageURL == null) {
                imageURL = "noImg";
            }
            values.put(KEY_IMG_URL, imageURL);
            // insert row in students table
            long insert = db.insert(TABLE, null, values);


        }
        return;
    }


    public long getID() {
        SQLiteDatabase db = this.getReadableDatabase();
        String selectQuery = "SELECT * FROM " + TABLE
                + " WHERE " + KEY_POST_DATE + " = (SELECT " + KEY_POST_DATE + " FROM " + TABLE + " ORDER BY "
                + KEY_POST_DATE + " DESC);";
        Cursor c = db.rawQuery(selectQuery, null);
        c.moveToFirst();
        if (c.getCount() != 0) {
            return Long.parseLong(c.getString(c.getColumnIndex(KEY_ID)));
        } else {
            return 0;
        }

    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TWEET_TABLE);
        db.execSQL(CREATE_USER_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS '" + CREATE_TWEET_TABLE + "'");
        db.execSQL("DROP TABLE IF EXISTS '" + CREATE_USER_TABLE + "'");
        onCreate(db);
    }

    public void limitSize() {
        String selectQuery = "DELETE FROM " + TABLE +
                " WHERE " + KEY_POST_DATE + " NOT IN (SELECT " + KEY_POST_DATE + " FROM (SELECT " + KEY_ID
                + " FROM " + TABLE + " ORDER BY " + KEY_POST_DATE + " DESC LIMIT 42 ) foo);";
        SQLiteDatabase db = this.getReadableDatabase();
        db.rawQuery(selectQuery, null);
    }

    public String[][] getAllTweets() {
        //String tweets[][] = new String[0][];

        String selectQuery = "SELECT * FROM " + TABLE + " ORDER BY  " + KEY_ID + " DESC ";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery(selectQuery, null);

        String tweets[][] = new String[c.getCount()][7];
        if (c.moveToFirst()) {
            do {
                tweets[c.getPosition()][0] = c.getString(c.getColumnIndex(KEY_USERNAME));
                tweets[c.getPosition()][1] = c.getString(c.getColumnIndex(KEY_TWEET));
                tweets[c.getPosition()][2] = c.getString(c.getColumnIndex(KEY_POST_DATE));
                tweets[c.getPosition()][3] = c.getString(c.getColumnIndex(KEY_TWEET_IMG));
                tweets[c.getPosition()][4] = c.getString(c.getColumnIndex(KEY_IMG_URL));
                tweets[c.getPosition()][5] = c.getString(c.getColumnIndex(KEY_ID));
                tweets[c.getPosition()][6] = c.getString(c.getColumnIndex(KEY_TAG));

            } while (c.moveToNext());
        }
        return tweets;
    }


}
