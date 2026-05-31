package ui;

import data.DataManager;
import audio.PlayerManager;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import model.Album;
import model.Artist;
import model.Playlist;
import model.Song;
import javafx.scene.paint.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import static javafx.geometry.Pos.CENTER_LEFT;

/**
 * Main content area of the application.
 */
public class ContentView {
    private ScrollPane root;
    private VBox contentBox;
    private DataManager dataManager;
    private PlayerManager playerManager;
    private PlayerUI playerUI;

    /**
     * Constructs the content view and initialises the scroll pane wrapper.
     */
    public ContentView(DataManager dataManager, PlayerManager playerManager, PlayerUI playerUI) {
        this.dataManager = dataManager;
        this.playerManager = playerManager;
        this.playerUI = playerUI;

        contentBox = new VBox(20);
        contentBox.getStyleClass().add("content-area");
        contentBox.setStyle("-fx-background-color: #121212;");

        root = new ScrollPane(contentBox);
        root.setFitToWidth(true);
        root.setStyle("-fx-background: #121212; -fx-border-color: #121212;");
    }

    /**
     * Filters a list of songs to those whose audio file is present on disk.
     */
    private ArrayList<Song> filterAvailable(ArrayList<Song> songs) {
        return songs.stream()
                .filter(s -> s.getFilePath() != null && new File(s.getFilePath()).exists())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * Loads an image from a local file path into a JavaFX
     */
    private Image loadImage(String path, double size) {
        if (path == null || path.isBlank()) return null;
        try {
            File f = new File(path);
            if (f.exists()) return new Image(f.toURI().toString(), size, size, true, true, true);
        } catch (Exception ignored) {}
        return null;
    }

    /**
     * @return the scrollable content container
     */
    public ScrollPane getRoot() {
        return root;
    }

    /**
     * Displays the home screen.
     */
    public void showHome() {
        contentBox.getChildren().clear();

        // ── Section header ────────────────────────────────────────────────────
        Label title = new Label("Domů");
        title.getStyleClass().add("section-title");

        // ── Recently Added row ────────────────────────────────────────────────
        Label recentTitle = new Label("Naposledy přidané");
        recentTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox recentBox = new HBox(15);
        for (Object obj : dataManager.getRecentlyAdded()) {
            if (obj instanceof Album) {
                Album album = (Album) obj;
                recentBox.getChildren().add(makeAlbumCard(album));
            } else if (obj instanceof Song) {
                Song song = (Song) obj;
                // Skip songs whose file is missing
                if (song.getFilePath() == null || !new File(song.getFilePath()).exists()) continue;
                recentBox.getChildren().add(makeSongCard(song));
            }
        }

        // ── Recently Played row ───────────────────────────────────────────────
        Label playedTitle = new Label("Naposledy přehrávané");
        playedTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox playedBox = new HBox(15);
        for (Song song : dataManager.getRecentlyPlayed()) {
            if (song.getFilePath() == null || !new File(song.getFilePath()).exists()) continue;
            playedBox.getChildren().add(makeSongCard(song));
        }

        contentBox.getChildren().addAll(title, playedTitle, playedBox, recentTitle, recentBox);
    }

    /**
     * Creates a clickable card UI element representing an album.
     * @param album the album to represent
     */
    private VBox makeAlbumCard(Album album) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");
        card.setPrefWidth(160);

        // Cover art thumbnail
        ImageView iv = new ImageView();
        iv.setFitWidth(130);
        iv.setFitHeight(130);
        iv.setPreserveRatio(true);
        Image img = loadImage(album.getCoverPath(), 130);
        if (img != null) iv.setImage(img);

        Label name = new Label(album.getTitle());
        name.getStyleClass().add("card-title");
        name.setWrapText(true);
        Label sub = new Label("Album");
        sub.getStyleClass().add("card-subtitle");
        card.getChildren().addAll(iv, name, sub);

        // Single-click opens the album detail view
        card.setOnMouseClicked(e -> showAlbum(album));
        return card;
    }

