package pieces;

import protocols.Chess;

public class Rook extends Piece{
    public Rook(String pieceColor) {
        super("rook", pieceColor, Chess.PieceType.ROOK);
    }
}
