package com.example.TheiaNewsAggregator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
// import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
// import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.PopupMenu;

import com.google.gson.Gson;

// import okhttp3.OkHttpClient;
// import okhttp3.Request;
// import okhttp3.Response;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
// import java.net.MalformedURLException;
// import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;


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

        SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String jsonString = prefs.getString("Playlists", null);
        List<Playlist> playlists = gson.fromJson(jsonString, new TypeToken<List<Playlist>>() {}.getType());
        Intent receiverIntent = getIntent();
        String playlistName = receiverIntent.getStringExtra("playlist");
        Playlist playlist = null;
        if(playlists == null){
            Log.i("ERROR", "PLaylists is Null");
            EditText input = new EditText(PlaylistDisplay.this);
            new AlertDialog.Builder(PlaylistDisplay.this)
                .setTitle("Playlist is Null").setView(input)
                .setPositiveButton("Okay", (dialog, which) -> {
                    startActivity(new Intent(PlaylistDisplay.this, MainPlaylists.class));
                    finish(); 
                })
                .show();
            return;
        }

        for(int i = 0; i < playlists.size(); i++){
            if(Objects.equals(playlists.get(i).name, playlistName)){
                playlist = playlists.get(i);
                Log.i("READ_CHECK", "size=" + playlist.list.size() + " json=" + gson.toJson(playlist));
                break;
            }
        }
        if(playlist == null){
            Log.i("ERROR", "Playlist is Null");
            EditText input = new EditText(PlaylistDisplay.this);
            new AlertDialog.Builder(PlaylistDisplay.this)
                .setTitle("Playlist is Null").setView(input)
                .setPositiveButton("Okay", (dialog, which) -> dialog.dismiss())
                .show();
            startActivity(new Intent(PlaylistDisplay.this, MainPlaylists.class));
            finish();
        }else{
            LinearLayout mainContainer = findViewById(R.id.LL1);
            for (int i2 = 0; i2 < playlist.list.size(); i2++) {
                Article article = playlist.list.get(i2);
                String plName = playlist.name;
                final Playlist finalPlaylist = playlist;

                int index = i2;

                LinearLayout.LayoutParams titleLp = new LinearLayout.LayoutParams(300,150);
                TextView tv1 = new TextView(PlaylistDisplay.this);
                tv1.setText(article.name);
                tv1.setLayoutParams(titleLp);
                tv1.setTextSize(30);

                String link = article.link;
                LinearLayout listMain = new LinearLayout(PlaylistDisplay.this);
                listMain.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                listMain.setOnClickListener(v -> {
                    Intent intent = new Intent(PlaylistDisplay.this, Article_Display.class);
                    intent.putExtra("uri", link);
                    startActivity(intent);
                });

                Button editButton = new Button(PlaylistDisplay.this);
                editButton.setText(getResources().getString(R.string.Edit));
                LinearLayout.LayoutParams editLp = new LinearLayout.LayoutParams(300, 150);
                editButton.setLayoutParams(editLp);
                editButton.setTextSize(30);
                editButton.setOnClickListener(v -> {
                    PopupMenu popupMenu = new PopupMenu(this, v);
                    popupMenu.getMenuInflater().inflate(R.menu.edit_article_btn, popupMenu.getMenu());

                    popupMenu.setOnMenuItemClickListener(item -> {
                        int itemId = item.getItemId();
                        if (itemId == R.id.changeName) {
                            EditText input = new EditText(v.getContext());
                            input.setHint("Enter new name");
                            new AlertDialog.Builder(v.getContext())
                                .setTitle("Change Name").setView(input)
                                .setPositiveButton("Apply", (dialog, which) -> {
                                        String userText = input.getText().toString();
                                        if (!userText.isEmpty()) {
                                            article.name = userText;
                                            for(int i3 = 0; i3 < playlists.size(); i3++){
                                                if(Objects.equals(playlists.get(i3).name, plName)){
                                                    playlists.set(i3, finalPlaylist);
                                                    prefs.edit().putString("Playlists", gson.toJson(playlists)).apply();
                                                    Intent intent = new Intent(PlaylistDisplay.this, PlaylistDisplay.class);
                                                    intent.putExtra("playlist", plName);
                                                    startActivity(intent);
                                                    finish();
                                                    break;
                                                }
                                            }
                                        }

                                    })
                                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                                .show();
                            return true;
                        } else if (itemId == R.id.delete) {
                            new AlertDialog.Builder(v.getContext()) //make sure user wants to delete playlist
                                .setTitle("Delete "+article.name+"?")
                                .setPositiveButton("Yes", (dialog, which) -> {
                                        finalPlaylist.list.remove(index);
                                        for(int i3 = 0; i3 < playlists.size(); i3++){
                                            if(Objects.equals(playlists.get(i3).name, plName)){
                                                playlists.set(i3, finalPlaylist);
                                                prefs.edit().putString("Playlists", gson.toJson(playlists)).apply();
                                                Intent intent = new Intent(PlaylistDisplay.this, PlaylistDisplay.class);
                                                intent.putExtra("playlist", plName);
                                                startActivity(intent);
                                                finish();
                                                break;
                                            }
                                        }
                                    })
                                .setNegativeButton("No", (dialog, which) -> dialog.dismiss())
                                .show();
                            return true;
                        }
                        return false;
                    });
                    popupMenu.show();
                });

                listMain.addView(tv1);
                listMain.addView(editButton);
                mainContainer.addView(listMain);
            }
        }
    }
}