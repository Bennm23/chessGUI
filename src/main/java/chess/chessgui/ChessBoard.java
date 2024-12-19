package chess.chessgui;

import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import pieces.*;
import protocols.Chess.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Chess Board has 64 ChessTiles
 *
 * ChessTiles can have 0 or 1 Piece
 */
public class ChessBoard extends GridPane {
    List<ChessTile> tiles = new ArrayList<>();
    public static ChessBoard INST;
    static Board protoBoard;
    static final AtomicInteger turnCount = new AtomicInteger(-1);
    static PieceColor playerTurn = PieceColor.WHITE;
    public ChessBoard() {
        super();
        INST = this;
        Comms.setupClient();

        int count = 0;
        String pieceColor;
        Color backgroundColor;
        int row, col;
        Piece piece = null;
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) count--;
            count++;
            pieceColor = (count > 16) ? "white" : "black";
            backgroundColor = (count % 2 == 0) ? Color.WHITE : Color.GRAY;
            ChessTile tile;

            row = getRow(i);
            col = getCol(i);
            Position.Builder position = Position.newBuilder();
            position.setRow(row).setCol(col);

            if (row == 1 || row == 6) {
                piece = new Pawn(pieceColor);
            } else if (row == 0 || row == 7) {
                if (col == 0 || col == 7) {
                    piece = new Rook(pieceColor);
                } else if (col == 1 || col == 6) {
                    piece = new Knight(pieceColor);
                } else if (col == 2 || col == 5) {
                    piece = new Bishop(pieceColor);
                } else if (col == 3) {
                    piece = new Queen(pieceColor);
                } else {
                    piece = new King(pieceColor);
                }
            } else {
                piece = null;
            }
            if (piece == null) {
                tile = new ChessTile(backgroundColor);
            } else {
                tile = new ChessTile(piece, backgroundColor);
//                piece.setPosition(position.build());
                piece.setPosition(row, col);
//                piece.setPosition(Position.newBuilder()
//                        .setCol(col)
//                        .setRow(row));
            }
//            tile.setPosition(position.build());
            tile.setRow(row);
            tile.setCol(col);

            this.add(tile, col, row);
            tiles.add(i, tile);

        }
        protoBoard = generateBoard();
        turnCount.getAndIncrement();

    }
    public static Piece getPiece(int index) {
        return INST.tiles.get(index).getPiece();
    }

    public static ChessTile getTile(Position p) {
        return INST.tiles.get(p.getRow() * 8 + p.getCol() % 8);
    }

    public static Board generateBoard(){
        if (protoBoard != null && turnCount.get() == protoBoard.getTurnCount()) return protoBoard;
        Board.Builder boardBuilder = Board.newBuilder();
        for (ChessTile tile : INST.tiles) {
           boardBuilder.addPieces(tile.getProtoPiece());
        }
        boardBuilder.setTurnCount(turnCount.get());
        boardBuilder.setPlayerToMove(playerTurn);
        protoBoard = boardBuilder.build();
        return protoBoard;
    }

    public static boolean gameOver = false;
    static ChessBot bot;
    public static void startGame() {
        bot = new ChessBot(PieceColor.WHITE, INST);
        new Thread(() -> takeTurn(turnCount.get())).start();

    }

    private static void takeTurn(int turnCount) {

        if (turnCount % 2 == 0) bot.takeTurn();
        while (ChessBoard.turnCount.get() == turnCount){

        }
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
        }
        takeTurn(ChessBoard.turnCount.get());
    }

    public static void nextTurn() {
        System.out.println("NEXT TURN CALLED");
       turnCount.getAndIncrement();
       playerTurn = playerTurn.equals(PieceColor.WHITE) ? PieceColor.BLACK : PieceColor.WHITE;
    }

    public int getCol(int total) {
        return total % 8;
    }
    public int getRow(int total) {
        return total / 8;
    }
}
