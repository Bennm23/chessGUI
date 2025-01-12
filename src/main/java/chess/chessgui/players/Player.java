package chess.chessgui.players;

import chess.chessgui.ChessBoard;
import protocols.Chess;

public abstract class Player {

    Chess.PieceColor color;

    public Player(Chess.PieceColor color) {
        this.color = color;
    }

    public abstract void takeTurn(ChessBoard board);

    public boolean myTurn(Chess.PieceColor colorToMove) {
        return color == colorToMove;
    }

    public Chess.PieceColor getColor() {
        return color;
    }
}
