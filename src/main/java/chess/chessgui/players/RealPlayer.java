package chess.chessgui.players;

import chess.chessgui.ChessBoard;
import javafx.concurrent.Task;
import protocols.Chess;

public class RealPlayer extends Player {
    public RealPlayer(Chess.PieceColor color) {
        super(color);
    }

    @Override
    public void takeTurn(ChessBoard board) {
        waitingForMove = true;
        System.out.println("Player = " + color + " Moving");
        board.awaitMoveCompletion(this);

//        var player = this;
//        new Thread(
//                new Task<Void>() {
//                    @Override
//                    protected Void call() throws Exception {
//                        System.out.println("TASK CALLED FOR PLAYER = " + color + " Moving");
//                        board.awaitMoveCompletion(player);
//                        return null;
//                    }
//                }
//        ).start();
    }
}
