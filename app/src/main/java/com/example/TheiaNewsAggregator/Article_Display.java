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
import android.widget.EditText;
import android.widget.Button;

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
    boolean ttsPressed = false;
    ImageButton ttsButton;
    ImageButton favorited;
    TextView contentView;
    WebView article;

    Thread thread1;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        if (thread1 != null && thread1.isAlive()) {
            thread1.interrupt();
        }
    }


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
                
            }

            @Override
            public void onError(String s) {

            }

            @Override
            public void onStart(String s) {
                
            }
        });

        article = findViewById(R.id.WebView);
        article.loadUrl(uri);

        favorited = findViewById(R.id.favoriteButton);
        SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();

        String jsonString = prefs.getString("Playlists", null);
        List<Playlist> playlists;
        if (jsonString != null) {
            playlists = gson.fromJson(jsonString, new TypeToken<List<Playlist>>() {}.getType());
        } else {
            playlists = new ArrayList<Playlist>();
        }

        favorited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (playlists.isEmpty()) {
                    Toast.makeText(view.getContext(), "No playlists available", Toast.LENGTH_SHORT).show();
                    return;
                }

                final int[] selectedIndex = {-1};
                Playlist[] playlistArray = playlists.toArray(new Playlist[0]);

                CharSequence[] playlistNames = new CharSequence[playlistArray.length];
                for (int i = 0; i < playlistArray.length; i++) {
                    playlistNames[i] = playlistArray[i].name;
                }

                new AlertDialog.Builder(view.getContext())
                        .setTitle("Save to Playlist")
                        .setSingleChoiceItems(playlistNames, -1, (dialog, which) -> {
                            selectedIndex[0] = which;
                        })
                        .setPositiveButton("Save", (dialog, which) -> {
                            if (selectedIndex[0] == -1) {
                                Toast.makeText(view.getContext(), "Please select a Playlist", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            //Lets user change name of article entered into playlist
                            EditText input = new EditText(view.getContext());
                            input.setHint("Change Article Name");
                            String playlistName = playlistArray[selectedIndex[0]].name;

                            new AlertDialog.Builder(view.getContext())
                                .setTitle("Change Article Name?").setView(input)
                                .setPositiveButton("Yes", (dialog2, selectOpt) -> {
                                    String name = input.getText().toString();
                                    playlistArray[selectedIndex[0]].addArticle(new Article(name, uri));
                                    prefs.edit().putString("Playlists", gson.toJson(playlistArray)).apply();
                                })
                                .setNegativeButton("No", (dialog2, selectOpt) -> {
                                    String name = uri;
                                    playlistArray[selectedIndex[0]].addArticle(new Article(name, uri));
                                    prefs.edit().putString("Playlists", gson.toJson(playlistArray)).apply();
                                })
                                .show();
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
    }
}