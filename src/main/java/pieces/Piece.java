package pieces;

import chess.chessgui.ChessBoard;
import chess.chessgui.ChessTile;
import chess.chessgui.Comms;
import com.google.protobuf.InvalidProtocolBufferException;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DataFormat;
import protocols.Chess.*;
import protocols.Common;
import utils.ChessUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Piece implements Serializable {
    public static final DataFormat CHESS_PIECE = new DataFormat("chess.figure");
    public static final Map<String, Image> cachedImages = new HashMap<>();

    String name;
    String color;
    String imageString;

    transient ChessTile tile;
    int row,col;
//    transient Position.Builder position = Position.newBuilder();
    transient final PieceType pieceType;
    transient ProtoPiece protoPiece;
    transient List<Position> validMoves = new ArrayList<>();

    public Piece(String pieceName, String pieceColor, PieceType type) {
        pieceType = type;
        color = pieceColor;
        name = pieceName;
        imageString = "images/" + color + "_" + name + ".png";
        updateProtoPiece();

        if (!cachedImages.containsKey(imageString)){
            cachedImages.put(imageString, ChessUtils.loadImage(imageString, ChessTile.TILE_SIZE,ChessTile.TILE_SIZE));
        }
    }

    public ProtoPiece getProtoPiece() {
        return protoPiece;
    }
    private void updateProtoPiece() {
        protoPiece = ProtoPiece.newBuilder()
                .setColor(getColor())
                .setCol(col)
                .setRow(row)
                .setType(pieceType)
                .build();
    }

    public void promoteTo(PieceType type) {

        Piece newPiece;
        switch (type) {
            case PAWN -> newPiece = new Pawn(color);
            case KNIGHT -> newPiece = new Knight(color);
            case BISHOP -> newPiece = new Bishop(color);
            case ROOK -> newPiece = new Rook(color);
            case QUEEN -> newPiece = new Queen(color);
            case KING -> newPiece = new King(color);
            default -> newPiece = null;
        }
        if (newPiece == null) return;

        newPiece.setPosition(row, col);

        tile.clearPiece();
        tile.setPiece(newPiece);
        tile = null;
    }

    public void moveTo(ChessTile destTile) {
        moveTo(destTile, true);
    }
    public void moveTo(ChessTile destTile, boolean primaryMove) {
        System.out.println("MOVE TO CALLED");
//        if (isPiece(PieceColor.BLACK, PieceType.KING)) {
//            if (ChessBoard.black_castle && destTile.getCol() == 6) {
//                //Short castle happening
//                ChessTile rookTile = ChessBoard.getTile(0, 7);
//                ChessTile rookDest = ChessBoard.getTile(0, 5);
//                rookTile.getPiece().moveTo(rookDest, false);
//            } else if (ChessBoard.black_long_castle && destTile.getCol() == 2) {
//                //Long castle happening
//                ChessTile rookTile = ChessBoard.getTile(0, 0);
//                ChessTile rookDest = ChessBoard.getTile(0, 3);
//                rookTile.getPiece().moveTo(rookDest, false);
//            }
//        } else if (isPiece(PieceColor.WHITE, PieceType.KING)) {
//            if (ChessBoard.white_castle && destTile.getCol() == 6) {
//                //Short castle happening
//                ChessTile rookTile = ChessBoard.getTile(7, 7);
//                ChessTile rookDest = ChessBoard.getTile(7, 5);
//                rookTile.getPiece().moveTo(rookDest, false);
//            } else if (ChessBoard.white_long_castle && destTile.getCol() == 2) {
//                //Long castle happening
//                ChessTile rookTile = ChessBoard.getTile(7, 0);
//                ChessTile rookDest = ChessBoard.getTile(7, 3);
//                rookTile.getPiece().moveTo(rookDest, false);
//            }
//        }


        //Check EnPassant
        String enPassantTile = "";
        if (isPiece(PieceColor.BLACK, PieceType.PAWN)) {
            if (tile.getRow() == 1 && destTile.getRow() == 3) {
                enPassantTile = colToFile(tile.getCol()) + "6"; //Rust chess is indexed by 1 as bottom
            }

        } else if (isPiece(PieceColor.WHITE, PieceType.PAWN)) {
            if (tile.getRow() == 6 && destTile.getRow() == 4) {
                enPassantTile = colToFile(tile.getCol()) + "3"; //Rust chess is indexed by 0 as bottom
            }
        }
        tile.clearPiece();
        tile = destTile;
        row = tile.getRow();
        col = tile.getCol();
        updateProtoPiece();
        tile.setPiece(this);

//        if (primaryMove) ChessBoard.completeMove(enPassantTile);
    }

    public static String colToFile(int col) {
        switch (col) {
            case 0 -> { return "a"; }
            case 1 -> { return "b"; }
            case 2 -> { return "c"; }
            case 3 -> { return "d"; }
            case 4 -> { return "e"; }
            case 5 -> { return "f"; }
            case 6 -> { return "g"; }
            case 7 -> { return "h"; }
            default -> { return ""; }
        }
    }

    public void requestValidMoves(Board board) {
        GetValidMoves validMovesMsg = GetValidMoves.newBuilder()
                .setBoard(board)
                .setPieceToMove(protoPiece)
                .build();
        validMoves.clear();
        Comms.send(Common.MessageID.GET_VALID_MOVES, validMovesMsg, this::handleValidMoveResponse);
    }
    void handleValidMoveResponse(byte[] bytes) {
        try {
            ValidMovesResponse response = ValidMovesResponse.parseFrom(bytes);
            validMoves.addAll(response.getMovesList());
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }

    public void clearValids() {
        validMoves.clear();
    }
    public void updateValids(List<Position> valids) {
        validMoves.clear();
        validMoves.addAll(valids);
    }

    public List<Position> getValidMoves() {
        return validMoves;
    }

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
        updateProtoPiece();
    }
    public void setTile(ChessTile chessTile) {
        tile = chessTile;
    }
    public int getCol() {
        return col;
    }
    public int getRow() {
        return row;
    }

    public PieceType getType() {
        return pieceType;
    }

    public PieceColor getColor() {
        return color.equals("black") ? PieceColor.BLACK : PieceColor.WHITE;
    }
    public Image getImage(){
        return cachedImages.get(imageString);
    }
    public ImageView getImageView() {
        return new ImageView(cachedImages.get(imageString));
    }

    public boolean tileIsValid(ChessTile chessTile) {
        System.out.println("EVALUATING TILE AT ROW = " + chessTile.getRow() +
                " COL = " + chessTile.getCol());
        System.out.println("FOR PIECE AT ROW = " + row + " COL = " + col);
        for (Position p : validMoves) {
            System.out.println(p);
            if (p.getCol() == chessTile.getCol() && p.getRow() == chessTile.getRow())
                return true;
        }
        return false;
    }

    public ChessTile getTile() {
        return tile;
    }

    public boolean isPiece(PieceColor color, PieceType type) {
        return getColor().equals(color) && getType().equals(type);
    }
}
