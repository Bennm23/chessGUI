package pieces;

import protocols.Chess;

public class Queen extends Piece{
    public Queen(String pieceColor) {
        super("queen", pieceColor, Chess.PieceType.QUEEN);
    }
}
