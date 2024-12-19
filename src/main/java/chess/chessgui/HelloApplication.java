package chess.chessgui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.google.protobuf.*;
import protocols.Common.*;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

//        Comms.setupClient();
//        sleep(2000);
//        ValidatePosition validatePosition = ValidatePosition.newBuilder()
//                .setValidate(false)
//                .setValidateCount(3)
//                .build();
//        Comms.send(MessageID.VALIDATE_POSITION, validatePosition, this::handle);
        ChessBoard board = new ChessBoard();

//        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("hello-view.fxml"));
//        Scene scene = new Scene(fxmlLoader.load(), 320, 240);
        Scene scene = new Scene(board);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();

        ChessBoard.startGame();

//
//        MovePiece movePiece = MovePiece.newBuilder()
//                .setFrom(1)
//                .setTo(2)
//                .build();
//        sleep(2000);
//        comms.send(MessageID.MOVE_PIECE, movePiece);
//        sleep(2000);
//        comms.send(MessageID.VALIDATE_POSITION, validatePosition);
    }

    private void handle(byte[] bytes) {
        System.out.println("GOT RESPONSE");
        System.out.println("INT VAL = " + ByteBuffer.wrap(bytes).getInt());
    }

    public void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }
}