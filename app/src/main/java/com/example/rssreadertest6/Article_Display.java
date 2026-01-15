package com.example.rssreadertest6;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.net.URI;

public class Article_Display extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent receiverIntent = getIntent();
        String uri = receiverIntent.getStringExtra("uri");
        System.out.println(uri);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_article_display);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        WebView web = findViewById(R.id.WebView);
        web.loadUrl(uri);

        ImageButton iButton = findViewById(R.id.returnButton1);
        iButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent senderIntent = new Intent(Article_Display.this, MainActivity.class);
                finish();
                startActivity(senderIntent);
            }
        });
    }
}