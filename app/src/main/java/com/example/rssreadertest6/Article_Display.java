package com.example.rssreadertest6;
import static com.example.rssreadertest6.R.id.textToSpeechButton;
import static java.util.Locale.ENGLISH;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
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
    Boolean ttsPressed = false;
    ImageButton ttsButton;
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

        ImageButton iButton = findViewById(R.id.returnButton);
        iButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent senderIntent = new Intent(Article_Display.this, MainActivity.class);
                finish();
                tts.stop();
                tts.shutdown();
                startActivity(senderIntent);
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