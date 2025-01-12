package chess.chessgui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

//        ChessBoard board = new ChessBoard();

        var fxmlLoader = new FXMLLoader(getClass().getResource("/gui.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
//        Scene scene = new Scene(board);
        stage.setTitle("Chess");
        stage.setScene(scene);
        stage.show();

//        ChessBoard.startGame();
    }

    public static void main(String[] args) {
        launch();
    }
}