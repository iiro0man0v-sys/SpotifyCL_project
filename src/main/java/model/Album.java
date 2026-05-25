package model;

import java.util.ArrayList;

public class Album {
    private String title;
    private int year;
    private String coverPath;
    private ArrayList<Song> songs;

    public Album(String title, int year, String coverPath) {
        this.title = title;
        this.year = year;
        this.coverPath = coverPath;
        this.songs = new ArrayList<>();
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public String getTitle() { return title; }
    public int getYear() { return year; }
    public String getCoverPath() { return coverPath; }
    public ArrayList<Song> getSongs() { return songs; }
}