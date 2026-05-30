package model;

import java.util.ArrayList;

public class Playlist {
    private String name;
    private ArrayList<Song> songs;

    public Playlist(String name) {
        this.name = name;
        this.songs = new ArrayList<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public ArrayList<Song> getSongs() { return songs; }

    public void addSong(Song song) { songs.add(song); }
    public void removeSong(Song song) { songs.remove(song); }
    public void addSongAt(int index, Song song) {songs.add(index, song);}
    public void swapSongs(int index1, int index2) {
        if (index1 >= 0 && index1 < songs.size() &&
                index2 >= 0 && index2 < songs.size()) {
            Song temp = songs.get(index1);
            songs.set(index1, songs.get(index2));
            songs.set(index2, temp);
        }
    }
    public int getTotalDuration() {
        int total = 0;
        for (Song song : songs) {
            total += song.getDuration();
        }
        return total;
    }

}