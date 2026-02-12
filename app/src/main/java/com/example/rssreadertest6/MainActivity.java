package com.example.rssreadertest6;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import org.apache.commons.io.FileUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.crashlytics.buildtools.reloc.org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Objects;


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

        LinearLayout b1 = findViewById((R.id.LL1));
        b1.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                DialogFragment dialog = new StartGameDialogFragment();
                dialog.show(getSupportFragmentManager(), "NoticeDialogFragment");
            }
        });

        new Thread(() -> {
            try {
                String sourcelinks = getLinks("https://feeds.bbci.co.uk/news/rss.xml?edition=uk");
                String titles = getTitles("https://feeds.bbci.co.uk/news/rss.xml?edition=uk");
                Image img = getImages("https://feeds.bbci.co.uk/news/rss.xml?edition=uk");
                LinkedList<String> list = new LinkedList<>();
                if (sourcelinks != null) {
                    Collections.addAll(list, sourcelinks.split("\\R"));
                }

                runOnUiThread(() -> {
                    LinearLayout container = findViewById(R.id.LL1);
                    for (int i = 2; i < list.size(); i++) {
                        Log.d("LIST: ", list.get(i));
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
                        container.addView(bb);
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
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
                    temp = temp.replace("<title>","");
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
    public static Image getImages(String urlAddress) throws IOException{
        try{
            URL rssUrl = new URL(urlAddress);

            BufferedReader in =new BufferedReader(new InputStreamReader(rssUrl.openStream()));

            String line = in.readLine();
            StringBuilder sCode = new StringBuilder();

            if (line != null) {
                sCode.append(line);
            }
            String imageFormat = ".jpg";
            while((line = in.readLine())!= null){
                if(line.contains("<media:thumbnail")){
                    int firstPos = line.indexOf("<media:thumbnail");
                    int secPos = line.indexOf("url=\"");
                    String temp = line.substring(firstPos, secPos);
                    temp = temp.replace(temp,"");
                    int lastPos = temp.indexOf("\"");
                    imageFormat = temp.substring(lastPos-3,lastPos);
                    temp = temp.substring(0,lastPos);
                    sCode.append(temp).append("\n");
                }
            }
            URL url = new URL(sCode.toString());

            Image f = null;
            FileUtils.copyURLToImage(url, f);
            //Glide.with(getApplicationContext()).load(sCode.toString()).placeholder(R.drawble.loading);
//            try{
//                URL url = new URL(sCode.toString());
//                InputStream is = url.openStream();
//                FileOutputStream fo = new FileOutputStream("thumbnail."+ imageFormat);
//                int b = 0;
//                while((b=is.read())!=-1){
//                    fo.write(b);
//                }
//                fo.close();
//                is.close();
//                in.close();
//            } catch(MalformedURLException e){
//                Log.d("IMAGE URL: ", Objects.requireNonNull(e.getMessage()));
//            }

            return f;
        } catch (MalformedURLException mue){
            System.out.println("Malformed URL");
        } catch (IOException ioe){
            System.out.println("Something went wrong reading the contents");
        }
        return null;
    }
    public static class StartGameDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the Builder class for convenient dialog construction.
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage(R.string.project_id)
                    .setPositiveButton(R.string.app_name, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // START THE GAME!
                        }
                    });
            // Create the AlertDialog object and return it.
            return builder.create();
        }
    }
}