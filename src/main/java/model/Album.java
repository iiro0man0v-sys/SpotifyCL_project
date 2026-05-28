package model;

import java.util.ArrayList;

public class Album {
    private String title;
    private int year;
    private String coverPath;
    private ArrayList<Song> songs;
    private Artist artist;

    public Album(String title, int year, String coverPath, Artist artist) {
        this.title = title;
        this.year = year;
        this.coverPath = coverPath;
        this.songs = new ArrayList<>();
        this.artist = artist;
    }

    public Album(String title, int year, String coverPath) {
        this.title = title;
        this.year = year;
        this.coverPath = coverPath;
    }
    public Album() {
        this.songs = new ArrayList<>();
    }

    public void addSong(Song song) {
        if (songs == null) {
            songs = new ArrayList<>();
        }
        songs.add(song);
        song.setAlbum(this);
    }

    public String getTitle() {return title;}
    public void setTitle(String title) {this.title = title;}
    public int getYear() {return year;}
    public void setYear(int year) {this.year = year;}
    public String getCoverPath() {return coverPath;}
    public void setCoverPath(String coverPath) {this.coverPath = coverPath;}
    public Artist getArtist() {return artist;}
    public void setArtist(Artist artist) {this.artist = artist;}
    public ArrayList<Song> getSongs() { return songs; }

    public int getTotalDuration() {
        int total = 0;
        for (Song song : songs) {
            total += song.getDuration();
        }
        return total;
    }
}