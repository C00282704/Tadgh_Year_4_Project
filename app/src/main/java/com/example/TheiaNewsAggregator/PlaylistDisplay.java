package com.example.TheiaNewsAggregator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PlaylistDisplay extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_playlist);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageButton settingsButton = findViewById(R.id.settingsBtn);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(PlaylistDisplay.this, Settings.class));
            }
        });

        Intent receiverIntent = getIntent();
        String name = receiverIntent.getStringExtra("uri");
        if(Objects.equals(name, "")){
            new AlertDialog.Builder(PlaylistDisplay.this)
                    .setTitle("Playlist Empty")
                    .setPositiveButton("Return", (dialog, which) -> {
                        finish();
                        Intent intent = new Intent(PlaylistDisplay.this, MainPlaylists.class);
                        startActivity(intent);
                    })
                    .show();
        }else{
            SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
            Gson gson = new Gson();
            String jsonString = prefs.getString(name, null);

            List<String> finalUrls = gson.fromJson(jsonString, new TypeToken<List<String>>() {}.getType());
        }
    }

    public static List<MainActivity.RssArticle> getFeedDetails(String feedUrl) throws IOException {
        try {
            OkHttpClient client = new OkHttpClient.Builder()
                    .followRedirects(true)
                    .build();

            Request request = new Request.Builder()
                    .url(feedUrl)
                    .header("User-Agent", "RssFeedParser2/1.0")
                    .build();
            String valResponse;
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    System.out.println("HTTP error: " + response.code());
                    return null;
                }
                valResponse = response.body().string();
            }

            Pattern ITEM_PATTERN = Pattern.compile("<item[^>]*>(.*?)</item>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

            Pattern TITLE_PATTERN = Pattern.compile("<title[^>]*>\\s*(?:<!\\[CDATA\\[)?(.*?)(?:]]>)?\\s*</title>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);

            Pattern LINK_PATTERN = Pattern.compile("<link[^>]*>\\s*(?:<!\\[CDATA\\[)?(.*?)(?:]]>)?\\s*</link>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);


            Matcher articleMatcher = ITEM_PATTERN.matcher(valResponse);

            List<MainActivity.RssArticle> articles = new ArrayList<>();

            while (articleMatcher.find()) {
                String block = articleMatcher.group(1);
                MainActivity.RssArticle article = new MainActivity.RssArticle();

                article.title = findString(TITLE_PATTERN, block);
                article.link = findString(LINK_PATTERN, block);
                article.image = findImage(block);

                // Clean up any stray whitespace / newlines
                if (article.title != null){
                    article.title = article.title.strip();
                }

                if (article.link != null) {
                    article.link = article.link.strip();
                }
                articles.add(article);

            }
            return articles;
        }catch (MalformedURLException mue) {
            System.out.println("Malformed URL");
        } catch (IOException ioe) {
            System.out.println("Something went wrong reading the contents");
        }
        return null;
    }

    private static String findString(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);

        if(m.find()){
            return m.group(1);
        }
        return null;
    }


    private static Bitmap findImage(String block) throws IOException {
        Pattern IMG_PATTERN_1 = Pattern.compile("<media:(?:content|thumbnail)[^>]+url=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE);

        Pattern IMG_PATTERN_2 = Pattern.compile("<enclosure[^>]+url=[\"'](.*?)[\"'][^>]+type=[\"']image/", Pattern.CASE_INSENSITIVE);

        Pattern IMG_PATTERN_3 = Pattern.compile("<img[^>]+src=[\"'](.*?)[\"']", Pattern.CASE_INSENSITIVE);

        String img = findString(IMG_PATTERN_1, block);
        if (img != null){
            URL imgUrl = new URL(img);
            return BitmapFactory.decodeStream(imgUrl.openStream());
        }

        img = findString(IMG_PATTERN_2, block);
        if (img != null){
            URL imgUrl = new URL(img);
            return BitmapFactory.decodeStream(imgUrl.openStream());
        }

        img = findString(IMG_PATTERN_3, block);
        if(img != null){
            URL imgUrl = new URL(img);
            return BitmapFactory.decodeStream(imgUrl.openStream());
        }
        return null;
    }
    public static class RssArticle {
        public String title;
        public String link;
        public Bitmap image;
    }
}