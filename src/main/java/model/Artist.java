package model;

import java.util.ArrayList;

public class Artist {
    private String name;
    private String bio;
    private String photoPath;
    private ArrayList<Album> albums;

    public Artist(String name, String bio, String photoPath) {
        this.name = name;
        this.bio = bio;
        this.photoPath = photoPath;
        this.albums = new ArrayList<>();
    }

    public void addAlbum(Album album) {
        albums.add(album);
        album.setArtist(this);
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
    public String getPhotoPath() { return photoPath; }
    public void setPhotoPath(String photoPath) { this.photoPath = photoPath; }
    public ArrayList<Album> getAlbums() { return albums; }

    public ArrayList<Song> getAllSongs() {
        ArrayList<Song> allSongs = new ArrayList<>();
        for (Album album : albums) {
            allSongs.addAll(album.getSongs());
        }
        return allSongs;
    }
}