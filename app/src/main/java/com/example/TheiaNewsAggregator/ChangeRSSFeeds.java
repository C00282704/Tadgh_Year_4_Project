package com.example.TheiaNewsAggregator;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.JsonSyntaxException;

// import org.json.JSONArray;
// import org.json.JSONException;
// import org.json.JSONObject;
// import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
// import java.io.IOException;
// import okhttp3.OkHttpClient;
// import okhttp3.Request;
// import okhttp3.Response;

import java.util.ArrayList;
import java.util.List;

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
        applyButton.setEnabled(false);
        applyButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
                Gson gson = new Gson();
                String json = gson.toJson(prefLinks);
                prefs.edit().putString("feedUrls", json).apply();
                Log.i("FEEDS", json);


                Intent senderIntent = new Intent(ChangeRSSFeeds.this, Settings.class);
                startActivity(senderIntent);
            }
        });

        new Thread(() -> {
            SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
            Gson gson = new Gson();
            String jsonString = prefs.getString("RSSFeeds", null);
            List<String> urls = new ArrayList<>();
            try{
                urls = gson.fromJson(jsonString, new TypeToken<List<String>>() {}.getType());
            }catch(JsonSyntaxException j){
                urls = new ArrayList<>();
            }

            if(urls == null || urls.isEmpty()){
                //make RTE
                List<String> list = new ArrayList<>();
                list.add("https://www.rte.ie/feeds/rss/?index=/news/");
                String json = gson.toJson(list);
                prefs.edit().putString("RSSFeeds", json).apply();
                urls = new ArrayList<>();
                urls.add("https://www.rte.ie/feeds/rss/?index=/news/");
            }
            /////////////////////////////////////////////////////
            //Create check boxes from below using urls
            /////////////////////////////////////////////////////

            if (urls != null) {
                final List<String> finalUrls = urls;
                String newJString = prefs.getString("feedUrls", null);
                List<String> selUrls = (newJString != null)
                ? gson.fromJson(newJString, new TypeToken<List<String>>(){}.getType())
                : new ArrayList<>();

                if (newJString != null) {
                    try{
                        selUrls = gson.fromJson(newJString, new TypeToken<List<String>>() {}.getType());
                    }catch(JsonSyntaxException j){
                        selUrls = new ArrayList<>();
                    }
                } else {
                    selUrls = new ArrayList<String>();
                }
                final List<String> selectedUrls = selUrls;
                //supabaseFeeds();

                runOnUiThread(() -> {
                    ImageButton addButton = findViewById(R.id.addFeedButton);
                    addButton.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            EditText input = new EditText(v.getContext());
                            input.setHint("Enter NEW Feed");
                            Log.i("LOG", "BUTTON PRESSED");
                            new AlertDialog.Builder(v.getContext())
                                .setTitle("New Feed").setView(input)
                                .setPositiveButton("Add", (dialog, which) -> {
                                        String userText = input.getText().toString();
                                        if (!userText.isEmpty() && isValidUrlSyntax(userText)) {
                                            String jsonString = prefs.getString("RSSFeeds", null);
                                            List<String> feeds;
                                            if (jsonString != null) {
                                                try{
                                                    feeds = gson.fromJson(jsonString, new TypeToken<List<String>>() {}.getType());
                                                }catch(JsonSyntaxException j){
                                                    feeds = new ArrayList<>();
                                                }
                                            } else {
                                                feeds = new ArrayList<String>();
                                            }
                                            feeds.add(userText);
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


                    LinearLayout mainContainer = findViewById(R.id.LLMain);
                    for (int i = 0; i < finalUrls.size(); i++) {
                        Log.i("LOG", "2-Printing");
                        //Create button
                        LinearLayout newLayout = new LinearLayout(ChangeRSSFeeds.this);
                        CheckBox newCB = new CheckBox(ChangeRSSFeeds.this);
                        newCB.setText(finalUrls.get(i));
                        int index = i;
                        newCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                if (isChecked) {
                                    prefLinks.add(finalUrls.get(index));
                                } else {
                                    prefLinks.remove(finalUrls.get(index));
                                }
                            }
                        });
                        for(int i2 = 0; i2 < selectedUrls.size(); i2++){
                            if(selectedUrls.get(i2).equals(finalUrls.get(i))){
                                newCB.setChecked(true);
                                break;
                            }
                        }

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(205,350);
                        newCB.setLayoutParams(lp);

                        newLayout.addView(newCB);
                        mainContainer.addView(newLayout);
                    }
                applyButton.setEnabled(true);
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
    // public void supabaseFeeds(){
        
    //         String url = "https://zganowuduwhsgxdhlxcl.supabase.co/rest/v1/RSSFeeds";
    //         String key = "sb_publishable_vSfZh1SKFxeBtuTdmErOSQ_hI7Ml1rU";
    //         OkHttpClient client = new OkHttpClient();

    //         Request request = new Request.Builder().url(url).addHeader("apikey", key).addHeader("Authorization", "Bearer " + key).build();

    //         try (Response response = client.newCall(request).execute()) {
    //             String responseString = "";
    //             if (response.body() != null) {
    //                 responseString = response.body().string();
    //             }

    //             JSONArray jsonArray = new JSONArray(responseString);

    //             // Loop through each row
    //             for (int i = 0; i < jsonArray.length(); i++) {//Get the name for each Site for the CheckBox Names
    //                 JSONObject row = jsonArray.getJSONObject(i);
    //                 if (!row.getString("name").isEmpty()) {
    //                     String feedName = row.getString("name");
    //                     names.add(feedName);
    //                     String link = row.getString("link");
    //                     links.add(link);
    //                 }
    //             }

    //             for (int i = 0; i < jsonArray.length(); i++) {//Get the logo for each Site for the CheckBox Names
    //                 JSONObject row = jsonArray.getJSONObject(i);
    //                 if (!row.getString("logoName").isEmpty()) {
    //                     String logoName = row.getString("logoName");
    //                     String imageUrl = "https://zganowuduwhsgxdhlxcl.supabase.co/storage/v1/object/public/FeedImages/" + logoName;
    //                     Request imageNameRequest = new Request.Builder().url(imageUrl).addHeader("apikey", key).addHeader("Authorization", "Bearer " + key).build();

    //                     try (Response imageResponse = client.newCall(imageNameRequest).execute()) {
    //                         if (imageResponse.body() != null) {
    //                             byte[] imageBytes = imageResponse.body().bytes();
    //                             Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
    //                             images.add(bitmap);
    //                         }
    //                     } catch (Exception e) {
    //                         e.printStackTrace();
    //                     }
    //                 }
    //             }
    //         } catch (IOException e) {
    //             throw new RuntimeException(e);
    //         } catch (JSONException e) {
    //             throw new RuntimeException(e);
    //         }
    //         if (names != null) {
    //             runOnUiThread(() -> {
    //                 LinearLayout mainContainer = findViewById(R.id.LLMain);
    //                 for (int i = 0; i < names.size(); i++) {
    //                     Log.i("LOG", "2-Printing");
    //                     //Create button
    //                     LinearLayout newLayout = new LinearLayout(ChangeRSSFeeds.this);
    //                     CheckBox newCB = new CheckBox(ChangeRSSFeeds.this);
    //                     newCB.setText(names.get(i));//RSS Feed Name
    //                     String link = links.get(i);
    //                     int index = i;
    //                     newCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    //                         @Override
    //                         public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    //                             if (isChecked) {
    //                                 prefLinks.add(links.get(index));
    //                             } else {
    //                                 prefLinks.remove(links.get(index));
    //                             }
    //                         }
    //                     });

    //                     ImageView newLogo = new ImageView(ChangeRSSFeeds.this);
    //                     newLogo.setImageBitmap(images.get(i));

    //                     LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(205,350);
    //                     newCB.setLayoutParams(lp);
    //                     newLogo.setLayoutParams(lp);

    //                     newLayout.addView(newCB);
    //                     newLayout.addView(newLogo);
    //                     mainContainer.addView(newLayout);
    //                 }
    //             });
    //         }
    // }

}