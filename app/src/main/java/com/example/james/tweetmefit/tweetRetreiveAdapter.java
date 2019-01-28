package com.example.james.tweetmefit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;


public class tweetRetreiveAdapter extends BaseAdapter {

    Context context;
    ArrayList<String> tweetID;
    ArrayList<String> Username;
    ArrayList<String> Tweet;
    ArrayList<String> postDate;
    ArrayList<String> imageUrl;
    LayoutInflater inflater;
    ArrayList<String> tweetImg;
    ArrayList<String> userTag;


    public tweetRetreiveAdapter(Context appContext, tweetDBHelper tweetDB) {
        this.context = appContext;
        int objCount = tweetDB.getCount();

        String[][] sb = tweetDB.getAllTweets();
        //Log.e("Val", sb[5][2]);

        Username = new ArrayList<String>(objCount);
        Tweet = new ArrayList<String>(objCount);
        postDate = new ArrayList<String>(objCount);
        tweetImg = new ArrayList<String>(objCount);
        imageUrl = new ArrayList<String>(objCount);
        tweetID = new ArrayList<String>(objCount);
        userTag = new ArrayList<String>(objCount);
        for (int i = 0; i < objCount; i++) {
            try {

                Username.add(i, sb[i][0]);
                Tweet.add(i, sb[i][1]);
                postDate.add(i, sb[i][2]);
                imageUrl.add(i, sb[i][3]);
                tweetImg.add(i, sb[i][4]);
                tweetID.add(i, sb[i][5]);
                userTag.add(i, sb[i][6]);

            } catch (NullPointerException e) {
                //Log.e("Crashed on item" + Integer.toString(i), e.toString());
            }
        }

        inflater = (LayoutInflater.from(appContext));
    }


    public void appendDataTweetRetAdp(Context appContext, tweetDBHelper tweetDB) {
        this.context = appContext;
        int objCount = tweetDB.getCount();
        int len = getCount();
        String[][] sb = tweetDB.getAllTweets();
        Username.ensureCapacity(len + 1);
        Tweet.ensureCapacity(len + 1);
        postDate.ensureCapacity(len + 1);
        imageUrl.ensureCapacity(len + 1);
        tweetImg.ensureCapacity(len + 1);
        tweetID.ensureCapacity(len + 1);
        for (int i = 0; i < objCount; i++) {
            try {

                Username.add(len + i, sb[i][0]);
                Tweet.add(len + i, sb[i][1]);
                postDate.add(len + i, sb[i][2]);
                imageUrl.add(i, sb[i][3]);
                tweetImg.add(i, sb[i][4]);
                tweetID.add(i, sb[i][5]);
                userTag.add(i, sb[i][6]);

            } catch (NullPointerException e) {
                //Log.e("Crashed on item" + Integer.toString(i), e.toString());
            }
        }

        inflater = (LayoutInflater.from(appContext));

    }


    @Override
    public int getCount() {
        return Username.toArray().length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @NotNull
    @Override
    public View getView(final int i, View view, ViewGroup viewGroup) {
        view = inflater.inflate(R.layout.user_list, null);
        TextView username = view.findViewById(R.id.screenName);
        TextView tweet = view.findViewById(R.id.tweetText);
        TextView date = view.findViewById(R.id.date);
        TextView id = view.findViewById(R.id.tweetID);
        TextView tag = view.findViewById(R.id.userTag);
        final ImageView profilePic = view.findViewById(R.id.ProfilePic);
        ImageView tweetImgs = view.findViewById(R.id.tweetPic);


        username.setText(Username.get(i));
        tag.setText(userTag.get(i));
        tweet.setText(Tweet.get(i));
        date.setText(postDate.get(i));
        id.setText(tweetID.get(i));
        if (tweetImg.get(i) != null) {
            Picasso.with(context).load(tweetImg.get(i)).into(tweetImgs);
        }


        final File file = new File(Environment.getDataDirectory().toString() + "/data/com.example.james.tweetmefit/profilePicture/" + Username.get(i) + ".jpg");
        if (file.exists() != true) {
            Picasso.with(context)
                    .load(imageUrl.get(i))
                    .into(new Target() {
                              @Override
                              public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                                  try {
                                      String root = Environment.getDataDirectory().toString() + "/data/com.example.james.tweetmefit";
                                      File myDir = new File(root + "/profilePicture");
                                      Log.e("---------------------", root);

                                      if (!myDir.exists()) {
                                          myDir.mkdirs();
                                      }

                                      String name = Username.get(i) + ".jpg";
                                      myDir = new File(myDir, name);
                                      FileOutputStream out = new FileOutputStream(myDir);
                                      bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);

                                      out.flush();
                                      out.close();

                                  } catch (Exception e) {
                                      // some action
                                  }
                                  Picasso.with(context)
                                          .load(file).transform(new RoundedTransformation(80, 0)).fit().into(profilePic);
                              }

                              @Override
                              public void onBitmapFailed(Drawable errorDrawable) {
                              }

                              @Override
                              public void onPrepareLoad(Drawable placeHolderDrawable) {
                              }
                          }
                    );

        } else {
            Picasso.with(context)
                    .load(file).transform(new RoundedTransformation(80, 0)).fit().into(profilePic);
        }

        return view;
    }
}