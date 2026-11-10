package com.example.TheiaNewsAggregator;

import android.speech.tts.TextToSpeech;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class runTTS implements Runnable {
    private final String text;
    private final TextToSpeech tts;
    static final String LAST_ID = "Article_END";

    runTTS(String text, TextToSpeech tts) {
        this.text = text;
        this.tts = tts;
    }

    @Override
    public void run() {
        int max = Math.min(TextToSpeech.getMaxSpeechInputLength(), 1000);
        List<String> chunks = chunk(text, max);
        for (int i = 0; i < chunks.size(); i++) {
            String id = (i == chunks.size() - 1) ? LAST_ID : "Article_" + i;
            tts.speak(chunks.get(i), mode, null, id);
        }
    }

    private static List<String> chunk(String text, int max) {
        List<String> chunks = new ArrayList<>();
        BreakIterator it = BreakIterator.getSentenceInstance(Locale.getDefault());
        it.setText(text);
        StringBuilder cur = new StringBuilder();
        int start = it.first();
        for (int end = it.next(); end != BreakIterator.DONE; start = end, end = it.next()) {
            String sentence = text.substring(start, end);
            if (cur.length() + sentence.length() > max && cur.length() > 0) {
                chunks.add(cur.toString());
                cur.setLength(0);
            }
            cur.append(sentence);
        }
        if (cur.length() > 0) chunks.add(cur.toString());
        return chunks;
    }
}