package ui;

import data.DataManager;
import audio.PlayerManager;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.Album;
import model.Artist;
import model.Playlist;
import model.Song;
import javafx.scene.paint.Color;
import java.util.ArrayList;


import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.geometry.Side.BOTTOM;

public class ContentView {
    private ScrollPane root;
    private VBox contentBox;
    private DataManager dataManager;
    private PlayerManager playerManager;
    private PlayerUI playerUI;

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

    public ScrollPane getRoot() {
        return root;
    }

    public void showHome() {
        contentBox.getChildren().clear();

        Label title = new Label("Domů");
        title.getStyleClass().add("section-title");

        Label recentTitle = new Label("Naposledy přidané");
        recentTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        HBox recentBox = new HBox(15);
        for (Object obj : dataManager.getRecentlyAdded()) {
            VBox card = new VBox(5);
            card.getStyleClass().add("card");
            card.setPrefWidth(150);
            
            if (obj instanceof Album) {
                Album album = (Album) obj;
                Label name = new Label(album.getTitle());
                name.getStyleClass().add("card-title");
                Label sub = new Label("Album");
                sub.getStyleClass().add("card-subtitle");
                card.getChildren().addAll(name, sub);
                card.setOnMouseClicked(e -> showAlbum(album));
            } else if (obj instanceof Song) {
                Song song = (Song) obj;
                Label name = new Label(song.getTitle());
                name.getStyleClass().add("card-title");
                Label sub = new Label("Skladba");
                sub.getStyleClass().add("card-subtitle");
                card.getChildren().addAll(name, sub);
                card.setOnMouseClicked(e -> {
                    ArrayList<Song> q = new ArrayList<>();
                    q.add(song);
                    playerManager.setQueue(q, 0);
                    playerUI.updateInfo();
                });
            }
            recentBox.getChildren().add(card);
        }

        contentBox.getChildren().addAll(title, recentTitle, recentBox);
    }

    public void showSearch(String query) {
        contentBox.getChildren().clear();
        Label title = new Label("Výsledky pro: " + query);
        title.getStyleClass().add("section-title");
        contentBox.getChildren().add(title);

        ArrayList<Song> songs = dataManager.searchSongs(query);
        ArrayList<Album> albums = dataManager.searchAlbums(query);
        ArrayList<Artist> artists = dataManager.searchArtists(query);

        if (!songs.isEmpty()) {
            Label lbl = new Label("Skladby");
            lbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0;");
            contentBox.getChildren().add(lbl);
            for (int i = 0; i < songs.size(); i++) {
                Song s = songs.get(i);
                HBox row = createSongRow(s, songs, i);
                contentBox.getChildren().add(row);
            }
        }

        if (!albums.isEmpty()) {
            Label lbl = new Label("Alba");
            lbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0;");
            contentBox.getChildren().add(lbl);
            HBox box = new HBox(15);
            for (Album a : albums) {
                VBox card = new VBox(5);
                card.getStyleClass().add("card");
                card.setPrefWidth(150);
                Label name = new Label(a.getTitle());
                name.getStyleClass().add("card-title");
                card.getChildren().add(name);
                card.setOnMouseClicked(e -> showAlbum(a));
                box.getChildren().add(card);
            }
            contentBox.getChildren().add(box);
        }

        if (!artists.isEmpty()) {
            Label lbl = new Label("Interpreti");
            lbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 10 0;");
            contentBox.getChildren().add(lbl);
            HBox box = new HBox(15);
            for (Artist a : artists) {
                VBox card = new VBox(5);
                card.getStyleClass().add("card");
                card.setPrefWidth(150);
                Label name = new Label(a.getName());
                name.getStyleClass().add("card-title");
                card.getChildren().add(name);
                card.setOnMouseClicked(e -> showArtistProfile(a));
                box.getChildren().add(card);
            }
            contentBox.getChildren().add(box);
        }
    }

