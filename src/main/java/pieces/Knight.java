package pieces;

import protocols.Chess;

public class Knight extends Piece{
    public Knight(String pieceColor) {
        super("knight", pieceColor, Chess.PieceType.KNIGHT);
    }
}
