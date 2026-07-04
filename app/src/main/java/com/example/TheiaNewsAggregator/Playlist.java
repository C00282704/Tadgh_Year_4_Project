package com.example.TheiaNewsAggregator;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    List<Article> list = new ArrayList<>();
    String name;

    public Playlist(String name) {
        this.name = name;
    }

    public void addArticle(Article newArticle) {
        this.list.add(newArticle);
    }
}