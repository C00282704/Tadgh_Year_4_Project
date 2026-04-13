package com.example.TheiaNewsAggregator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Settings extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_settings);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        Button rssbutton = findViewById(R.id.rssFeedsBtn);
        rssbutton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
                startActivity(new Intent(Settings.this, ChangeRSSFeeds.class));
            }
        });
        Button creditsButton = findViewById(R.id.creditsBtn);
        rssbutton.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
                startActivity(new Intent(Settings.this, Credits.class));
            }
        });
    }
}