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
    public String getBio() { return bio; }
    public String getPhotoPath() { return photoPath; }
    public ArrayList<Album> getAlbums() { return albums; }
}