package model;

public class Song {
    private String title;
    private int duration;
    private String genre;
    private int year;
    private String filePath;
    private String coverPath;

    public Song(String title, int duration, String genre, int year, String filePath, String coverPath) {
        this.title = title;
        this.duration = duration;
        this.genre = genre;
        this.year = year;
        this.filePath = filePath;
        this.coverPath = coverPath;
    }


    public String getTitle() { return title; }
    public int getDuration() { return duration; }
    public String getGenre() { return genre; }
    public int getYear() { return year; }
    public String getFilePath() { return filePath; }
    public String getCoverPath() { return coverPath; }

    @Override
    public String toString() {
        return title + " (" + (duration / 60) + ":" + String.format("%02d", duration % 60) + ")";
    }
}