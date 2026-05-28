package audio;


import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import model.Album;
import model.Song;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;


public class PlayerManager {
    private ArrayList<Song> currentQueue = new ArrayList<>();
    private ArrayList<Song> originalQueue = new ArrayList<>();
    private int currentIndex = -1;
    private MediaPlayer mediaPlayer;
    private boolean isShuffle = false;
    private int repeatMode = 0; // 0 = bez opakování, 1 = opakovat skladbu, 2 = opakovat album
    private double volume = 0.5;
    private Random random = new Random();


    public Song getCurrentSong() {
        if (currentIndex >= 0 && currentIndex < currentQueue.size()) {
            return currentQueue.get(currentIndex);
        }
        return null;
    }


    public void playSong(Song song) {
        if (song == null || song.getFilePath() == null) {
            return;
        }

        File file = new File(song.getFilePath());
        if (!file.exists()) {
            System.out.println("Soubor nenalezen: " + song.getFilePath());
            return;
        }

        String uri = file.toURI().toString();


        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
        }

        try {
            Media media = new Media(uri);
            mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setVolume(volume);

            mediaPlayer.setOnEndOfMedia(() -> {
                repeating();
            });

            mediaPlayer.play();
        } catch (Exception e) {
            System.out.println("Chyba při přehrávání: " + e.getMessage());
            e.printStackTrace();
        }
    }


    public void playCurrent() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        } else if (getCurrentSong() != null) {
            playSong(getCurrentSong());
        }
    }

    public void play() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }


    public void next() {
        if (currentQueue.isEmpty()) {
            return;
        }

        if (isShuffle) {
            int newIndex = currentIndex;
            if (currentQueue.size() > 1) {
                while (newIndex == currentIndex) {
                    newIndex = random.nextInt(currentQueue.size());
                }
            }
            currentIndex = newIndex;
        } else {
            currentIndex++;
            if (currentIndex >= currentQueue.size()) {
                currentIndex = 0;
            }
        }

        if (currentIndex >= 0 && currentIndex < currentQueue.size()) {
            playSong(currentQueue.get(currentIndex));
        }
    }

    public void previous() {
        if (currentQueue.isEmpty()) {
            return;
        }

        if (mediaPlayer != null && mediaPlayer.getCurrentTime().toSeconds() > 3) {
            mediaPlayer.seek(javafx.util.Duration.ZERO);
            return;
        }

        currentIndex--;
        if (currentIndex < 0) {
            currentIndex = currentQueue.size() - 1;
        }

        if (currentIndex >= 0 && currentIndex < currentQueue.size()) {
            playSong(currentQueue.get(currentIndex));
        }
    }


    public void setQueue(ArrayList<Song> songs, int startIndex) {
        this.currentQueue = new ArrayList<>(songs);
        this.originalQueue = new ArrayList<>(songs);
        this.currentIndex = startIndex;
        if (currentIndex >= 0 && currentIndex < currentQueue.size()) {
            playSong(currentQueue.get(currentIndex));
        }
    }


    private void repeating() {
        if (repeatMode == 1) {
            playSong(currentQueue.get(currentIndex));
        } else if (repeatMode == 2) {
            Song currentSong = currentQueue.get(currentIndex);
            Album currentAlbum = currentSong.getAlbum();

            if (currentAlbum != null) {
                ArrayList<Song> albumSongs = currentAlbum.getSongs();
                int currentIndexInAlbum = albumSongs.indexOf(currentSong);

                if (currentIndexInAlbum >= 0 && currentIndexInAlbum < albumSongs.size() - 1) {
                    playSong(albumSongs.get(currentIndexInAlbum + 1));
                } else {
                    playSong(albumSongs.getFirst());
                }
            } else {
                next();
            }
        } else {
            next();
        }
    }

    public ArrayList<Song> getCurrentQueue() {
        return currentQueue;
    }

    public void setCurrentQueue(ArrayList<Song> currentQueue) {
        this.currentQueue = currentQueue;
    }

    public ArrayList<Song> getOriginalQueue() {
        return originalQueue;
    }

    public void setOriginalQueue(ArrayList<Song> originalQueue) {
        this.originalQueue = originalQueue;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }

    public MediaPlayer getMediaPlayer() {
        return mediaPlayer;
    }

    public void setMediaPlayer(MediaPlayer mediaPlayer) {
        this.mediaPlayer = mediaPlayer;
    }

    public boolean isShuffle() {
        return isShuffle;
    }


    public void toggleShuffle() {
        isShuffle = !isShuffle;
        if (isShuffle) {

            ArrayList<Song> shuffled = new ArrayList<>(currentQueue);
            for (int i = shuffled.size() - 1; i > 0; i--) {
                int j = random.nextInt(i + 1);
                Song temp = shuffled.get(i);
                shuffled.set(i, shuffled.get(j));
                shuffled.set(j, temp);
            }
            originalQueue = new ArrayList<>(currentQueue);
            currentQueue = shuffled;
        } else {
            currentQueue = new ArrayList<>(originalQueue);
        }
    }

    public int getRepeatMode() {
        return repeatMode;
    }


    public void toggleRepeat() {
        repeatMode = (repeatMode + 1) % 3;
    }

    public double getVolume() {
        return volume;
    }


    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume));
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(this.volume);
        }
    }


    public void increaseVolume() {
        setVolume(volume + 0.1);
    }


    public void decreaseVolume() {
        setVolume(volume - 0.1);
    }


    public double getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentTime().toSeconds();
        }
        return 0;
    }


    public double getDuration() {
        if (mediaPlayer != null && mediaPlayer.getMedia() != null) {
            return mediaPlayer.getMedia().getDuration().toSeconds();
        }
        return 0;
    }


    public void seek(double seconds) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(new javafx.util.Duration(seconds * 1000));
        }
    }
}