package pieces;

import protocols.Chess;

public class King extends Piece{
    public King(String pieceColor) {
        super("king", pieceColor, Chess.PieceType.KING);
    }
}
