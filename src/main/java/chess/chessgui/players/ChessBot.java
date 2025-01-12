package chess.chessgui.players;

import chess.chessgui.ChessBoard;
import chess.chessgui.Comms;
import com.google.protobuf.InvalidProtocolBufferException;
import protocols.Chess.*;
import protocols.Common;


public class ChessBot extends Player {

    public ChessBot(PieceColor color) {
        super(color);
    }
    public void takeTurn(ChessBoard board){
        System.out.println("CHESS BOT TAKING TURN");

//        waitingForMove = true;
        FindBest findBest = FindBest.newBuilder()
                .setFenString(board.getBoardFen())
                .build();


//        new Thread(()->board.awaitMoveCompletion(this));

        board.tcpComms.send(Common.MessageID.FIND_BEST, findBest);


        board.awaitMoveCompletion(this);
//        var player = this;
//        new Thread(
//                new Task<Void>() {
//                    @Override
//                    protected Void call() throws Exception {
//                        System.out.println("TASK CALLED FOR PLAYER = " + color + " Moving");
//                        Comms.send(Common.MessageID.FIND_BEST, findBest, bytes -> processBestMoveNew(bytes, board));
//                        board.awaitMoveCompletion(player);
//                        return null;
//                    }
//                }
//        ).start();
    }


    private void processBestMoveNew(byte[] bytes, ChessBoard board) {
        try {
            System.out.println("PROCESSING FIND MOVE");
            FindBestResponse resp = FindBestResponse.parseFrom(bytes);
            board.handleComputerMove(resp);
//            int startRow = resp.getFromPos().getRow();
//            int startCol = resp.getFromPos().getCol();
//            var index = startRow * 8 + startCol;
//            if (resp.getPromotedPiece() != PieceType.NONE) {
//                Piece p = ChessBoard.getPiece(index);
//                p.promoteTo(resp.getPromotedPiece());
//            }
//
//            board.handleComputerMove(resp.getFromPos(), resp.getEndPos(), resp.getPromotedPiece());
//
//            Piece p = ChessBoard.getPiece(startRow * 8 + startCol % 8);
//            System.out.println("BOT MOVING FROM " + resp.getFromPos());
//            System.out.println("BOT MOVING TO " + resp.getEndPos());
//            Platform.runLater(() -> {
//                p.moveTo(ChessBoard.getTile(resp.getEndPos()));
//            });

        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}
