package chess.chessgui;

import chess.chessgui.players.ChessBot;
import chess.chessgui.players.Player;
import chess.chessgui.players.RealPlayer;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import protocols.Chess;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicBoolean;

public class GuiController implements Initializable {

    @FXML AnchorPane root;
    @FXML VBox boardContainer;

    @FXML ToggleButton freePlaceToggle;
    @FXML Button gameControlButton;

    @FXML ComboBox<PlayerType> whiteChoiceCB;
    @FXML ComboBox<PlayerType> blackChoiceCB;

    static ChessBoard board = new ChessBoard();
    Game game;

    AtomicBoolean gameWatcher = new AtomicBoolean(false);

    public GuiController() {
//        game = new Game(new ChessBot(Chess.PieceColor.WHITE), new RealPlayer(Chess.PieceColor.BLACK), board);
//        game.startGame();
//        start();
    }

    static void start() {
        new Thread(() -> {
            Game game = new Game(new ChessBot(Chess.PieceColor.WHITE), new RealPlayer(Chess.PieceColor.BLACK), board);
            game.startGame();

        }).start();
    }

    void startGameWatcher() {
        new Thread(() -> {
            boolean last = false;
            while (!Thread.currentThread().isInterrupted()) {

                while (last == gameWatcher.get()) {
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException ignored) {}
                }
                last = gameWatcher.get();

                if (game != null && game.gameStarted()) {
                    game.resetGame();
                    Platform.runLater(() -> gameControlButton.setText("Start Game"));
                } else if (game == null || !game.gameStarted()) {
                    game = new Game(
                            whiteChoiceCB.getSelectionModel().getSelectedItem().getPlayer(Chess.PieceColor.WHITE),
                            blackChoiceCB.getSelectionModel().getSelectedItem().getPlayer(Chess.PieceColor.BLACK),
                            board
                    );
                    Platform.runLater(() -> gameControlButton.setText("Reset Game"));
                    game.startGame();
                }

            }
        }).start();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        boardContainer.getChildren().add(board);

        freePlaceToggle.selectedProperty().addListener((obv, ov, nv) -> {
            if (nv) {
                freePlaceToggle.setText("Free Place");
                board.setFreePlayOn(false);
            } else {
                freePlaceToggle.setText("Live Play");
                board.setFreePlayOn(true);
            }
        });

        whiteChoiceCB.getItems().addAll(PlayerType.values());
        whiteChoiceCB.getSelectionModel().select(0);
        blackChoiceCB.getItems().addAll(PlayerType.values());
        blackChoiceCB.getSelectionModel().select(0);

        gameControlButton.setOnAction(evt -> {

//            gameWatcher.set(!gameWatcher.get());
            if (game != null && game.gameStarted()) {
                game.resetGame();
                gameControlButton.setText("Start Game");
            } else if (game == null || !game.gameStarted()) {
                game = new Game(
                        whiteChoiceCB.getSelectionModel().getSelectedItem().getPlayer(Chess.PieceColor.WHITE),
                        blackChoiceCB.getSelectionModel().getSelectedItem().getPlayer(Chess.PieceColor.BLACK),
                        board
                );
                gameControlButton.setText("Reset Game");
               game.startGame();
            }
        });

    }



    enum PlayerType {
        HUMAN("Human"),
        COMPUTER("Computer");

        final String displayStr;
        PlayerType(String display) {
            displayStr = display;
        }

        @Override
        public String toString() {
            return displayStr;
        }

        public Player getPlayer(Chess.PieceColor color) {
            if (this == PlayerType.HUMAN) {
                return new RealPlayer(color);
            }
            return new ChessBot(color);
        }
    }
}
