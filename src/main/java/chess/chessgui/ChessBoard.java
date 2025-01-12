package chess.chessgui;

import chess.chessgui.players.Player;
import com.google.protobuf.InvalidProtocolBufferException;
import javafx.application.Platform;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import pieces.*;
import protocols.Chess.*;
import protocols.Common;
import utils.TcpComms;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static pieces.Piece.colToFile;

/**
 * Chess Board has 64 ChessTiles
 *
 * ChessTiles can have 0 or 1 Piece
 */
public class ChessBoard extends GridPane {
    private final static Color BLACK_BG_COLOR = Color.rgb(119, 153, 67);
    private final static Color WHITE_BG_COLOR = Color.rgb(255, 255, 240);

    List<ChessTile> tiles = new ArrayList<>();
    String boardFen = "";
    Board protoBoard;

    boolean black_long_castle = true;
    boolean black_castle = true;
    boolean white_long_castle = true;
    boolean white_castle = true;

    public AtomicBoolean freePlayOn = new AtomicBoolean(false);
    String lastEnPassant = "";

    AtomicBoolean latestMoveCompleted = new AtomicBoolean(false);
    AtomicInteger playerExpected = new AtomicInteger(0);

    public TcpComms tcpComms = new TcpComms(this::handleProtoMsg);

    public ChessBoard() {
        super();
        tcpComms.start();
        setup_game();
    }

    public Piece findPiece(Position pos) {
        return findTile(pos).getPiece();
    }
    public Piece findPiece(int row, int col) {
        return findTile(row, col).getPiece();
    }
    public ChessTile findTile(int row, int col) {
        return tiles.get(row * 8 + col);
    }
    public ChessTile findTile(Position pos) {
        return findTile(pos.getRow(), pos.getCol());
    }

    public void reset() {
        tiles.clear();
        super.getChildren().clear();
        latestMoveCompleted.set(true);
        black_long_castle = true;
        black_castle = true;
        white_long_castle = true;
        white_castle = true;
        latestMoveCompleted.set(false);
        playerExpected.set(0);
        lastEnPassant = "";

        setup_game();
    }

