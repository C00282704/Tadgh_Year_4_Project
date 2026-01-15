package com.example.rssreadertest6;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;

public class MainActivity extends AppCompatActivity {

    String sourceCode;
    LinkedList<String> subStr = new LinkedList<>();

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

        LinearLayout scroll = findViewById((R.id.LL1));
        new Thread(() -> {
            try {
                String source = readRSS("https://feeds.bbci.co.uk/news/rss.xml?edition=uk");
                LinkedList<String> list = new LinkedList<>();
                if (source != null) {
                    Collections.addAll(list, source.split("\\R"));
                }

                runOnUiThread(() -> {
                    LinearLayout container = findViewById(R.id.LL1);
                    for (int i = 2; i < list.size(); i++) {
                        String link = list.get(i);
                        Button bb = new Button(MainActivity.this);
                        bb.setLayoutParams(new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT));
                        bb.setText(link);
                        bb.setBackgroundColor(5016882);
                        System.out.println(link);
                        bb.setOnClickListener(new View.OnClickListener(){
                            public void onClick(View v){
                                Intent senderIntent = new Intent(MainActivity.this, Article_Display.class);
                                senderIntent.putExtra("uri", link);
                                finish();
                                startActivity(senderIntent);
                            }
                        });
//                        bb.setOnClickListener(view -> {
//                            startActivity(new Intent(MainActivity.this, Article_Display.class));
//                            Uri uri = Uri.parse(link);
//                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
//                        });
                        container.addView(bb);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
    public static String readRSS(String urlAddress) throws IOException{
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
}