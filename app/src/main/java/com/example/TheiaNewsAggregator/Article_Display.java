package com.example.TheiaNewsAggregator;
import static com.example.TheiaNewsAggregator.R.id.textToSpeechButton;
import static java.util.Locale.ENGLISH;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class Article_Display extends AppCompatActivity {

    TextToSpeech tts;
    Boolean ttsPressed = false;
    ImageButton ttsButton;
    ImageButton favorited;
    TextView contentView;
    WebView article;
    boolean ttsDone;

    Thread thread1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent receiverIntent = getIntent();
        String uri = receiverIntent.getStringExtra("uri");

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_article_display);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR){
                    tts.setLanguage(ENGLISH);
                }
            }
        });
        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onDone(String s) {
                ttsDone = true;
            }

            @Override
            public void onError(String s) {

            }

            @Override
            public void onStart(String s) {
                ttsDone = false;
            }
        });

        article = findViewById(R.id.WebView);
        article.loadUrl(uri);

        favorited = findViewById(R.id.favoriteButton);
        SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();

        String jsonString = prefs.getString("Playlists", null);
        List<String> playlists;
        if (jsonString != null) {
            playlists = gson.fromJson(jsonString, new TypeToken<List<String>>() {}.getType());
        } else {
            playlists = new ArrayList<>();
        }

        favorited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playlists.isEmpty()) {
                    Toast.makeText(view.getContext(), "No playlists available", Toast.LENGTH_SHORT).show();
                    return;
                }

                final int[] selectedIndex = {-1};
                String[] playlistArray = playlists.toArray(new String[0]);

                new AlertDialog.Builder(view.getContext())
                        .setTitle("Save to Playlist")
                        .setSingleChoiceItems(playlistArray, -1, (dialog, which) -> {
                            selectedIndex[0] = which;
                        })
                        .setPositiveButton("Save", (dialog, which) -> {
                            if (selectedIndex[0] == -1) {
                                Toast.makeText(view.getContext(), "Please select a Playlist", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            String selectedPlaylist = playlistArray[selectedIndex[0]];
                            Log.i("SELPLAYLIST", selectedPlaylist);
                            String newJSON = prefs.getString(selectedPlaylist, null);
                            List<String> pl;
                            if(newJSON != null){
                                pl = gson.fromJson(newJSON, new TypeToken<List<String>>() {}.getType());
                            }else{
                                pl = new ArrayList<>();
                            }

                            pl.add(uri);
                            prefs.edit().putString(selectedPlaylist, gson.toJson(pl)).apply();
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });
        contentView = (TextView) findViewById(R.id.contentView);
        class MyJavaScriptInterface {
            public MyJavaScriptInterface(TextView aContentView) {
                contentView = aContentView;
            }

            @JavascriptInterface
            public void processContent(String aContent) {
                final String content = aContent;
                contentView.post(new Runnable() {
                    public void run() {
                        contentView.setText(content);
                    }
                });
            }
        }
        article.getSettings().setDomStorageEnabled(true);
        article.getSettings().setJavaScriptEnabled(true);
        article.addJavascriptInterface(new MyJavaScriptInterface(contentView), "INTERFACE");
        article.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url)
            {
                view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].innerText);");
            }
        });
        ttsButton = findViewById(textToSpeechButton);
        ttsButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (ttsPressed == false) {
                    ttsButton.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
                    ttsPressed = true;
                    String text = contentView.getText().toString();
                    thread1 = new Thread(new runTTS(text, tts));
                    thread1.start();

                }
                else {
                    ttsButton.setImageResource(android.R.drawable.ic_lock_silent_mode);
                    ttsPressed = false;
                    thread1.interrupt();
                    tts.stop();
                }
            }
        });
    }
}