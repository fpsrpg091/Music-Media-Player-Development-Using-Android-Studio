package com.example.myapplication.ui;

import java.util.List;

public class SongData {
    private String title;
    private List<String> path;
    private String date;
    private long size;

    public SongData(String title, List<String> path, String date, long size) {
        this.title = title;
        this.path = path;
        this.date = date;
        this.size = size;
    }

    public String getTitle() {
        return title;
    }

    public  List<String>  getPath() {
        return path;
    }

    public String getDate() {
        return date;
    }

    public long getSize() {
        return size;
    }
}
