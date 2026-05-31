package audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;
import model.Album;
import model.Song;
import java.io.File;
import data.DataManager;
import java.util.ArrayList;
import java.util.Random;
import static javafx.util.Duration.ZERO;


public class PlayerManager {
    private DataManager dataManager;
    private ArrayList<Song> currentQueue = new ArrayList<>();
    private ArrayList<Song> originalQueue = new ArrayList<>();
    private int currentIndex = -1;
    private MediaPlayer mediaPlayer;
    private boolean isShuffle = false;
    private int repeatMode = 0;
    private double volume = 0.5;
    private Random random = new Random();

    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public Song getCurrentSong() {
        if (currentIndex >= 0 && currentIndex < currentQueue.size()) {
            return currentQueue.get(currentIndex);
        }
        return null;
    }



    /**
     * Loads and immediately plays the given song.
     * @param song the song to play; silently returns if {@code null} or has no file path
     */
    public void playSong(Song song) {
        if (song == null || song.getFilePath() == null) {
            return;
        }

        if (dataManager != null) {
            dataManager.addToRecentlyPlayed(song);
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
            mediaPlayer = null;
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

    /**
     * Resumes playback of the current song.
     */
    public void playCurrent() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        } else if (getCurrentSong() != null) {
            playSong(getCurrentSong());
        }
    }

    /**
     * Pauses playback
     */
    public void pause() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    /**
     * Advances to the next song in the queue.
     */
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

    /**
     * Moves to the previous song in the queue.
     */
    public void previous() {
        if (currentQueue.isEmpty()) {
            return;
        }

        if (mediaPlayer != null && mediaPlayer.getCurrentTime().toSeconds() > 3) {
            mediaPlayer.seek(ZERO);
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


    /**
     * Replaces the current queue with the provided song list and starts
     * playing from the given index.
     * @param songs      the new list of songs to use as the queue
     * @param startIndex the index of the song to play immediately;
     */
    public void setQueue(ArrayList<Song> songs, int startIndex) {
        this.currentQueue = new ArrayList<>(songs);
        this.originalQueue = new ArrayList<>(songs);
        this.currentIndex = startIndex;
        if (currentIndex >= 0 && currentIndex < currentQueue.size()) {
            playSong(currentQueue.get(currentIndex));
        }
    }

    /**
     * Determines what happens when the current song finishes
     */
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

    /**
     * Toggles shuffle mode on or off.
     */
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
            originalQueue = new ArrayList<>(currentQueue); // preserve original order
            currentQueue = shuffled;
        } else {
            currentQueue = new ArrayList<>(originalQueue);
        }
    }
    /**
     * Cycles the repeat mode through its three states: 0 → 1 → 2 → 0.
     */
    public void toggleRepeat() {
        repeatMode = (repeatMode + 1) % 3;
    }

    /**
     * Sets the playback volume, clamped to [0.0, 1.0].
     */
    public void setVolume(double volume) {
        this.volume = Math.max(0.0, Math.min(1.0, volume));
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(this.volume);
        }
    }

    /**
     * Returns the current playback position in seconds.
     */
    public double getCurrentPosition() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentTime().toSeconds();
        }
        return 0;
    }

    /**
     * Returns the total duration of the current track in seconds.
     */
    public double getDuration() {
        if (mediaPlayer != null && mediaPlayer.getMedia() != null) {
            return mediaPlayer.getMedia().getDuration().toSeconds();
        }
        return 0;
    }

    /**
     * Seeks to the given position within the current track.
     */
    public void seek(double seconds) {
        if (mediaPlayer != null) {
            mediaPlayer.seek(new Duration(seconds * 1000));
        }
    }

    /**
     * Returns whether the player is currently in the playing state.
     */
    public boolean isPlaying() {
        return mediaPlayer != null &&
                mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
    }


    public ArrayList<Song> getCurrentQueue() { return currentQueue; }

    public int getCurrentIndex() { return currentIndex; }

    public void setCurrentIndex(int currentIndex) { this.currentIndex = currentIndex; }

    public boolean isShuffle() { return isShuffle; }

    public int getRepeatMode() { return repeatMode; }
}