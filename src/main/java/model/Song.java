package model;

public class Song {
    private String title;
    private int duration;
    private String genre;
    private int year;
    private String filePath;
    private String coverPath;
    private Artist artist;
    private Album album;

    public Song(String title, String genre, int year, String filePath,
                String coverPath, Artist artist, Album album) {
        this.title = title;
        this.genre = genre;
        this.year = year;
        this.filePath = filePath;
        this.coverPath = coverPath;
        this.artist = artist;
        this.album = album;
    }


    public String getTitle() { return title; }
    public int getDuration() { return duration; }
    public String getGenre() { return genre; }
    public String getFilePath() { return filePath; }
    public String getCoverPath() { return coverPath; }
    public Artist getArtist() {return artist;}
    public Album getAlbum() {return album;}
    public void setAlbum(Album album) {this.album = album;}

    public String getFormattedDuration() {
        int minutes = duration / 60;
        int seconds = duration % 60;
        return minutes + ":" + String.format("%02d", seconds);
    }

    @Override
    public String toString() {
        return title + " - " + (artist != null ? artist.getName() : "Neznamy");
    }
}