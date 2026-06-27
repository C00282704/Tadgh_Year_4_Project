package com.example.TheiaNewsAggregator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ScrollView scrollMain = findViewById(R.id.MainScroll);
        ScrollView scrollPlaylist = findViewById(R.id.PlayScroll);
        scrollMain.setVisibility(View.VISIBLE);
        scrollPlaylist.setVisibility(View.GONE);

        Button playListBtn = findViewById(R.id.PlaylistBtn);
        playListBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, MainPlaylists.class));
            }
        });
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton settingsButton = findViewById(R.id.settingsBtn);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Settings.class));
            }
        });
        SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonString = prefs.getString("feedUrls", null);
        List<String> urls = gson.fromJson(jsonString, new TypeToken<List<String>>() {
        }.getType());

        if (urls == null || urls.isEmpty()) {
            List<String> list = new ArrayList<>();
            list.add("https://www.rte.ie/feeds/rss/?index=/news/");
            String json = gson.toJson(list);
            prefs.edit().putString("feedUrls", json).apply();
            urls = new ArrayList<>();
            urls.add("https://www.rte.ie/feeds/rss/?index=/news/");

        }
        List<String> finalUrls = urls;
        new Thread(() -> {
            try {
                for (int i = 0; i < finalUrls.size(); i++) {
                    //These pull all of the Links, titles and thumbnails in the given RSS Feed.
                    String url = finalUrls.get(i);
                    Log.i("URLOG", finalUrls.get(i));

                    List<RssArticle> articles = getFeedDetails(url);
                    if (articles == null || articles.isEmpty()){
                        continue;
                    }

                    runOnUiThread(() -> {
                        LinearLayout mainContainer = findViewById(R.id.LL1);
                        for (int i2 = 0; i2 < articles.size(); i2++) {
                            RssArticle article = articles.get(i2);
                            LinearLayout listMain = new LinearLayout(MainActivity.this);
                            listMain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));

                            LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(205,350);
                            TextView tv1 = new TextView(MainActivity.this);
                            tv1.setText(article.title);
                            tv1.setLayoutParams(titleLp);

                            LinearLayout.LayoutParams imageLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,350);
                            ImageView iv1 = new ImageView(MainActivity.this);
                            iv1.setImageBitmap(article.image);
                            iv1.setLayoutParams(imageLp);

                            String link = article.link;
                            Log.i("LINKS", link);
                            listMain.setOnClickListener(v -> {
                                Intent intent = new Intent(MainActivity.this, Article_Display.class);
                                intent.putExtra("uri", link);
                                startActivity(intent);
                            });

                            listMain.addView(tv1);
                            listMain.addView(iv1);
                            mainContainer.addView(listMain);
                        }
                    });

                }
            } catch (IOException e) {
                Log.e("IOException:", Objects.requireNonNull(e.getMessage()));
            }
        }).start();
    }

    public static List<RssArticle> getFeedDetails(String feedUrl) throws IOException {
        try {
            OkHttpClient client = new OkHttpClient.Builder().followRedirects(true).build();

            Request request = new Request.Builder().url(feedUrl).header("User-Agent", "RssFeedParser2/1.0").build();
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

            List<RssArticle> articles = new ArrayList<>();

            while (articleMatcher.find()) {
                String block = articleMatcher.group(1);
                RssArticle article = new RssArticle();

                article.title = findString(TITLE_PATTERN, block);
                article.link = findString(LINK_PATTERN, block);
                article.image = findImage(block);

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
            Log.i("Error", mue.toString());
        } catch (IOException ioe) {
            Log.i("Error", ioe.toString());
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