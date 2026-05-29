package data;

import model.Album;
import model.Artist;
import model.Song;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Scanner;
import model.Playlist;

public class DataLoader {

    public static void loadData(DataManager dataManager, String csvFilePath) {
        File file = new File(csvFilePath);
        if (!file.exists()) {
            System.err.println("Datovy soubor nebyl nalezen: " + csvFilePath);
            return;
        }

        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(";");
                if (parts.length == 0) continue;

                String type = parts[0];
                try {
                    switch (type) {
                        case "ARTIST":
                            if (parts.length >= 4) {
                                Artist artist = new Artist(parts[1], parts[2], parts[3]);
                                dataManager.addArtist(artist);
                            }
                            break;
                        case "ALBUM":
                            if (parts.length >= 5) {
                                Artist artist = findArtist(dataManager, parts[4]);
                                if (artist != null) {
                                    Album album = new Album(parts[1], Integer.parseInt(parts[2]), parts[3], artist);
                                    artist.addAlbum(album);
                                    dataManager.addAlbum(album);
                                }
                            }
                            break;
                        case "SONG":
                            if (parts.length >= 7) {
                                Artist artist = findArtist(dataManager, parts[5]);
                                Album album = findAlbum(dataManager, parts[6]);
                                if (artist != null && album != null) {
                                    String coverPath = album.getCoverPath();
                                    Song song = new Song(parts[1], parts[2], Integer.parseInt(parts[3]), parts[4], coverPath, artist, album);
                                    album.addSong(song);
                                    dataManager.addSong(song);
                                }
                            }
                            break;
                    }
                } catch (Exception e) {
                    System.err.println("Chyba pri parsovani radku: " + line);
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Artist findArtist(DataManager manager, String name) {
        for (Artist a : manager.getAllArtists()) {
            if (a.getName().equalsIgnoreCase(name)) {
                return a;
            }
        }
        return null;
    }

    private static Album findAlbum(DataManager manager, String title) {
        for (Album a : manager.getAllAlbums()) {
            if (a.getTitle().equalsIgnoreCase(title)) {
                return a;
            }
        }
        return null;
    }

    public static void loadPlaylists(DataManager manager, String filePath) {
        File file = new File(filePath);
        if (!file.exists()) return;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(";");
                if (parts.length > 0) {
                    Playlist p = manager.createPlaylist(parts[0]);
                    for (int i = 1; i < parts.length; i++) {
                        for (Song s : manager.getAllSongs()) {
                            if (s.getFilePath().equals(parts[i])) {
                                p.addSong(s);
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Chyba při načítání playlistů: " + e.getMessage());
        }
    }

    public static void savePlaylists(DataManager manager, String filePath) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            for (Playlist p : manager.getAllPlaylists()) {
                StringBuilder sb = new StringBuilder(p.getName());
                for (Song s : p.getSongs()) {
                    sb.append(";").append(s.getFilePath());
                }
                writer.println(sb.toString());
            }
        } catch (Exception e) {
            System.err.println("Chyba při ukládání playlistů: " + e.getMessage());
        }
    }
}
