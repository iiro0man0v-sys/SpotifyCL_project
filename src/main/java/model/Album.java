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

    public void addSong(Song song) {
        if (songs == null) {
            songs = new ArrayList<>();
        }
        songs.add(song);
        song.setAlbum(this);
    }

    public String getTitle() {return title;}
    public int getYear() {return year;}
    public String getCoverPath() {return coverPath;}
    public Artist getArtist() {return artist;}
    public void setArtist(Artist artist) {this.artist = artist;}
    public ArrayList<Song> getSongs() { return songs; }
}