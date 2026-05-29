package ui;

import audio.PlayerManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.Song;


public class PlayerUI {
    private HBox root;
    private PlayerManager playerManager;
    private Label songTitleLbl;
    private Label artistLbl;
    private ImageView coverView;
    private Button playBtn;
    private Slider progressBar;
    private Label timeLbl;
    private Label totalTimeLbl;
    private Timeline timeline;

    public PlayerUI(PlayerManager playerManager) {
        this.playerManager = playerManager;
        
        root = new HBox();
        root.getStyleClass().add("player");
        root.setAlignment(Pos.CENTER);
        root.setSpacing(20);
        
        // --- Left: Song Info ---
        HBox songInfoBox = new HBox(15);
        songInfoBox.setAlignment(Pos.CENTER_LEFT);
        songInfoBox.setPrefWidth(250);
        
        coverView = new ImageView();
        coverView.setFitWidth(56);
        coverView.setFitHeight(56);
        coverView.setPreserveRatio(true);
        
        VBox textsBox = new VBox(5);
        textsBox.setAlignment(Pos.CENTER_LEFT);
        songTitleLbl = new Label("Není vybrána skladba");
        songTitleLbl.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        artistLbl = new Label("");
        artistLbl.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 12px;");
        textsBox.getChildren().addAll(songTitleLbl, artistLbl);
        
        songInfoBox.getChildren().addAll(coverView, textsBox);
        
        // --- Center: Controls ---
        VBox centerBox = new VBox(5);
        centerBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(centerBox, Priority.ALWAYS);
        
        HBox btnsBox = new HBox(15);
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
        
        HBox progressBox = new HBox(10);
        progressBox.setAlignment(Pos.CENTER);
        timeLbl = new Label("0:00");
        timeLbl.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 11px;");
        progressBar = new Slider(0, 100, 0);
        progressBar.setPrefWidth(400);
        progressBar.getStyleClass().add("progress-bar");
        totalTimeLbl = new Label("0:00");
        totalTimeLbl.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 11px;");
        progressBox.getChildren().addAll(timeLbl, progressBar, totalTimeLbl);
        
        centerBox.getChildren().addAll(btnsBox, progressBox);
        
        // --- Right: Volume ---
        HBox volBox = new HBox(10);
        volBox.setAlignment(Pos.CENTER_RIGHT);
        volBox.setPrefWidth(250);
        Label volIcon = new Label("🔊");
        volIcon.setStyle("-fx-text-fill: #B3B3B3;");
        Slider volSlider = new Slider(0, 1, 0.5);
        volSlider.setPrefWidth(100);
        volBox.getChildren().addAll(volIcon, volSlider);
        
        root.getChildren().addAll(songInfoBox, centerBox, volBox);
        
        // --- Actions ---
        playBtn.setOnAction(e -> {
            if (playerManager.getCurrentPosition() > 0 && isPlaying()) {
                playerManager.pause();
                playBtn.setText("▶");
            } else {
                playerManager.playCurrent();
                playBtn.setText("⏸");
            }
            updateInfo();
        });
        
        nextBtn.setOnAction(e -> {
            playerManager.next();
            updateInfo();
            playBtn.setText("⏸");
        });
        
        prevBtn.setOnAction(e -> {
            playerManager.previous();
            updateInfo();
            playBtn.setText("⏸");
        });
        
        shuffleBtn.setOnAction(e -> {
            playerManager.toggleShuffle();
            if (playerManager.isShuffle()) {
                shuffleBtn.setStyle("-fx-text-fill: #1DB954;");
            } else {
                shuffleBtn.setStyle("");
            }
        });
        
        repeatBtn.setOnAction(e -> {
            playerManager.toggleRepeat();
            int mode = playerManager.getRepeatMode();
            if (mode == 1) repeatBtn.setStyle("-fx-text-fill: #1DB954;");
            else if (mode == 2) repeatBtn.setStyle("-fx-text-fill: #1DB954; -fx-font-weight: bold;");
            else repeatBtn.setStyle("");
        });
        
        volSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            playerManager.setVolume(newVal.doubleValue());
        });
        
        progressBar.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (progressBar.isValueChanging()) {
                playerManager.seek(newVal.doubleValue());
            }
        });
        
        // Timeline for updating progress
        timeline = new Timeline(new KeyFrame(Duration.millis(500), e -> {
            updateProgress();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }
    
    private boolean isPlaying() {
        return playBtn.getText().equals("⏸");
    }

    public void updateInfo() {
        Song current = playerManager.getCurrentSong();
        if (current != null) {
            songTitleLbl.setText(current.getTitle());
            artistLbl.setText(current.getArtist() != null ? current.getArtist().getName() : "Neznámý");
            progressBar.setMax(current.getDuration());
            totalTimeLbl.setText(current.getFormattedDuration());
            playBtn.setText("⏸");
        }
    }
    
    private void updateProgress() {
        double pos = playerManager.getCurrentPosition();
        if (!progressBar.isValueChanging() && pos > 0) {
            progressBar.setValue(pos);
            int m = (int) pos / 60;
            int s = (int) pos % 60;
            timeLbl.setText(m + ":" + String.format("%02d", s));
            
            // Hack to detect next song start natively
            if (playerManager.getCurrentSong() != null) {
                if (!songTitleLbl.getText().equals(playerManager.getCurrentSong().getTitle())) {
                    updateInfo();
                }
            }
        }
    }
    
    public HBox getRoot() {
        return root;
    }
}
