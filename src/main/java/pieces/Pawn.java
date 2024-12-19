package pieces;

import protocols.Chess;

import java.io.Serializable;

public class Pawn extends Piece {
    public Pawn(String pieceColor) {
        super("pawn", pieceColor, Chess.PieceType.PAWN);
    }
}
