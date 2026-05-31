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

/**
 * Application entry point and window controller
 */
public class SpotifyCL extends Application {
    private DataManager dataManager;
    private PlayerManager playerManager;
    private PlayerUI playerUI;
    private ContentView contentView;
    private VBox playlistMenuBox;
    private VBox queuePanel;
    private ListView<Song> queueListView;
    private boolean queueVisible = false;

    /**
     * Initialises and displays the primary application window.
     * @param primaryStage the stage provided by the JavaFX runtime
     */
    @Override
    public void start(Stage primaryStage) {

        // ── 1. Data layer ─────────────────────────────────────────────────────
        dataManager = new DataManager();
        DataLoader.loadData(dataManager, "src/main/resources/data/data.csv");
        DataLoader.loadPlaylists(dataManager, "src/main/resources/data/playlists.csv");

        // ── 2. Audio layer ────────────────────────────────────────────────────
        playerManager = new PlayerManager();
        playerManager.setDataManager(dataManager);

        // ── 3. UI components ──────────────────────────────────────────────────
        playerUI = new PlayerUI(playerManager, dataManager);
        contentView = new ContentView(dataManager, playerManager, playerUI);

        // ── 4. Root layout ────────────────────────────────────────────────────
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #000000;");

        // ── 5. Sidebar ────────────────────────────────────────────────────────
        VBox sidebar = new VBox(20);
        sidebar.setPadding(new Insets(20));
        sidebar.setPrefWidth(220);
        sidebar.setStyle("-fx-background-color: #000000;");

        // Home navigation button
        Button homeBtn = new Button("🏠 Domů");
        homeBtn.getStyleClass().add("nav-btn");
        homeBtn.setOnAction(e -> contentView.showHome());

        // Search field: triggers search on Enter key
        TextField searchField = new TextField();
        searchField.setPromptText("Hledat...");
        searchField.getStyleClass().add("search-field");
        searchField.setOnAction(e -> {
            if (!searchField.getText().trim().isEmpty())
                contentView.showSearch(searchField.getText().trim());
        });

        Label libLbl = new Label("Tvoje knihovna");
        libLbl.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 20 0 10 0;");

        // "New playlist" button: creates a playlist, persists it, and navigates to it
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

        // ── 6. Queue side panel ───────────────────────────────────────────────
        queuePanel = new VBox(10);
        queuePanel.setStyle("-fx-background-color: #181818; -fx-padding: 20;");
        queuePanel.setPrefWidth(280);
        queuePanel.setMinWidth(280);

        Label queueTitle = new Label("Fronta");
        queueTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        // Custom list view cell: shows title on the first line, artist on the second
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

        // Double-click a queue entry: jump directly to that song
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

        // ── 7. Centre area (content + optional queue panel) ───────────────────
        HBox centerArea = new HBox();
        HBox.setHgrow(contentView.getRoot(), Priority.ALWAYS);
        centerArea.getChildren().add(contentView.getRoot());

        // Wire the queue toggle: show/hide the queue panel to the right of the content
        playerUI.setOnQueueToggle(() -> {
            queueVisible = !queueVisible;
            if (queueVisible) {
                // Populate and scroll to the current song before showing
                queueListView.getItems().setAll(playerManager.getCurrentQueue());
                int cur = playerManager.getCurrentIndex();
                if (cur >= 0) queueListView.scrollTo(cur);
                if (!centerArea.getChildren().contains(queuePanel))
                    centerArea.getChildren().add(queuePanel);
            } else {
                centerArea.getChildren().remove(queuePanel);
            }
        });

        // ── 8. Assemble scene ─────────────────────────────────────────────────
        root.setLeft(sidebar);
        root.setCenter(centerArea);
        root.setBottom(playerUI.getRoot());

        // Show the home screen
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

    /**
     * Rebuilds the playlist button list in the sidebar
     */
    private void updatePlaylistMenu() {
        playlistMenuBox.getChildren().clear();
        for (Playlist p : dataManager.getAllPlaylists()) {
            Button pBtn = new Button(p.getName());
            pBtn.getStyleClass().add("nav-btn");
            pBtn.setMaxWidth(Double.MAX_VALUE);

            // Single click: open the playlist; double click: enter rename mode
            pBtn.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2) startRename(p, pBtn);
                else contentView.showPlaylist(p);
            });
            pBtn.setOnAction(null); // prevent action event from firing on click
            playlistMenuBox.getChildren().add(pBtn);
        }
    }

    /**
     * Replaces a playlist button in the sidebar with an editable text field
     * so the user can rename the playlist inline.
     * @param playlist the playlist whose name is being changed
     * @param btn      the sidebar button currently representing the playlist;
     */
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
        // Commit on Enter key or when focus leaves the field
        tf.setOnAction(e -> commit.run());
        tf.focusedProperty().addListener((obs, was, now) -> { if (!now) commit.run(); });
        playlistMenuBox.getChildren().set(idx, tf);
        tf.requestFocus();
        tf.selectAll();
    }

    public static void main(String[] args) { launch(args); }
}