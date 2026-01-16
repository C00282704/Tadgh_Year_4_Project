package com.example.rssreadertest6;

import static com.example.rssreadertest6.R.*;
import static com.example.rssreadertest6.R.id.textToSpeechButton;
import static java.util.Locale.ENGLISH;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Article_Display extends AppCompatActivity {

    TextToSpeech tts;
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

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if(i != TextToSpeech.ERROR){
                    tts.setLanguage(ENGLISH);
                }
            }
        });



        WebView article = findViewById(R.id.WebView);

        ImageButton iButton = findViewById(R.id.returnButton);
        iButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent senderIntent = new Intent(Article_Display.this, MainActivity.class);
                finish();
                startActivity(senderIntent);
            }
        });
        TextView contentView = (TextView) findViewById(R.id.contentView);
        class MyJavaScriptInterface {
            private TextView contentView;
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
        article.getSettings().setJavaScriptEnabled(true);
        article.addJavascriptInterface(new MyJavaScriptInterface(contentView), "INTERFACE");
        article.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url)
            {
                view.loadUrl("javascript:window.INTERFACE.processContent(document.getElementsByTagName('body')[0].innerText);");
            }
        });

        article.loadUrl(uri);
        ImageButton ttsButton = findViewById(textToSpeechButton);
//        ttsButton.setOnClickListener(new View.OnClickListener(){
//            @Override
//            public void onClick(View view) {
////                String text = "XXXXXXXXXXXXXXX";
//                WebView element = driver.findElement(By.id("exampleId"));
//                String text = element.getText();
//
//                tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
//                //FInd out how to get the text off of the article
//            }
//        });
    }
}