    public void setup_game() {

//        Comms.resetClient();

        int count = 0;
        String pieceColor;
        Color backgroundColor;
        int row, col;
        Piece piece = null;
        for (int i = 0; i < 64; i++) {
            if (i % 8 == 0) count--;
            count++;
            pieceColor = (count > 16) ? "white" : "black";
            backgroundColor = (count % 2 == 0) ? WHITE_BG_COLOR : BLACK_BG_COLOR;
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
                tile = new ChessTile(backgroundColor, this::handleManualMove, this::handlePieceHover);
            } else {
                tile = new ChessTile(piece, backgroundColor, this::handleManualMove, this::handlePieceHover);
                piece.setPosition(row, col);
            }
            tile.setRow(row);
            tile.setCol(col);

            this.add(tile, col, row);
            tiles.add(i, tile);
        }
        updateProtoBoard(PieceColor.WHITE, 0);
    }

    public void handlePieceHover(Piece piece, boolean hoverEnter) {
//        GetValidMoves validMovesMsg = GetValidMoves.newBuilder()
//                .setBoard(protoBoard)
//                .setPieceToMove(piece.getProtoPiece())
//                .build();
//        tcpComms.send(Common.MessageID.GET_VALID_MOVES, validMovesMsg);
//        piece.requestValidMoves(protoBoard);
        for (Position p : piece.getValidMoves()) {
            ChessTile t = findTile(p);
            t.setAccessibleHighlight(hoverEnter);
        }
    }

    ChessTile lastSrcTile;
    ChessTile lastDestTile;

    public void handleManualMove(int pieceRow, int pieceCol, ChessTile destinationTile) {
        Piece pieceToMove = findPiece(pieceRow, pieceCol);
        checkIfCastleMove(pieceToMove, destinationTile);
        ChessTile currTile = pieceToMove.getTile();
        checkForEnPassant(pieceToMove, currTile, destinationTile);

        if (lastDestTile != null && lastSrcTile != null) {
            lastSrcTile.setLastMoveHighlight(false);
            lastDestTile.setLastMoveHighlight(false);
        }

        boolean moveValid = false;
        if (freePlayOn.get()) {
            pieceToMove.moveTo(destinationTile);
            moveValid = true;
        } else if (pieceToMove.getColor().getNumber() == playerExpected.get() && !pieceToMove.getValidMoves().isEmpty() && pieceToMove.tileIsValid(destinationTile)) {
            pieceToMove.moveTo(destinationTile);
            moveValid = true;
        }
        if (moveValid) {
            lastSrcTile = currTile;
            lastDestTile = destinationTile;
            currTile.setLastMoveHighlight(true);
            destinationTile.setLastMoveHighlight(true);
            completeMove();
        }
    }
    private void handleProtoMsg(Common.MessageID messageID, byte[] bytes) throws InvalidProtocolBufferException {
        switch (messageID) {
            case FIND_BEST_RESPONSE -> {
                FindBestResponse f = FindBestResponse.parseFrom(bytes);
                handleComputerMove(f);
            }
            case VALID_MOVES_RESPONSE -> {
                ValidMovesResponse response = ValidMovesResponse.parseFrom(bytes);
                ProtoPiece piece = response.getRequestPiece();
                var realPiece = findPiece(piece.getRow(), piece.getCol());
                realPiece.updateValids(response.getMovesList());
//                System.out.println("Vald MOves List = " + response.getMovesList());
//
//                for (Position p : realPiece.getValidMoves()) {
//                    ChessTile t = findTile(p);
//                    t.setAccessibleHighlight(true);
//                }
            }
        }
    }


    public void handleComputerMove(FindBestResponse bestMoveResponse) {
//        var index = startRow * 8 + startCol;
        System.out.println("GOT COMPUTER MOVE RESPONSE");
        if (bestMoveResponse.getPromotedPiece() != PieceType.NONE) {
            Piece p = findPiece(bestMoveResponse.getFromPos());
            p.promoteTo(bestMoveResponse.getPromotedPiece());
        }
        //Grab the piece after promotion
        ChessTile sourceTile = findTile(bestMoveResponse.getFromPos());
        Piece pieceToMove = sourceTile.getPiece();
        ChessTile destinationTile = findTile(bestMoveResponse.getEndPos());
        System.out.println("BOT MOVING FROM\n " + bestMoveResponse.getFromPos());
        System.out.println("BOT MOVING TO\n " + bestMoveResponse.getEndPos());

        checkIfCastleMove(pieceToMove, destinationTile);
        checkForEnPassant(pieceToMove, sourceTile, destinationTile);

        pieceToMove.moveTo(destinationTile);
        if (lastDestTile != null && lastSrcTile != null) {
            lastSrcTile.setLastMoveHighlight(false);
            lastDestTile.setLastMoveHighlight(false);
        }
        lastSrcTile = sourceTile;
        lastDestTile = destinationTile;
        sourceTile.setLastMoveHighlight(true);
        destinationTile.setLastMoveHighlight(true);

        completeMove();
    }

    private void checkIfCastleMove(Piece pieceToMove, ChessTile destTile) {
        if (pieceToMove.isPiece(PieceColor.BLACK, PieceType.KING)) {
            if (black_castle && destTile.getCol() == 6) {
                //Short castle happening
                ChessTile rookTile = findTile(0, 7);
                ChessTile rookDest = findTile(0, 5);
                rookTile.getPiece().moveTo(rookDest, false);
                black_castle = false;
                black_long_castle = false;
            } else if (black_long_castle && destTile.getCol() == 2) {
                //Long castle happening
                ChessTile rookTile = findTile(0, 0);
                ChessTile rookDest = findTile(0, 3);
                rookTile.getPiece().moveTo(rookDest, false);
                black_castle = false;
                black_long_castle = false;
            }
        } else if (pieceToMove.isPiece(PieceColor.WHITE, PieceType.KING)) {
            if (white_castle && destTile.getCol() == 6) {
                //Short castle happening
                ChessTile rookTile = findTile(7, 7);
                ChessTile rookDest = findTile(7, 5);
                rookTile.getPiece().moveTo(rookDest, false);
                white_castle = false;
                white_long_castle = false;
            } else if (white_long_castle && destTile.getCol() == 2) {
                //Long castle happening
                ChessTile rookTile = findTile(7, 0);
                ChessTile rookDest = findTile(7, 3);
                rookTile.getPiece().moveTo(rookDest, false);
                white_castle = false;
                white_long_castle = false;
            }
        }
    }


    public void checkForEnPassant(Piece pieceToMove, ChessTile currTile, ChessTile destTile) {
        //Check EnPassant
        String enPassantTile = "";
        if (pieceToMove.isPiece(PieceColor.BLACK, PieceType.PAWN)) {
            if (currTile.getRow() == 1 && destTile.getRow() == 3) {
                enPassantTile = colToFile(currTile.getCol()) + "6"; //Rust chess is indexed by 1 as bottom
            }

        } else if (pieceToMove.isPiece(PieceColor.WHITE, PieceType.PAWN)) {
            if (currTile.getRow() == 6 && destTile.getRow() == 4) {
                enPassantTile = colToFile(currTile.getCol()) + "3"; //Rust chess is indexed by 0 as bottom
            }
        }
        lastEnPassant = enPassantTile;
    }

    public void updateProtoBoard(PieceColor playerToMove, int turnCount){
//        if (protoBoard != null && turnCount.get() == protoBoard.getTurnCount()) return protoBoard;
        Board.Builder boardBuilder = Board.newBuilder();
        for (ChessTile tile : tiles) {
           boardBuilder.addPieces(tile.getProtoPiece());
        }
        boardBuilder.setTurnCount(turnCount);
        boardBuilder.setPlayerToMove(playerToMove);

        ChessTile blackKingTile = findTile(0, 4);
        if (blackKingTile.piece == null || !blackKingTile.piece.isPiece(PieceColor.BLACK, PieceType.KING)) {
            black_long_castle = false;
            black_castle = false;
        } else if (black_long_castle) {
            ChessTile longRook = findTile(0, 0);
            if (longRook.piece == null || !longRook.piece.isPiece(PieceColor.BLACK, PieceType.ROOK)) {
                black_long_castle = false;
            }
        } else if (black_castle) {
            ChessTile shortRook = findTile(0, 7);
            if (shortRook.piece == null || !shortRook.piece.isPiece(PieceColor.BLACK, PieceType.ROOK)) {
                black_castle = false;
            }
        }

        ChessTile whiteKingTile = findTile(7, 4);
        if (whiteKingTile.piece == null || !whiteKingTile.piece.isPiece(PieceColor.WHITE, PieceType.KING)) {
            white_long_castle = false;
            white_castle = false;
        } else if (white_long_castle) {
            ChessTile longRook = findTile(7, 0);
            if (longRook.piece == null || !longRook.piece.isPiece(PieceColor.WHITE, PieceType.ROOK)) {
                white_long_castle = false;
            }
        } else if (white_castle) {
            ChessTile shortRook = findTile(7, 7);
            if (shortRook.piece == null || !shortRook.piece.isPiece(PieceColor.WHITE, PieceType.ROOK)) {
                white_castle = false;
            }
        }

        updateFen(playerToMove, turnCount);

        boardBuilder.setBlackLongCastle(black_long_castle);
        boardBuilder.setBlackCastle(black_castle);
        boardBuilder.setWhiteLongCastle(white_long_castle);
        boardBuilder.setWhiteCastle(white_castle);

        protoBoard = boardBuilder.build();

        for (ChessTile tile : tiles) {
            ProtoPiece p = tile.getProtoPiece();
            if (p.getType() != PieceType.NONE) {
                GetValidMoves validMovesMsg = GetValidMoves.newBuilder()
                        .setBoard(protoBoard)
                        .setPieceToMove(p)
                        .build();
                tcpComms.send(Common.MessageID.GET_VALID_MOVES, validMovesMsg);
                sleep(1);
            }
        }

        Platform.runLater(() -> {
            super.requestLayout();
        });
    }

    public void updateFen(PieceColor playerToMove, int turnCount) {
        StringBuilder fen = new StringBuilder();
        int emptyCounter = 0;
        int counter = 0;
        for (ChessTile tile : tiles) {
            var curr = tile.getProtoPiece();
            var pieceFen = getFenVal(curr);

            //Piece empty
            if (pieceFen.isBlank()) {
                emptyCounter++;
            } else {
                if (emptyCounter != 0) {
                    fen.append(emptyCounter);
                    emptyCounter = 0;
                }
                fen.append(pieceFen);
            }

            if (++counter % 8 == 0 && counter != 64) {
                if (emptyCounter != 0) {
                    fen.append(emptyCounter);
                    emptyCounter = 0;
                }
                //wrap row
                fen.append("/");
            }
        }
        fen.append(" ");
        if (playerToMove == PieceColor.WHITE) {
            fen.append("w");
        } else {
            fen.append("b");
        }
        var castleString = "";
//        if (white_castle) castleString += "K";
//        if (white_long_castle) castleString += "Q";//Fix castling later
//        if (black_castle) castleString += "k";
//        if (black_long_castle) castleString += "q";

        if (castleString.isBlank()) fen.append(" -");
        else fen.append(" ").append(castleString);

        if (lastEnPassant.isBlank()) {
            fen.append(" -");//En Passent Field
        } else {
            fen.append(" ").append(lastEnPassant);
        }
        fen.append(" 0");//Half moves

        fen.append(" ").append(turnCount);

        boardFen = fen.toString();
        System.out.println("FEN STRING = " + fen);
    }
    public static String getFenVal(ProtoPiece piece) {
        String pieceVal;

        switch (piece.getType()) {
            case PAWN -> pieceVal = "p";
            case BISHOP -> pieceVal = "b";
            case KNIGHT -> pieceVal = "n";
            case ROOK -> pieceVal = "r";
            case QUEEN -> pieceVal = "q";
            case KING -> pieceVal = "k";
            default -> pieceVal = "";
        }

        if (piece.getColor() == PieceColor.WHITE)
            return pieceVal.toUpperCase();

        return pieceVal;
    }

    public String getBoardFen() {
        return boardFen;
    }

    public void setFreePlayOn(boolean on) {
        freePlayOn.set(on);
    }

    private static boolean sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {
//            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public void awaitMoveCompletion(Player player) {
        latestMoveCompleted.set(false);
        playerExpected.set(player.getColor().getNumber());

        while (!latestMoveCompleted.get()) {
            if (!sleep(10)) return; //Game interrupted
        }
        System.out.println("Move completed in await");
        player.setWaitingForMove(false);
//        updateProtoBoard(player == PieceColor.WHITE ? PieceColor.BLACK : PieceColor.WHITE);
    }

    SimpleBooleanProperty validMoveMade = new SimpleBooleanProperty(false);

    public void completeMove() {
        System.out.println("MOVE COMPLETED!");
        latestMoveCompleted.set(true);
        validMoveMade.set(true);
        validMoveMade.set(false);//clear immediately
    }

    public static int getCol(int total) {
        return total % 8;
    }
    public static int getRow(int total) {
        return total / 8;
    }
}
