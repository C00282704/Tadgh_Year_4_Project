package com.example.TheiaNewsAggregator;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;


public class MainPlaylists extends AppCompatActivity {
    List<String> prefLinks = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_playlists);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        @SuppressLint({"MissingInflatedId", "LocalSuppress"}) ImageButton settingsButton = findViewById(R.id.settingsBtn);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainPlaylists.this, Settings.class));
            }
        });
        Button mainBtn = findViewById(R.id.MainBtn);
        mainBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainPlaylists.this, MainActivity.class));
            }
        });

        ImageButton addButton = findViewById(R.id.addPlaylistButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                EditText input = new EditText(v.getContext());
                input.setHint("Enter NEW playlist name");
                new AlertDialog.Builder(v.getContext()).setTitle("New Playlist").setView(input).setPositiveButton("Create", (dialog, which) -> {
                            String userText = input.getText().toString();
                            if (!userText.isEmpty()) {
                                SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
                                Gson gson = new Gson();

                                //prefs.edit().putString(userText, gson.toJson(new ArrayList<>())).apply();

                                String jsonString = prefs.getString("Playlists", null);
                                List<String> playlists;
                                if (jsonString != null) {
                                    playlists = gson.fromJson(jsonString, new TypeToken<List<List<String,String>>>() {}.getType());
                                } else {
                                    playlists = new ArrayList<>();
                                }
                                
                                playlists.add(userText);
                                prefs.edit().putString("Playlists", gson.toJson(playlists)).apply();
                            }
                            finish();
                            Intent intent = new Intent(MainPlaylists.this, MainPlaylists.class);
                            startActivity(intent);
                        })
                        .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                        .show();
            }
        });

        new Thread(() -> {
            SharedPreferences prefs = getSharedPreferences("Prefs", Context.MODE_PRIVATE);
            Log.i("PREFERENCES", prefs.toString());
            Gson gson = new Gson();
            String jsonString = prefs.getString("Playlists", null);
            List<String> playlists = gson.fromJson(jsonString, new TypeToken<List<String>>() {}.getType());
            if(playlists != null){
                runOnUiThread(() -> {
                    LinearLayout scrollList = findViewById(R.id.LL1);
                    for (int i = 0; i < playlists.size(); i++) {
                        Log.i("CREATED", "Created");

                        LinearLayout newLayout = new LinearLayout(MainPlaylists.this);
                        TextView title = new TextView(MainPlaylists.this);
                        title.setText(playlists.get(i));//Playlist name
                        Log.i("Playlist Name:", playlists.get(i));

                        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(205,350);
                        title.setLayoutParams(lp);
                        title.setGravity(Gravity.CENTER);
                        title.setTextSize(1 , 25);

                        ImageView folder = new ImageView(MainPlaylists.this);
                        folder.setImageDrawable(getDrawable(R.drawable.file));
                        newLayout.addView(folder);
                        newLayout.addView(title);
                        String pl = prefs.getString(playlists.get(i), null);
                        newLayout.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                Intent intent = new Intent(MainPlaylists.this, Playlist.class);
                                intent.putExtra("playlist", pl);
                                startActivity(intent);
                            }
                        });
                        scrollList.addView(newLayout);
                    }
                });
            }
        }).start();
    }
}