package chess.chessgui.players;

import chess.chessgui.ChessBoard;
import protocols.Chess.FindBest;
import protocols.Chess.PieceColor;
import protocols.Common;


public class ChessBot extends Player {

    public ChessBot(PieceColor color) {
        super(color);
    }

    public void takeTurn(ChessBoard board) {
        System.out.println("CHESS BOT TAKING TURN");

        FindBest findBest = FindBest.newBuilder()
                .setFenString(board.getBoardFen())
                .build();

        board.tcpComms.send(Common.MessageID.FIND_BEST, findBest);
        board.awaitMoveCompletion(color);

    }
}
