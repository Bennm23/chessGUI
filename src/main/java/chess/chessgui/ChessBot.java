package chess.chessgui;

import com.google.protobuf.InvalidProtocolBufferException;
import javafx.application.Platform;
import pieces.Piece;
import protocols.Chess.*;
import protocols.Common;


public class ChessBot {

    ChessBoard gameBoard;
    PieceColor myColor;
    public ChessBot(PieceColor color, ChessBoard board) {
       myColor = color;
       gameBoard = board;
    }
    public void takeTurn(){
        System.out.println("CHESS BOT TAKING TURN");
        GetBestMove bestMoveRequest = GetBestMove.newBuilder()
                .setBoard(ChessBoard.generateBoard())
                .setPlayer(myColor)
                .build();

        Comms.send(Common.MessageID.GET_BEST_MOVE, bestMoveRequest, this::processBestMove);

    }

    private void processBestMove(byte[] bytes) {
        try {
            System.out.println("PROCESSING BEST MOVE");
            BestMoveResponse resp = BestMoveResponse.parseFrom(bytes);
            Move move = resp.getBestMove();
            int startRow = move.getPieceToMove().getRow();
            int startCol = move.getPieceToMove().getCol();
            Piece p = ChessBoard.getPiece(startRow * 8 + startCol % 8);
            System.out.println("BOT MOVING TO " + move.getEndPosition());
            Platform.runLater(() -> {
                p.moveTo(ChessBoard.getTile(move.getEndPosition()));
            });

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
