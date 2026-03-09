package com.example.TheiaNewsAggregator;

import static android.content.res.Configuration.UI_MODE_NIGHT_MASK;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;


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

        Button mainBtn = findViewById(R.id.MainBtn);
        mainBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                scrollPlaylist.setVisibility(View.GONE);
                scrollMain.setVisibility(View.VISIBLE);
            }
        });

        Button playListBtn = findViewById(R.id.PlaylistBtn);
        playListBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                scrollMain.setVisibility(View.GONE);
                scrollPlaylist.setVisibility(View.VISIBLE);
            }
        });
        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton settingsButton = findViewById(R.id.settingsBtn);
        settingsButton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
                startActivity(new Intent(MainActivity.this, Settings.class));
            }
        });

        new Thread(() -> {
            try {
                //These pull all of the Links, titles and thumbnails in the given RSS Feed.
                String sourceLinks = getLinks("https://feeds.bbci.co.uk/news/rss.xml?edition=uk");
                String titles = getTitles("https://feeds.bbci.co.uk/news/rss.xml?edition=uk");
                LinkedList<Bitmap> imageList = getImages("https://feeds.bbci.co.uk/news/rss.xml?edition=uk");

                LinkedList<String> list = new LinkedList<>();
                LinkedList<String> titleList = new LinkedList<>();
                if (sourceLinks != null) {
                    Collections.addAll(list, sourceLinks.split("\\R"));
                    Collections.addAll(titleList, titles.split("\\R"));
                }
                int uiTheme = UI_MODE_NIGHT_MASK;


                runOnUiThread(() -> {
                    LinearLayout mainContainer = findViewById(R.id.LL1);
                    LinearLayout playContainer = findViewById(R.id.LL2);
                    for (int i = 2; i < list.size(); i++) {
                        String link = list.get(i);
                        LinearLayout listMain = new LinearLayout(MainActivity.this);
                        listMain.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));

                        //Creates Title and Thumbnail for each article
                        TextView tv1 = new TextView(MainActivity.this);
                        LinearLayout.LayoutParams lp =
                                new LinearLayout.LayoutParams(
                                        205,   // width in pixels
                                        LinearLayout.LayoutParams.MATCH_PARENT
                                );
                        tv1.setLayoutParams(lp);
                        tv1.setText(titleList.get(i));
                        tv1.setLayoutParams(lp);
                        ImageView iv1 = new ImageView(MainActivity.this);
                        iv1.setImageBitmap(imageList.get(i-2));
                        iv1.setLayoutParams(lp);

                        listMain.setBackgroundColor(uiTheme);
                        listMain.setOnClickListener(new View.OnClickListener(){
                            public void onClick(View v){
                                Intent senderIntent = new Intent(MainActivity.this, Article_Display.class);
                                senderIntent.putExtra("uri", link);
                                finish();
                                startActivity(senderIntent);
                            }
                        });
                        listMain.addView(tv1);
                        listMain.addView(iv1);
                        mainContainer.addView(listMain);
                    }
                    //Site Playlists
                    for (int i = 0; i <= 2; i++) {
                        Log.i("LOG", "Hello");
                        LinearLayout listPlay = new LinearLayout(MainActivity.this);
                        listPlay.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));

                        //Creates Title and Thumbnail for each Playlist
                        ImageView folder = new ImageView(MainActivity.this);
                        folder.setImageDrawable(Drawable.createFromPath("@android:drawable/file.png"));
                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(205, LinearLayout.LayoutParams.MATCH_PARENT);
                        folder.setLayoutParams(lp);

                        TextView playListName = new TextView(MainActivity.this);
                        playListName.setText("Temp");
                        listPlay.addView(folder);
                        listPlay.addView(playListName);
                        listPlay.setOnClickListener(new View.OnClickListener(){
                            public void onClick(View v){
//                                Intent senderIntent = new Intent(MainActivity.this, Article_Display.class);
//                                senderIntent.putExtra("uri", link);
//                                finish();
//                                startActivity(senderIntent);
                            }
                        });

                        playContainer.addView(listPlay);
                    }
                });

            } catch (IOException e) {
                Log.e("IOException:", Objects.requireNonNull(e.getMessage()));
            }
        }).start();
    }
    public static String getLinks(String urlAddress) throws IOException{
        try{
            URL rssUrl = new URL(urlAddress);

            BufferedReader in =new BufferedReader(new InputStreamReader(rssUrl.openStream()));

            String line = in.readLine();
            StringBuilder sCode = new StringBuilder();

            if (line != null) {
                sCode.append(line);
            }
            while((line = in.readLine())!= null){
                if(line.contains("<link>")){
                    int firstPos = line.indexOf("<link>");
                    String temp = line.substring(firstPos);
                    temp = temp.replace("<link>","");
                    int lastPos = temp.indexOf("</link>");
                    temp = temp.substring(0,lastPos);
                    sCode.append(temp).append("\n");
                }
            }

            in.close();

            return sCode.toString();
        } catch (MalformedURLException mue){
            System.out.println("Malformed URL");
        } catch (IOException ioe){
            System.out.println("Something went wrong reading the contents");
        }
        return null;
    }
    public static String getTitles(String urlAddress) throws IOException{
        try{
            URL rssUrl = new URL(urlAddress);

            BufferedReader in =new BufferedReader(new InputStreamReader(rssUrl.openStream()));

            String line = in.readLine();
            StringBuilder sCode = new StringBuilder();

            if (line != null) {
                sCode.append(line);
            }
            while((line = in.readLine())!= null){
                if(line.contains("<title>")){
                    int firstPos = line.indexOf("<title>");
                    String temp = line.substring(firstPos);
                    temp = getString(temp);
                    if(temp.contains("<![CDATA[")){
                        temp = temp.replace("<![CDATA[","");
                        temp = temp.replace("]]>","");
                    }
                    int lastPos = temp.indexOf("</title>");
                    temp = temp.substring(0,lastPos);
                    sCode.append(temp).append("\n");
                }
            }

            in.close();

            return sCode.toString();
        } catch (MalformedURLException mue){
            System.out.println("Malformed URL");
        } catch (IOException ioe){
            System.out.println("Something went wrong reading the contents");
        }
        return null;
    }

    @NonNull
    private static String getString(String temp) {
        temp = temp.replace("<title>","");
        return temp;
    }

    public static LinkedList<Bitmap> getImages(String urlAddress) throws IOException{
        try{
            URL rssUrl = new URL(urlAddress);

            BufferedReader in =new BufferedReader(new InputStreamReader(rssUrl.openStream()));

            String line = in.readLine();
            StringBuilder sCode = new StringBuilder();
            LinkedList<String> images = new LinkedList<>();

            if (line != null) {
                sCode.append(line);
            }
            while((line = in.readLine())!= null){
                if(line.contains("<media:thumbnail")){
                    line = line.substring(line.indexOf("url=\""), line.indexOf("\"/>"));
                    String temp = line.replace("url=\"", "");
                    images.add(temp);
                }
            }
            LinkedList<Bitmap> bitmaps = new LinkedList<>();
            for(int i = 0; i < images.size(); i++) {
                URL url = new URL(images.get(i));
                try {
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    bitmaps.add(myBitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("Exception", e.getMessage());
                    return null;
                }
            }
            return bitmaps;
        } catch (MalformedURLException mue){
            System.out.println("Malformed URL");
        } catch (IOException ioe){
            System.out.println("Something went wrong reading the contents");
        }
        return null;
    }
//    public class ConnectToSupabase {
//        String SUPABASE_URL = "https://zganowuduwhsgxdhlxcl.supabase.co";
//        String SUPABASE_KEY = "sb_publishable_vSfZh1SKFxeBtuTdmErOSQ_hI7Ml1rU";
//        OkHttpClient client = new OkHttpClient();
//
//        public void fetchData(String table, Callback callback) {
//            String url = SUPABASE_URL + "/rest/v1/" + table + "?select=*";
//
//            Request request = new Request.Builder().url(url).build();
//
//            client.newCall(request).enqueue(callback);
//        }
//    }
}