package com.example.TheiaNewsAggregator;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

public class runTTS implements Runnable {
    private final String text;
    private final TextToSpeech tts;

    boolean ttsDone = false;

    runTTS(String text, TextToSpeech tts){
        this.text = text;
        this.tts = tts;
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
    }
    @Override
    public void run() {
        if(text.length() > 400){
            String[] strs;
            int divisions = 0;
            int length = text.length();
            int start = 0;
            while(length > 400){
                divisions = length/400;
                length = length/400;
            }
            for(int i = 0; i <= divisions; i++) {
                tts.speak(text.substring(start, (start + 400)), TextToSpeech.QUEUE_FLUSH, null, "Article_ID");
                while(!ttsDone){

                }
                tts.stop();
                start = start + 400;
                ttsDone = false;
            }
        }
        else{
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "Article_ID");
        }
    }
}
