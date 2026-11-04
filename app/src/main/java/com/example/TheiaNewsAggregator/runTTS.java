public class runTTS implements Runnable {
    private final String text;
    private final TextToSpeech tts;

    runTTS(String text, TextToSpeech tts) {
        this.text = text;
        this.tts = tts;
    }

    @Override
    public void run() {
        int max = Math.min(TextToSpeech.getMaxSpeechInputLength(), 1000);
        List<String> chunks = chunk(text, max);
        for (int i = 0; i < chunks.size(); i++) {
            int mode = (i == 0) ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD;
            tts.speak(chunks.get(i), mode, null, "Article_" + i);
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