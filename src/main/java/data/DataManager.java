package data;

import model.*;
import java.util.ArrayList;


public class DataManager {
    private ArrayList<Song> allSongs;
    private ArrayList<Album> allAlbums;
    private ArrayList<Artist> allArtists;
    private ArrayList<Playlist> allPlaylists;
    private ArrayList<Song> recentlyPlayed;
    private ArrayList<Object> recentlyAdded;

    public DataManager() {
        allSongs = new ArrayList<>();
        allAlbums = new ArrayList<>();
        allArtists = new ArrayList<>();
        allPlaylists = new ArrayList<>();
        recentlyPlayed = new ArrayList<>();
        recentlyAdded = new ArrayList<>();
    }


    public void addSong(Song song) {
        allSongs.add(song);
        recentlyAdded.add(song);
    }

    public ArrayList<Song> getAllSongs() {
        return allSongs;
    }

    public void addAlbum(Album album) {
        allAlbums.add(album);
        recentlyAdded.add(album);
    }

    public ArrayList<Album> getAllAlbums() {
        return allAlbums;
    }

    public void addArtist(Artist artist) {
        allArtists.add(artist);
    }

    public ArrayList<Artist> getAllArtists() {
        return allArtists;
    }

    public ArrayList<Playlist> getAllPlaylists() {
        return allPlaylists;
    }

    public Playlist createPlaylist(String name) {
        Playlist playlist = new Playlist(name);
        allPlaylists.add(playlist);
        return playlist;
    }

    public void addToRecentlyPlayed(Song song) {
        recentlyPlayed.addFirst(song);
        if (recentlyPlayed.size() > 50) {
            recentlyPlayed.removeLast();
        }
    }


    public ArrayList<Song> getRecentlyPlayed() {
        return recentlyPlayed;
    }

    public ArrayList<Object> getRecentlyAdded() {
        return recentlyAdded;
    }

    /**
     * Searches the song library by title or genre
     */
    public ArrayList<Song> searchSongs(String query) {
        ArrayList<Song> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Song song : allSongs) {
            if (song.getTitle().toLowerCase().contains(lowerQuery) ||
                    song.getGenre().toLowerCase().contains(lowerQuery)) {
                results.add(song);
            }
        }
        return results;
    }

    /**
     * Searches the album library by title
     */
    public ArrayList<Album> searchAlbums(String query) {
        ArrayList<Album> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Album album : allAlbums) {
            if (album.getTitle().toLowerCase().contains(lowerQuery)) {
                results.add(album);
            }
        }
        return results;
    }

    /**
     * Searches the artist library by name
     */
    public ArrayList<Artist> searchArtists(String query) {
        ArrayList<Artist> results = new ArrayList<>();
        String lowerQuery = query.toLowerCase();
        for (Artist artist : allArtists) {
            if (artist.getName().toLowerCase().contains(lowerQuery)) {
                results.add(artist);
            }
        }
        return results;
    }
}