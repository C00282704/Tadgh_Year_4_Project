package com.example.TheiaNewsAggregator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.widget.EditText;
import android.widget.AlertDialog;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import android.webkit.URLUtil;
import java.net.MalformedURLException;
import java.net.URL;

public class ChangeRSSFeeds extends AppCompatActivity {

   List<String> names = new ArrayList<>();
    // List<Bitmap> images = new ArrayList<>();
    // List<String> links = new ArrayList<>();
    List<String> prefLinks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_change_rssfeeds);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        //Get the amount of rows
        //Get the name for each Site for the CheckBox Names
        //Get for CBS, add on click that adds them to array
        //Add Button apply, that saves chosen rss feed links to preferences for Main to pull

        Button applyButton = findViewById(R.id.applyButton);
        applyButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
                Gson gson = new Gson();
                String json = gson.toJson(prefLinks);
                prefs.edit().putString("feedUrls", json).apply();


                Intent senderIntent = new Intent(ChangeRSSFeeds.this, Settings.class);
                startActivity(senderIntent);
            }
        });

        new Thread(() -> {
            SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
            Gson gson = new Gson();
            String jsonString = prefs.getString("RSSFeeds", null);
            List<String> urls = gson.fromJson(jsonString, new TypeToken<List<String>>() {}.getType());

            if(urls.isEmpty() || urls == null){
                //make RTE
                List<String> list = new ArrayList<>();
                list.add("https://www.rte.ie/feeds/rss/?index=/news/");
                String json = gson.toJson(list);
                prefs.edit().putString("feedUrls", json).apply();
                urls = new ArrayList<>();
                urls.add("https://www.rte.ie/feeds/rss/?index=/news/");
            }
            /////////////////////////////////////////////////////
            //Create check boxes from below using urls
            /////////////////////////////////////////////////////

            ImageButton addButton = findViewById(R.id.addFeedButton);
            addButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    EditText input = new EditText(v.getContext());
                    input.setHint("Enter NEW Feed");
                    new AlertDialog.Builder(v.getContext())
                        .setTitle("New Feed").setView(input)
                        .setPositiveButton("Add", (dialog, which) -> {
                                String userText = input.getText().toString();
                                if (!userText.isEmpty() && isValidUrlSyntax(userText)) {
                                    SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
                                    Gson gson = new Gson();

                                    String jsonString = prefs.getString("RSSFeeds", null);
                                    List<String> feeds;
                                    if (jsonString != null) {
                                        feeds = gson.fromJson(jsonString, new TypeToken<List<String>>() {}.getType());
                                    } else {
                                        feeds = new ArrayList<String>();
                                    }
                                    feeds.add(new String(userText));
                                    prefs.edit().putString("RSSFeeds", gson.toJson(feeds)).apply();
                                    finish();
                                    Intent intent = new Intent(ChangeRSSFeeds.this, ChangeRSSFeeds.class);
                                    startActivity(intent);
                                }else{
                                    new AlertDialog.Builder(v.getContext())
                                    .setTitle("Invalid RSS Feed URL")
                                    .setMessage("The URL you entered isn't a valid web address.")
                                    .setPositiveButton("OK", null)
                                    .show();
                                }
                            })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
                }
            });

            if (urls != null) {
                runOnUiThread(() -> {
                    LinearLayout mainContainer = findViewById(R.id.LLMain);
                    for (int i = 0; i < urls.size(); i++) {
                        Log.i("LOG", "2-Printing");
                        //Create button
                        LinearLayout newLayout = new LinearLayout(ChangeRSSFeeds.this);
                        CheckBox newCB = new CheckBox(ChangeRSSFeeds.this);
                        newCB.setText(urls.get(i));
                        int index = i;
                        newCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    prefLinks.add(urls.get(index));
                                } else {
                                    prefLinks.remove(urls.get(index));
                                }
                            }
                        });


                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(205,350);
                        newCB.setLayoutParams(lp);

                        newLayout.addView(newCB);
                        mainContainer.addView(newLayout);
                    }
                });
            }}).start();
    }

    public boolean isValidUrlSyntax(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            return false;
        }
        if (!URLUtil.isValidUrl(urlString)) {
            return false;
        }
        try {
            new URL(urlString); // throws if truly malformed
            return urlString.startsWith("http://") || urlString.startsWith("https://");
        } catch (MalformedURLException e) {
            return false;
        }
    }

}