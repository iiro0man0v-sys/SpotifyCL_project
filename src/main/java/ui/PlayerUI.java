package ui;

import audio.PlayerManager;
import data.DataLoader;
import data.DataManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Playlist;
import model.Song;

import java.io.File;

import static javafx.geometry.Side.TOP;

/**
 * The persistent player bar rendered at the bottom of the application window.
 */
public class PlayerUI {
    private HBox root;
    private PlayerManager playerManager;
    private DataManager dataManager;
    private Label songTitleLbl;
    private Label artistLbl;
    private ImageView coverView;
    private Button playBtn;
    private Slider progressBar;
    private Label timeLbl;
    private Label totalTimeLbl;
    private Timeline timeline;
    private String lastLoadedTitle = "";
    private boolean userSeeking = false;
    private Runnable onQueueToggle;

    /**
     * Registers the callback that is invoked when the queue button is clicked.
     * @param r the action to run
     */
    public void setOnQueueToggle(Runnable r) { this.onQueueToggle = r; }


    /**
     * Builds the entire player bar UI and wires up all event handlers.
     * @param playerManager the audio engine to control
     * @param dataManager   the data store used to populate the playlist menu
     */
    public PlayerUI(PlayerManager playerManager, DataManager dataManager) {
        this.playerManager = playerManager;
        this.dataManager = dataManager;

        root = new HBox(0);
        root.getStyleClass().add("player");
        root.setAlignment(Pos.CENTER);

        // ── Left section: cover art + song info + queue toggle ────────────────
        HBox leftBox = new HBox(10);
        leftBox.setAlignment(Pos.CENTER_LEFT);
        leftBox.setMinWidth(280);
        leftBox.setPrefWidth(280);
        leftBox.setMaxWidth(280);
        leftBox.setStyle("-fx-padding: 0 0 0 16;");

        coverView = new ImageView();
        coverView.setFitWidth(56);
        coverView.setFitHeight(56);
        coverView.setPreserveRatio(true);

        VBox textsBox = new VBox(3);
        textsBox.setAlignment(Pos.CENTER_LEFT);
        songTitleLbl = new Label("Není vybrána skladba");
        songTitleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: white;");
        songTitleLbl.setMaxWidth(150);
        artistLbl = new Label("");
        artistLbl.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 11px;");
        textsBox.getChildren().addAll(songTitleLbl, artistLbl);

        Button queueBtn = new Button("☰");
        queueBtn.getStyleClass().add("player-btn");
        queueBtn.setStyle("-fx-font-size: 15px;");
        queueBtn.setOnAction(e -> { if (onQueueToggle != null) onQueueToggle.run(); });

        leftBox.getChildren().addAll(coverView, textsBox, queueBtn);

        // ── Centre section: transport controls + progress bar ─────────────────
        VBox centerBox = new VBox(6);
        centerBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(centerBox, Priority.ALWAYS);
        centerBox.setStyle("-fx-padding: 0 20;");

        // Transport buttons row
        HBox btnsBox = new HBox(16);
        btnsBox.setAlignment(Pos.CENTER);

        Button shuffleBtn = new Button("🔀");
        shuffleBtn.getStyleClass().add("player-btn");
        Button prevBtn = new Button("⏮");
        prevBtn.getStyleClass().add("player-btn");
        playBtn = new Button("▶");
        playBtn.getStyleClass().add("player-btn-play");
        Button nextBtn = new Button("⏭");
        nextBtn.getStyleClass().add("player-btn");
        Button repeatBtn = new Button("🔁");
        repeatBtn.getStyleClass().add("player-btn");
        btnsBox.getChildren().addAll(shuffleBtn, prevBtn, playBtn, nextBtn, repeatBtn);

        // Progress bar row with elapsed and total time labels
        HBox progressBox = new HBox(8);
        progressBox.setAlignment(Pos.CENTER);
        timeLbl = new Label("0:00");
        timeLbl.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 11px; -fx-min-width: 35;");
        progressBar = new Slider(0, 100, 0);
        HBox.setHgrow(progressBar, Priority.ALWAYS);
        progressBar.getStyleClass().add("progress-bar");
        totalTimeLbl = new Label("0:00");
        totalTimeLbl.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 11px; -fx-min-width: 35;");
        progressBox.getChildren().addAll(timeLbl, progressBar, totalTimeLbl);

        centerBox.getChildren().addAll(btnsBox, progressBox);

        // ── Right section: add-to-playlist + volume ───────────────────────────
        HBox rightBox = new HBox(10);
        rightBox.setAlignment(Pos.CENTER_RIGHT);
        rightBox.setMinWidth(280);
        rightBox.setPrefWidth(280);
        rightBox.setMaxWidth(280);
        rightBox.setStyle("-fx-padding: 0 16 0 0;");

        // "Add to playlist" button – shows a ContextMenu of available playlists
        Button addToPlaylistBtn = new Button("➕");
        addToPlaylistBtn.getStyleClass().add("player-btn");
        addToPlaylistBtn.setStyle("-fx-font-size: 15px;");
        addToPlaylistBtn.setOnAction(e -> {
            Song current = playerManager.getCurrentSong();
            if (current == null) return;

            // Build context menu entries for each existing playlist
            ContextMenu contextMenu = new ContextMenu();
            if (dataManager.getAllPlaylists().isEmpty()) {
                MenuItem emptyItem = new MenuItem("Žádné playlisty");
                emptyItem.setDisable(true);
                contextMenu.getItems().add(emptyItem);
            } else {
                for (Playlist p : dataManager.getAllPlaylists()) {
                    MenuItem item = new MenuItem("Přidat do: " + p.getName());
                    item.setOnAction(ev -> {
                        p.addSong(current);
                        // Persist immediately so the addition survives app restarts
                        DataLoader.savePlaylists(dataManager, "src/main/resources/data/playlists.csv");
                    });
                    contextMenu.getItems().add(item);
                }
            }
            contextMenu.show(addToPlaylistBtn, TOP, 0, 0);
        });

        Label volIcon = new Label("🔊");
        volIcon.setStyle("-fx-text-fill: #B3B3B3;");
        Slider volSlider = new Slider(0, 1, 0.5);
        volSlider.setPrefWidth(90);
        volSlider.setMinWidth(90);
        rightBox.getChildren().addAll(addToPlaylistBtn, volIcon, volSlider);

        root.getChildren().addAll(leftBox, centerBox, rightBox);

        // Play/pause toggle
        playBtn.setOnAction(e -> {
            if (playerManager.isPlaying()) {
                playerManager.pause();
                playBtn.setText("▶");
            } else {
                playerManager.playCurrent();
                playBtn.setText("⏸");
            }
        });

        // Next / previous; always show the pause icon because playback starts
        nextBtn.setOnAction(e -> { playerManager.next(); updateInfo(); playBtn.setText("⏸"); });
        prevBtn.setOnAction(e -> { playerManager.previous(); updateInfo(); playBtn.setText("⏸"); });

        // Shuffle: highlight the button in green while active
        shuffleBtn.setOnAction(e -> {
            playerManager.toggleShuffle();
            shuffleBtn.setStyle(playerManager.isShuffle() ? "-fx-text-fill: #1DB954;" : "");
        });

        // Repeat: cycle through modes and reflect state via button colour/weight
        repeatBtn.setOnAction(e -> {
            playerManager.toggleRepeat();
            int mode = playerManager.getRepeatMode();
            if (mode == 1) repeatBtn.setStyle("-fx-text-fill: #1DB954;");
            else if (mode == 2) repeatBtn.setStyle("-fx-text-fill: #1DB954; -fx-font-weight: bold;");
            else repeatBtn.setStyle("");
        });

        // Volume slider: forward changes to the player in real time
        volSlider.valueProperty().addListener((obs, o, n) -> playerManager.setVolume(n.doubleValue()));

        // Progress bar seeking: suppress polling while the thumb is being dragged
        progressBar.setOnMousePressed(e -> userSeeking = true);
        progressBar.setOnMouseReleased(e -> {
            playerManager.seek(progressBar.getValue());
            userSeeking = false;
        });

        // Fires every 300 ms to update the progress bar and detect auto-advances
        timeline = new Timeline(new KeyFrame(Duration.millis(300), e -> updateProgress()));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    /**
     * Refreshes all player bar labels and the cover image to reflect the currently-playing song.
     */
    public void updateInfo() {
        Song current = playerManager.getCurrentSong();
        if (current != null) {
            songTitleLbl.setText(current.getTitle());
            artistLbl.setText(current.getArtist() != null ? current.getArtist().getName() : "Neznámý");
            playBtn.setText("▶");
            loadCover(current.getCoverPath());
            lastLoadedTitle = current.getTitle();
            progressBar.setValue(0);
            progressBar.setMax(100);
            timeLbl.setText("0:00");
            totalTimeLbl.setText("0:00");
        }
    }

    /**
     * Loads and displays cover art from the given local file path.
     */
    private void loadCover(String coverPath) {
        if (coverPath == null || coverPath.isBlank()) { coverView.setImage(null); return; }
        try {
            File f = new File(coverPath);
            coverView.setImage(f.exists() ? new Image(f.toURI().toString(), 56, 56, true, true, true) : null);
        } catch (Exception ex) { coverView.setImage(null); }
    }

    /**
     * Polling callback executed every 300 ms by timeline.
     */
    private void updateProgress() {
        // ── 1. Detect auto-advance to a new song ─────────────────────────────
        Song current = playerManager.getCurrentSong();
        if (current != null && !current.getTitle().equals(lastLoadedTitle)) {
            updateInfo();
            return;
        }

        // ── 2. Keep play button icon in sync ──────────────────────────────────
        if (playerManager.isPlaying()) playBtn.setText("⏸");

        // ── 3. Update the total-duration label once per track ─────────────────
        double duration = playerManager.getDuration();
        if (duration > 0 && Math.abs(progressBar.getMax() - duration) > 0.5) {
            progressBar.setMax(duration);
            int dm = (int) duration / 60, ds = (int) duration % 60;
            totalTimeLbl.setText(dm + ":" + String.format("%02d", ds));
        }

        // ── 4. Update elapsed-time label and slider position ──────────────────
        if (!userSeeking) {
            double pos = playerManager.getCurrentPosition();
            if (pos >= 0) {
                progressBar.setValue(pos);
                int m = (int) pos / 60, s = (int) pos % 60;
                timeLbl.setText(m + ":" + String.format("%02d", s));
            }
        }
    }

    /**
     * Returns the root that should be placed at the bottom of the scene
     * @return the player bar root node
     */
    public HBox getRoot() {
        return root;
    }
}