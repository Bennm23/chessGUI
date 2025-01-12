package chess.chessgui.players;

import chess.chessgui.ChessBoard;
import protocols.Chess;

public abstract class Player {

    Chess.PieceColor color;
    boolean waitingForMove = false;

    public Player(Chess.PieceColor color) {
        this.color = color;
    }

    public abstract void takeTurn(ChessBoard board);

    public boolean myTurn(Chess.PieceColor colorToMove) {
        return color == colorToMove;
    }

    public boolean isWaitingForMove() {
        return waitingForMove;
    }

    public void setWaitingForMove(boolean waitingForMove) {
        this.waitingForMove = waitingForMove;
    }

    public Chess.PieceColor getColor() {
        return color;
    }
}