    /**
     * Creates a clickable card UI element representing a single song.
     * @param song the song to represent
     */
    private VBox makeSongCard(Song song) {
        VBox card = new VBox(6);
        card.getStyleClass().add("card");
        card.setPrefWidth(160);

        // Cover art thumbnail
        ImageView iv = new ImageView();
        iv.setFitWidth(130);
        iv.setFitHeight(130);
        iv.setPreserveRatio(true);
        Image img = loadImage(song.getCoverPath(), 130);
        if (img != null) iv.setImage(img);

        Label name = new Label(song.getTitle());
        name.getStyleClass().add("card-title");
        name.setWrapText(true);
        Label sub = new Label("Skladba");
        sub.getStyleClass().add("card-subtitle");
        card.getChildren().addAll(iv, name, sub);

        // Click: create a single-song queue and play it
        card.setOnMouseClicked(e -> {
            ArrayList<Song> q = new ArrayList<>();
            q.add(song);
            playerManager.setQueue(q, 0);
            playerUI.updateInfo();
        });
        return card;
    }

    /**
     * Displays search results for the given query string
     * @param query the search string entered by the user
     */
    public void showSearch(String query) {
        contentBox.getChildren().clear();

        Label title = new Label("Výsledky pro: " + query);
        title.getStyleClass().add("section-title");
        contentBox.getChildren().add(title);

        // Retrieve and filter results from the data layer
        ArrayList<Song> songs = filterAvailable(dataManager.searchSongs(query));
        ArrayList<Album> albums = dataManager.searchAlbums(query);
        ArrayList<Artist> artists = dataManager.searchArtists(query);

        // ── Songs section ─────────────────────────────────────────────────────
        if (!songs.isEmpty()) {
            Label lbl = new Label("Skladby");
            lbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0; -fx-text-fill: white;");
            contentBox.getChildren().add(lbl);
            for (int i = 0; i < songs.size(); i++) {
                contentBox.getChildren().add(createSongRow(songs.get(i), songs, i));
            }
        }

        // ── Albums section ────────────────────────────────────────────────────
        if (!albums.isEmpty()) {
            Label lbl = new Label("Alba");
            lbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0; -fx-text-fill: white;");
            contentBox.getChildren().add(lbl);
            HBox box = new HBox(15);
            for (Album a : albums) box.getChildren().add(makeAlbumCard(a));
            contentBox.getChildren().add(box);
        }

        // ── Artists section ───────────────────────────────────────────────────
        if (!artists.isEmpty()) {
            Label lbl = new Label("Interpreti");
            lbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0; -fx-text-fill: white;");
            contentBox.getChildren().add(lbl);
            HBox box = new HBox(15);
            for (Artist a : artists) {
                VBox card = new VBox(5);
                card.getStyleClass().add("card");
                card.setPrefWidth(160);
                Label name = new Label(a.getName());
                name.getStyleClass().add("card-title");
                card.getChildren().add(name);
                // Click navigates to the full artist profile
                card.setOnMouseClicked(e -> showArtistProfile(a));
                box.getChildren().add(card);
            }
            contentBox.getChildren().add(box);
        }
    }