    public void showPlaylist(Playlist playlist) {
        contentBox.getChildren().clear();
        Label title = new Label(playlist.getName());
        title.getStyleClass().add("section-title");

        ListView<Song> listView = new ListView<>();
        listView.getItems().addAll(playlist.getSongs());
        listView.setPrefHeight(400);
        listView.setStyle("-fx-background-color: #121212; -fx-control-inner-background: #121212;");
        
        listView.setCellFactory(lv -> new ListCell<Song>() {
            @Override
            protected void updateItem(Song song, boolean empty) {
                super.updateItem(song, empty);
                if (empty || song == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(song.getTitle() + " - " + (song.getArtist() != null ? song.getArtist().getName() : ""));
                    setTextFill(Color.WHITE);
                }
            }
        });

        listView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
                int idx = listView.getSelectionModel().getSelectedIndex();
                playerManager.setQueue(new ArrayList<>(playlist.getSongs()), idx);
                playerUI.updateInfo();
            }
        });


        Button playAll = new Button("Přehrát vše");
        playAll.getStyleClass().add("player-btn-play");
        playAll.setStyle("-fx-padding: 10 20; -fx-background-radius: 20;");
        playAll.setOnAction(e -> {
            if (!playlist.getSongs().isEmpty()) {
                playerManager.setQueue(new ArrayList<>(playlist.getSongs()), 0);
                playerUI.updateInfo();
            }
        });

        contentBox.getChildren().addAll(title, playAll, listView);
    }

    public void showAlbum(Album album) {
        contentBox.getChildren().clear();
        Label title = new Label(album.getTitle());
        title.getStyleClass().add("section-title");
        
        Button playAll = new Button("Přehrát vše");
        playAll.getStyleClass().add("player-btn-play");
        playAll.setStyle("-fx-padding: 10 20; -fx-background-radius: 20;");
        playAll.setOnAction(e -> {
            if (!album.getSongs().isEmpty()) {
                playerManager.setQueue(new ArrayList<>(album.getSongs()), 0);
                playerUI.updateInfo();
            }
        });

        contentBox.getChildren().addAll(title, playAll);

        ArrayList<Song> songs = album.getSongs();
        for (int i = 0; i < songs.size(); i++) {
            contentBox.getChildren().add(createSongRow(songs.get(i), songs, i));
        }
    }

    public void showArtistProfile(Artist artist) {
        contentBox.getChildren().clear();
        Label title = new Label(artist.getName());
        title.setStyle("-fx-font-size: 48px; -fx-font-weight: bold;");
        
        Label bio = new Label(artist.getBio());
        bio.setWrapText(true);
        bio.setStyle("-fx-text-fill: #B3B3B3;");

        Label albumsTitle = new Label("Alba");
        albumsTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 20 0 10 0;");
        
        HBox albumsBox = new HBox(15);
        for (Album a : artist.getAlbums()) {
            VBox card = new VBox(5);
            card.getStyleClass().add("card");
            card.setPrefWidth(150);
            Label name = new Label(a.getTitle());
            name.getStyleClass().add("card-title");
            card.getChildren().add(name);
            card.setOnMouseClicked(e -> showAlbum(a));
            albumsBox.getChildren().add(card);
        }

        contentBox.getChildren().addAll(title, bio, albumsTitle, albumsBox);
    }

    private HBox createSongRow(Song song, ArrayList<Song> queue, int index) {
        HBox row = new HBox(15);
        row.getStyleClass().add("track-row");
        row.setAlignment(CENTER_LEFT);

        Label num = new Label(String.valueOf(index + 1));
        num.setPrefWidth(30);
        num.setStyle("-fx-text-fill: #B3B3B3;");

        VBox texts = new VBox(2);
        Label title = new Label(song.getTitle());
        title.getStyleClass().add("track-title");
        Label artist = new Label(song.getArtist() != null ? song.getArtist().getName() : "");
        artist.getStyleClass().add("track-artist");
        texts.getChildren().addAll(title, artist);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        Label time = new Label(song.getFormattedDuration());
        time.setStyle("-fx-text-fill: #B3B3B3;");

        Button addBtn = new Button("➕");
        addBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #B3B3B3; -fx-cursor: hand;");
        addBtn.setOnAction(e -> {
            ContextMenu contextMenu = new ContextMenu();
            if (dataManager.getAllPlaylists().isEmpty()) {
                MenuItem emptyItem = new MenuItem("Žádné playlisty");
                emptyItem.setDisable(true);
                contextMenu.getItems().add(emptyItem);
            } else {
                for (Playlist p : dataManager.getAllPlaylists()) {
                    MenuItem item = new MenuItem("Přidat do: " + p.getName());
                    item.setOnAction(ev -> {
                        p.addSong(song);
                        data.DataLoader.savePlaylists(dataManager, "src/main/resources/data/playlists.csv");
                    });
                    contextMenu.getItems().add(item);
                }
            }
            contextMenu.show(addBtn, BOTTOM, 0, 0);
        });

        row.getChildren().addAll(num, texts, spacer, time, addBtn);

        row.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                playerManager.setQueue(queue, index);
                playerUI.updateInfo();
            }
        });
        return row;
    }
}
