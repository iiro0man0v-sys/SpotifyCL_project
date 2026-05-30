package ui;

import data.DataLoader;
import data.DataManager;
import audio.PlayerManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.Playlist;
import model.Song;

public class SpotifyCL extends Application {

    private DataManager dataManager;
    private PlayerManager playerManager;
    private PlayerUI playerUI;
    private ContentView contentView;
    private VBox playlistMenuBox;
    private VBox queuePanel;
    private ListView<Song> queueListView;
    private boolean queueVisible = false;

    @Override
    public void start(Stage primaryStage) {
        dataManager = new DataManager();
        DataLoader.loadData(dataManager, "src/main/resources/data/data.csv");
        DataLoader.loadPlaylists(dataManager, "src/main/resources/data/playlists.csv");

        playerManager = new PlayerManager();
        playerManager.setDataManager(dataManager);

        playerUI = new PlayerUI(playerManager, dataManager);
        contentView = new ContentView(dataManager, playerManager, playerUI);

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #000000;");


        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #000000;");

        Button homeBtn = new Button("🏠 Domů");
        homeBtn.getStyleClass().add("nav-btn");
        homeBtn.setOnAction(e -> contentView.showHome());

        TextField searchField = new TextField();
        searchField.setPromptText("Hledat...");
        searchField.getStyleClass().add("search-field");
        searchField.setOnAction(e -> {
            if (!searchField.getText().trim().isEmpty())
                contentView.showSearch(searchField.getText().trim());
        });

        Label libLbl = new Label("Tvoje knihovna");
        libLbl.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 20 0 10 0;");

        Button newPlaylistBtn = new Button("➕ Vytvořit playlist");
        newPlaylistBtn.getStyleClass().add("nav-btn");
        newPlaylistBtn.setOnAction(e -> {
            Playlist p = dataManager.createPlaylist("Nový playlist " + (dataManager.getAllPlaylists().size() + 1));
            DataLoader.savePlaylists(dataManager, "src/main/resources/data/playlists.csv");
            updatePlaylistMenu();
            contentView.showPlaylist(p);
        });

        playlistMenuBox = new VBox(10);
        updatePlaylistMenu();
        sidebar.getChildren().addAll(homeBtn, searchField, libLbl, newPlaylistBtn, playlistMenuBox);


        queuePanel = new VBox(10);
        queuePanel.setStyle("-fx-background-color: #181818; -fx-padding: 20;");
        queuePanel.setPrefWidth(280);
        queuePanel.setMinWidth(280);

        Label queueTitle = new Label("Fronta");
        queueTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        queueListView = new ListView<>();
        queueListView.setStyle("-fx-background-color: #181818; -fx-control-inner-background: #181818;");
        VBox.setVgrow(queueListView, Priority.ALWAYS);
        queueListView.setCellFactory(lv -> new javafx.scene.control.ListCell<Song>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                if (empty || song == null) { setText(null); }
                else {
                    setText(song.getTitle() + "\n" + (song.getArtist() != null ? song.getArtist().getName() : ""));
                    setStyle("-fx-text-fill: white; -fx-font-size: 12px;");
                }
            }
        });
        queueListView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                int idx = queueListView.getSelectionModel().getSelectedIndex();
                if (idx >= 0) {
                    playerManager.setCurrentIndex(idx);
                    playerManager.playSong(playerManager.getCurrentQueue().get(idx));
                    playerUI.updateInfo();
                }
            }
        });

        queuePanel.getChildren().addAll(queueTitle, queueListView);


        HBox centerArea = new HBox();
        HBox.setHgrow(contentView.getRoot(), Priority.ALWAYS);
        centerArea.getChildren().add(contentView.getRoot());


        playerUI.setOnQueueToggle(() -> {
            queueVisible = !queueVisible;
            if (queueVisible) {
                queueListView.getItems().setAll(playerManager.getCurrentQueue());
                int cur = playerManager.getCurrentIndex();
                if (cur >= 0) queueListView.scrollTo(cur);
                if (!centerArea.getChildren().contains(queuePanel))
                    centerArea.getChildren().add(queuePanel);
            } else {
                centerArea.getChildren().remove(queuePanel);
            }
        });

        root.setLeft(sidebar);
        root.setCenter(centerArea);
        root.setBottom(playerUI.getRoot());

        contentView.showHome();

        Scene scene = new Scene(root, 1200, 800);
        try {
            String cssUrl = getClass().getResource("/css/style.css").toExternalForm();
            scene.getStylesheets().add(cssUrl);
        } catch (Exception e) {
            System.err.println("CSS file not found!");
        }

        primaryStage.setTitle("Spotify Clone");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void updatePlaylistMenu() {
        playlistMenuBox.getChildren().clear();
        for (Playlist p : dataManager.getAllPlaylists()) {
            Button pBtn = new Button(p.getName());
            pBtn.getStyleClass().add("nav-btn");
            pBtn.setMaxWidth(Double.MAX_VALUE);
            pBtn.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) startRename(p, pBtn);
                else contentView.showPlaylist(p);
            });
            pBtn.setOnAction(null);
            playlistMenuBox.getChildren().add(pBtn);
        }
    }

    private void startRename(Playlist playlist, Button btn) {
        int idx = playlistMenuBox.getChildren().indexOf(btn);
        if (idx < 0) return;
        TextField tf = new TextField(playlist.getName());
        tf.getStyleClass().add("search-field");
        tf.setMaxWidth(Double.MAX_VALUE);
        Runnable commit = () -> {
            String name = tf.getText().trim();
            if (!name.isEmpty()) {
                playlist.setName(name);
                DataLoader.savePlaylists(dataManager, "src/main/resources/data/playlists.csv");
            }
            updatePlaylistMenu();
        };
        tf.setOnAction(e -> commit.run());
        tf.focusedProperty().addListener((obs, was, now) -> { if (!now) commit.run(); });
        playlistMenuBox.getChildren().set(idx, tf);
        tf.requestFocus();
        tf.selectAll();
    }

    public static void main(String[] args) { launch(args); }
}