    /**
     * Displays the contents of a user playlist with drag-and-drop reordering.
     * @param playlist the playlist to display
     */
    public void showPlaylist(Playlist playlist) {
        contentBox.getChildren().clear();

        Label title = new Label(playlist.getName());
        title.getStyleClass().add("section-title");

        // ── Song list with custom cells ───────────────────────────────────────
        ListView<Song> listView = new ListView<>();
        listView.getItems().addAll(playlist.getSongs());
        listView.setPrefHeight(400);
        listView.setStyle("-fx-background-color: #121212; -fx-control-inner-background: #121212;");

        listView.setCellFactory(lv -> {
            // Custom cell: thumbnail + title + artist, with dim style for unavailable songs
            ListCell<Song> cell = new ListCell<>() {
                private final ImageView thumb = new ImageView();
                private final Label titleLbl = new Label();
                private final Label artistLbl = new Label();
                private final HBox cellRoot = new HBox(10);
                {
                    thumb.setFitWidth(40);
                    thumb.setFitHeight(40);
                    thumb.setPreserveRatio(true);
                    VBox texts = new VBox(2, titleLbl, artistLbl);
                    texts.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    cellRoot.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    cellRoot.getChildren().addAll(thumb, texts);
                    artistLbl.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 11px;");
                }

                /**
                 * Songs without an existing file are shown in a dimmed colour.
                 */
                @Override
                protected void updateItem(Song song, boolean empty) {
                    super.updateItem(song, empty);
                    if (empty || song == null) {
                        setGraphic(null);
                    } else {
                        // Dim unavailable songs so the user knows they cannot be played
                        boolean available = song.getFilePath() != null && new File(song.getFilePath()).exists();
                        titleLbl.setText(song.getTitle());
                        titleLbl.setTextFill(available ? Color.WHITE : Color.web("#555555"));
                        artistLbl.setText(song.getArtist() != null ? song.getArtist().getName() : "");
                        Image img = loadImage(song.getCoverPath(), 40);
                        thumb.setImage(img);
                        setGraphic(cellRoot);
                    }
                }
            };

            // ── Drag-and-drop: initiate drag from a non-empty cell ────────────
            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty()) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent cc = new ClipboardContent();
                    // Store the source index as a string on the drag board
                    cc.putString(String.valueOf(cell.getIndex()));
                    db.setContent(cc);
                    event.consume();
                }
            });

            // ── Drag-and-drop: accept incoming drags ──────────────────────────
            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.MOVE);
                }
                event.consume();
            });

            // ── Drag-and-drop: perform the reorder on drop ────────────────────
            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    int draggedIdx = Integer.parseInt(db.getString());
                    // Drop at the end of the list when the target cell is empty
                    int thisIdx = cell.isEmpty() ? listView.getItems().size() : cell.getIndex();
                    Song draggedSong = listView.getItems().remove(draggedIdx);
                    if (thisIdx > listView.getItems().size()) thisIdx = listView.getItems().size();
                    listView.getItems().add(thisIdx, draggedSong);

                    // Sync the underlying playlist model and persist the new order
                    playlist.getSongs().clear();
                    playlist.getSongs().addAll(listView.getItems());
                    data.DataLoader.savePlaylists(dataManager, "src/main/resources/data/playlists.csv");
                    success = true;
                }
                event.setDropCompleted(success);
                event.consume();
            });

            return cell;
        });

        // ── Double-click to start playback from the selected song ─────────────
        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
                Song selected = listView.getSelectionModel().getSelectedItem();
                if (selected.getFilePath() != null && new File(selected.getFilePath()).exists()) {
                    // Build queue from only available songs; find the selected song's index within it
                    ArrayList<Song> available = filterAvailable(new ArrayList<>(playlist.getSongs()));
                    int availIdx = available.indexOf(selected);
                    if (availIdx >= 0) {
                        playerManager.setQueue(available, availIdx);
                        playerUI.updateInfo();
                    }
                }
            }
        });

        // ── "Play All" button ─────────────────────────────────────────────────
        Button playAll = new Button("Přehrát vše");
        playAll.getStyleClass().add("player-btn-play");
        playAll.setStyle("-fx-padding: 10 20; -fx-background-radius: 20;");
        playAll.setOnAction(e -> {
            ArrayList<Song> available = filterAvailable(new ArrayList<>(playlist.getSongs()));
            if (!available.isEmpty()) {
                playerManager.setQueue(available, 0);
                playerUI.updateInfo();
            }
        });

        contentBox.getChildren().addAll(title, playAll, listView);
    }

    /**
     * Displays the detail view for a single album.
     * @param album the album whose detail page should be rendered
     */
    public void showAlbum(Album album) {
        contentBox.getChildren().clear();

        // ── Header: cover art + metadata ──────────────────────────────────────
        HBox header = new HBox(20);
        header.setAlignment(CENTER_LEFT);
        header.setStyle("-fx-padding: 0 0 20 0;");

        ImageView albumCover = new ImageView();
        albumCover.setFitWidth(180);
        albumCover.setFitHeight(180);
        albumCover.setPreserveRatio(true);
        albumCover.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 16, 0, 0, 4);");
        Image img = loadImage(album.getCoverPath(), 180);
        if (img != null) albumCover.setImage(img);

        // Metadata labels stacked vertically
        VBox albumInfo = new VBox(8);
        albumInfo.setAlignment(javafx.geometry.Pos.BOTTOM_LEFT);
        Label typeLabel = new Label("ALBUM");
        typeLabel.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 11px;");
        Label titleLbl = new Label(album.getTitle());
        titleLbl.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");
        Label artistLbl = new Label(album.getArtist() != null ? album.getArtist().getName() : "");
        artistLbl.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 14px;");
        Label yearLbl = new Label(String.valueOf(album.getYear()));
        yearLbl.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 13px;");
        albumInfo.getChildren().addAll(typeLabel, titleLbl, artistLbl, yearLbl);
        header.getChildren().addAll(albumCover, albumInfo);
        ArrayList<Song> available = filterAvailable(album.getSongs());

        // ── "Play All" button ─────────────────────────────────────────────────
        Button playAll = new Button("▶  Přehrát vše");
        playAll.getStyleClass().add("player-btn-play");
        playAll.setStyle("-fx-padding: 12 28; -fx-background-radius: 24; -fx-font-size: 14px;");
        playAll.setOnAction(e -> {
            if (!available.isEmpty()) {
                playerManager.setQueue(new ArrayList<>(available), 0);
                playerUI.updateInfo();
            }
        });

        // ── Track list ────────────────────────────────────────────────────────
        contentBox.getChildren().addAll(header, playAll);
        for (int i = 0; i < available.size(); i++) {
            contentBox.getChildren().add(createSongRow(available.get(i), available, i));
        }
    }

    /**
     * Displays the artist profile page.
     * @param artist the artist whose profile page should be rendered
     */
    public void showArtistProfile(Artist artist) {
        contentBox.getChildren().clear();

        // ── Header: 200×200 photo card + name/bio to the right ───────────────
        HBox header = new HBox(24);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        header.setStyle("-fx-padding: 24 30 16 30;");

        // 200×200 squared photo
        StackPane photoFrame = new StackPane();
        photoFrame.setMinSize(200, 200);
        photoFrame.setMaxSize(200, 200);
        photoFrame.setPrefSize(200, 200);
        photoFrame.setStyle("-fx-background-color: #282828; -fx-border-color: #3a3a3a; -fx-border-width: 1;");

        Image photoImg = loadImage(artist.getPhotoPath(), 200);
        if (photoImg != null) {
            ImageView photoView = new ImageView(photoImg);
            photoView.setFitWidth(200);
            photoView.setFitHeight(200);
            photoView.setPreserveRatio(false);
            photoFrame.getChildren().add(photoView);
        } else {
            // Placeholder label centred inside the grey frame
            Label placeholder = new Label("No photo");
            placeholder.setStyle("-fx-text-fill: #555555; -fx-font-size: 12px;");
            photoFrame.getChildren().add(placeholder);
        }

        // Artist name and bio stacked vertically to the right of the photo
        VBox infoBox = new VBox(10);
        infoBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nameLabel = new Label(artist.getName());
        nameLabel.setStyle("-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label bio = new Label(artist.getBio());
        bio.setWrapText(true);
        bio.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 13px;");

        infoBox.getChildren().addAll(nameLabel, bio);
        header.getChildren().addAll(photoFrame, infoBox);

        Label albumsTitle = new Label("Alba");
        albumsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; " +
                "-fx-padding: 8 0 10 30; -fx-text-fill: white;");

        HBox albumsBox = new HBox(15);
        albumsBox.setStyle("-fx-padding: 0 30;");
        for (Album a : artist.getAlbums()) {
            albumsBox.getChildren().add(makeAlbumCard(a));
        }

        contentBox.getChildren().addAll(header, albumsTitle, albumsBox);
    }

    /**
     * Creates a single track row
     * @param song  the song this row represents
     * @param queue the full list of songs to use as the playback queue
     * @param index the position of {@code song} within {@code queue} (0-based)
     * @return a styled {@link HBox} track row
     */
    private HBox createSongRow(Song song, ArrayList<Song> queue, int index) {
        HBox row = new HBox(12);
        row.getStyleClass().add("track-row");
        row.setAlignment(CENTER_LEFT);

        // Track number
        Label num = new Label(String.valueOf(index + 1));
        num.setPrefWidth(28);
        num.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 13px;");

        // 40×40 cover art thumbnail
        ImageView thumb = new ImageView();
        thumb.setFitWidth(40);
        thumb.setFitHeight(40);
        thumb.setPreserveRatio(true);
        Image img = loadImage(song.getCoverPath(), 40);
        if (img != null) thumb.setImage(img);

        // Title + artist stacked vertically
        VBox texts = new VBox(2);
        Label titleLbl = new Label(song.getTitle());
        titleLbl.getStyleClass().add("track-title");
        Label artistLbl = new Label(song.getArtist() != null ? song.getArtist().getName() : "");
        artistLbl.getStyleClass().add("track-artist");
        texts.getChildren().addAll(titleLbl, artistLbl);

        // Flexible space pushes the duration to the right edge
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label time = new Label(song.getFormattedDuration());
        time.setStyle("-fx-text-fill: #B3B3B3; -fx-font-size: 13px;");

        row.getChildren().addAll(num, thumb, texts, spacer, time);

        // Double-click: set the queue at this index and refresh the player bar
        row.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                playerManager.setQueue(queue, index);
                playerUI.updateInfo();
            }
        });
        return row;
    }
}