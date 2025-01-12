package chess.chessgui.players;

import chess.chessgui.ChessBoard;
import protocols.Chess;

public class RealPlayer extends Player {
    public RealPlayer(Chess.PieceColor color) {
        super(color);
    }

    @Override
    public void takeTurn(ChessBoard board) {
        System.out.println("Player = " + color + " Moving");
        board.awaitMoveCompletion(color);
    }
}
