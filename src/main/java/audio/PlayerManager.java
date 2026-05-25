package audio;

import javafx.scene.media.MediaPlayer;
import model.Song;

import java.util.ArrayList;

public class PlayerManager {
    private ArrayList<Song> currentQueue = new ArrayList<>();
    private ArrayList<Song> originalQueue = new ArrayList<>(); // Pro vypnuti shuffle
    private int currentIndex = -1;
    private MediaPlayer mediaPlayer;
    private boolean isShuffle = false;
    private int repeatMode = 0; // 0 = bez opakování, 1 = opakovat skladbu, 2 = opakovat album


    public Song getCurrentSong() {
        if (currentIndex >= 0 && currentIndex < currentQueue.size()) {
            return currentQueue.get(currentIndex);
        }
        return null;
    }
    public void playCurrent(){}
    public void play() { if (mediaPlayer != null) mediaPlayer.play(); }
    public void pause() { if (mediaPlayer != null) mediaPlayer.pause(); }

    public void next() {}

    public void previous() {}

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

    public void setShuffle(boolean shuffle) {
        isShuffle = shuffle;
    }

    public int getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(int repeatMode) {
        this.repeatMode = repeatMode;
    }
}
