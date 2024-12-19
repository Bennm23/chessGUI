package pieces;

import protocols.Chess;

public class Bishop extends Piece{
    public Bishop(String pieceColor) {
        super("bishop", pieceColor, Chess.PieceType.BISHOP);
    }
}
